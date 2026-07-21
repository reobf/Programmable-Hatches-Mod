"""fs.py -- filesystem access for mpyos.

Wraps the raw OpenComputers filesystem component calls (component.invoke(addr,
"open"/"read"/...)) in a Python API, and presents the several filesystems on a
machine (the LiveCD, a hard disk, tmpfs) as one thing addressable by component
address.

mpyos runs off a read-only LiveCD, so this module never assumes it can write to
the disk it booted from; callers that need scratch space use a writable disk or a
tmpfs (see mount points below).
"""



# --- low-level: one filesystem component -------------------------------------

class Filesystem:
    """A single OC filesystem component, addressed by its component address."""

    def __init__(self, address):
        self.address = address

    def _invoke(self, method, *args):
        return component.invoke(self.address, method, *args)

    # -- queries --
    def exists(self, path):
        return bool(self._invoke("exists", path))

    def is_directory(self, path):
        return bool(self._invoke("isDirectory", path))

    def is_readonly(self):
        return bool(self._invoke("isReadOnly"))

    def size(self, path):
        return self._invoke("size", path) or 0

    def last_modified(self, path):
        return self._invoke("lastModified", path) or 0

    def space_used(self):
        return self._invoke("spaceUsed") or 0

    def space_total(self):
        return self._invoke("spaceTotal") or 0

    def label(self):
        return self._invoke("getLabel")

    def set_label(self, name):
        return self._invoke("setLabel", name)

    def list(self, path):
        """Names in a directory. Directories come back with a trailing '/'."""
        res = self._invoke("list", path)
        if not res:
            return []
        # OC hands back a 1-based table; our bridge turns it into a list.
        if isinstance(res, dict):
            return [res[k] for k in sorted(res)]
        return list(res)

    # -- mutations --
    def make_directory(self, path):
        return bool(self._invoke("makeDirectory", path))

    def remove(self, path):
        return bool(self._invoke("remove", path))

    def rename(self, frm, to):
        return bool(self._invoke("rename", frm, to))

    # -- handles --
    def open(self, path, mode="r"):
        h = self._invoke("open", path, mode)
        if not h:
            raise IOError("cannot open " + path)
        return h

    def _read_chunk(self, handle, n):
        return self._invoke("read", handle, n)

    def _write_chunk(self, handle, data):
        return self._invoke("write", handle, data)

    def close(self, handle):
        self._invoke("close", handle)

    def seek(self, handle, whence, offset):
        return self._invoke("seek", handle, whence, offset)

    # -- whole-file convenience --
    def read_file(self, path):
        """Read a whole file as text, or None if it does not exist."""
        if not self.exists(path):
            return None
        h = self.open(path, "r")
        try:
            chunks = []
            while True:
                chunk = self._read_chunk(h, 4096)
                if chunk is None:
                    break
                if isinstance(chunk, bytes):
                    chunk = chunk.decode("utf-8")
                chunks.append(chunk)
            return "".join(chunks)
        finally:
            self.close(h)

    def read_bytes(self, path):
        if not self.exists(path):
            return None
        h = self.open(path, "rb")
        try:
            # bytes.join is not available in this VM; accumulate instead.
            data = b""
            while True:
                chunk = self._read_chunk(h, 4096)
                if chunk is None:
                    break
                if isinstance(chunk, str):
                    chunk = chunk.encode("utf-8")
                data += chunk
            return data
        finally:
            self.close(h)

    def write_file(self, path, data):
        """Overwrite a file with text or bytes. Raises if this disk is read-only."""
        if self.is_readonly():
            raise IOError("read-only filesystem")
        h = self.open(path, "w")
        try:
            if isinstance(data, str):
                data = data.encode("utf-8")
            self._write_chunk(h, data)
        finally:
            self.close(h)


# --- path helpers ------------------------------------------------------------

def normalize(path):
    """Collapse '.', '..' and duplicate slashes into a clean absolute path."""
    parts = []
    for seg in path.replace("\\", "/").split("/"):
        if seg == "" or seg == ".":
            continue
        if seg == "..":
            if parts:
                parts.pop()
            continue
        parts.append(seg)
    return "/" + "/".join(parts)


def join(*segments):
    out = ""
    for s in segments:
        if not s:
            continue
        if out and not out.endswith("/"):
            out += "/"
        out += s
    return normalize(out)


def dirname(path):
    p = normalize(path)
    i = p.rfind("/")
    return "/" if i <= 0 else p[:i]


def basename(path):
    p = normalize(path)
    i = p.rfind("/")
    return p[i + 1:]


# --- discovery ---------------------------------------------------------------

def list_filesystems():
    """Every filesystem component on the machine, as Filesystem objects."""
    return [Filesystem(a) for a in component.list("filesystem")]


def get(address):
    return Filesystem(address)


def find_writable(exclude=None):
    """The first writable filesystem, optionally excluding some address (e.g. the
    read-only LiveCD you booted from). Used by install to pick a target disk."""
    for fs in list_filesystems():
        if exclude is not None and fs.address == exclude:
            continue
        if not fs.is_readonly():
            return fs
    return None


def tmp_address():
    """Address of the machine's built-in virtual temporary filesystem (the
    memory-backed "tmpfs" OC gives every computer -- it shows up in
    component.list('filesystem') but corresponds to no physical disk). None if
    the machine has none."""
    try:
        return computer.tmpAddress()
    except Exception:
        return None


def find_empty_writable(exclude=None):
    """The first writable filesystem with nothing on it -- install's target.
    The machine's tmpfs is never a candidate: it is tiny (64KB by default),
    vanishes on power-off, and is exactly the "ghost" disk that made install
    die with 'not enough space' when it got picked."""
    tmp = tmp_address()
    for fs in list_filesystems():
        if exclude is not None and fs.address == exclude:
            continue
        if tmp is not None and fs.address == tmp:
            continue
        if fs.is_readonly():
            continue
        try:
            if not fs.list("/"):
                return fs
        except Exception:
            continue
    return None


# --- VFS: boot disk with other filesystems auto-mounted under /mnt ------------
#
# OpenOS mounts every filesystem component (except the tmpfs) under
# /mnt/<address-prefix>, so a second hard disk shows up at e.g. /mnt/a1b and its
# files are reachable as ordinary paths. We mirror that: Vfs wraps the boot disk
# and a mount table, and routes each path-based call to whichever filesystem owns
# that path. Everything the shell and commands use goes through sh.fs, so making
# that a Vfs is enough -- ls / cd / read all become mount-aware for free.

_MNT = "/mnt"


def _mount_name(address, taken):
    """OpenOS-style mount name: the first 3 chars of the address, extended one
    char at a time until it doesn't collide with an already-taken name."""
    n = 3
    name = address[:n]
    while name in taken and n < len(address):
        n += 1
        name = address[:n]
    return name


def build_mounts(boot_addr):
    """Map every filesystem component (except the boot disk and the tmpfs) to a
    mount name under /mnt, the way OpenOS does on boot. Returns {name: address}."""
    tmp = tmp_address()
    mounts = {}
    taken = []
    for f in list_filesystems():
        addr = f.address
        if addr == boot_addr:
            continue
        if tmp is not None and addr == tmp:
            continue
        name = _mount_name(addr, taken)
        taken.append(name)
        mounts[name] = addr
    return mounts


class Vfs:
    """A filesystem view: the boot disk at /, plus auto-mounted disks under /mnt.
    Presents the same method surface as Filesystem, so it drops in as sh.fs."""

    def __init__(self, boot_addr):
        self.address = boot_addr
        self.boot = Filesystem(boot_addr)
        self._fs = {}         # address -> Filesystem (cache)
        self.mounts = build_mounts(boot_addr)   # name -> address

    def refresh(self):
        """Re-scan components (call when a disk is inserted/removed)."""
        self.mounts = build_mounts(self.address)

    def _disk(self, address):
        d = self._fs.get(address)
        if d is None:
            d = Filesystem(address)
            self._fs[address] = d
        return d

    def _route(self, path):
        """(filesystem, path_on_it) for an absolute path. Paths under /mnt/<name>
        go to that mounted disk (with the prefix stripped); everything else is on
        the boot disk."""
        p = normalize(path)
        if p == _MNT or p.startswith(_MNT + "/"):
            rest = p[len(_MNT):]        # "" or "/<name>/..."
            rest = rest[1:] if rest.startswith("/") else rest
            if rest:
                slash = rest.find("/")
                name = rest if slash < 0 else rest[:slash]
                sub = "/" if slash < 0 else rest[slash:]
                addr = self.mounts.get(name)
                if addr is not None:
                    return self._disk(addr), sub
        return self.boot, p

    # -- queries (path-based: routed) --
    def exists(self, path):
        # /mnt itself and each mount point exist as virtual directories
        p = normalize(path)
        if p == _MNT:
            return True
        if p.startswith(_MNT + "/"):
            rest = p[len(_MNT) + 1:]
            if rest in self.mounts:
                return True
        fs_, sub = self._route(path)
        return fs_.exists(sub)

    def is_directory(self, path):
        p = normalize(path)
        if p == _MNT or (p.startswith(_MNT + "/") and p[len(_MNT) + 1:] in self.mounts):
            return True
        fs_, sub = self._route(path)
        return fs_.is_directory(sub)

    def list(self, path):
        p = normalize(path)
        if p == _MNT:
            # the mount points themselves, shown as directories
            return [name + "/" for name in sorted(self.mounts.keys())]
        fs_, sub = self._route(path)
        return fs_.list(sub)

    def size(self, path):
        fs_, sub = self._route(path)
        return fs_.size(sub)

    def last_modified(self, path):
        fs_, sub = self._route(path)
        return fs_.last_modified(sub)

    def read_file(self, path):
        fs_, sub = self._route(path)
        return fs_.read_file(sub)

    def read_bytes(self, path):
        fs_, sub = self._route(path)
        return fs_.read_bytes(sub)

    def write_file(self, path, data):
        fs_, sub = self._route(path)
        return fs_.write_file(sub, data)

    def make_directory(self, path):
        fs_, sub = self._route(path)
        return fs_.make_directory(sub)

    def remove(self, path):
        fs_, sub = self._route(path)
        return fs_.remove(sub)

    def rename(self, frm, to):
        f1, s1 = self._route(frm)
        f2, s2 = self._route(to)
        if f1.address != f2.address:
            raise IOError("cannot rename across filesystems")
        return f1.rename(s1, s2)

    def open(self, path, mode="r"):
        fs_, sub = self._route(path)
        return fs_.open(sub, mode)

    # -- whole-disk queries: apply to the boot disk --
    def is_readonly(self):
        return self.boot.is_readonly()

    def space_used(self):
        return self.boot.space_used()

    def space_total(self):
        return self.boot.space_total()

    def label(self):
        return self.boot.label()
