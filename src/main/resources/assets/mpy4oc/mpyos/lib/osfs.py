"""osfs.py -- mpyos's filesystem layer for the standard `os` module.

The frozen VM `os` module deliberately has no filesystem ("sandbox-appropriate
bits only"): what a file IS depends on the host. Here the host is mpyos with OC
disks, so at boot init.py calls install(os, vfs), which plants the standard
filesystem API (listdir/mkdir/remove/stat/os.path/...) straight into the frozen
module's namespace. User code then just does `import os` and gets the standard
experience; urandom/uname from the frozen module stay untouched.

Working directory: os keeps its own cwd (getcwd/chdir). The shell's `cd` syncs
it (shell -> os), so scripts see the shell's directory; a script's own os.chdir
affects os.* relative paths for that script without moving the shell.
"""

import fs

_vfs = None
_cwd = "/"

sep = "/"


def install(os_module, vfs):
    """Wire the shared Vfs and plant the filesystem API into `os_module`."""
    global _vfs
    _vfs = vfs
    import ospath
    os_module.path = ospath
    os_module.sep = sep
    os_module.getcwd = getcwd
    os_module.chdir = chdir
    os_module.listdir = listdir
    os_module.mkdir = mkdir
    os_module.makedirs = makedirs
    os_module.rmdir = rmdir
    os_module.remove = remove
    os_module.unlink = remove
    os_module.rename = rename
    os_module.stat = stat
    os_module.statvfs = statvfs


def shared_vfs():
    """The Vfs given to install(), so the shell can reuse it (single mount state)."""
    return _vfs


def _v():
    if _vfs is None:
        raise OSError("os: filesystem not installed (mpyos not booted)")
    return _vfs


def _abs(path):
    """Absolute, normalized path; relative resolves against os's cwd."""
    if not path or path == ".":
        return _cwd
    if path.startswith("/"):
        return fs.normalize(path)
    return fs.normalize(_cwd + "/" + path)


# -- directory state ---------------------------------------------------------

def getcwd():
    return _cwd


def chdir(path):
    global _cwd
    t = _abs(path)
    if not _v().is_directory(t):
        raise OSError("chdir: not a directory: " + t)
    _cwd = t


# -- queries -----------------------------------------------------------------

def listdir(path="."):
    t = _abs(path)
    v = _v()
    if not v.is_directory(t):
        raise OSError("listdir: not a directory: " + t)
    out = []
    for name in v.list(t):
        # mpyos's fs marks directories with a trailing '/'; os.listdir gives
        # bare names like CPython.
        out.append(name[:-1] if name.endswith("/") else name)
    return out


def stat(path):
    """MicroPython-style 10-tuple: (mode, ino, dev, nlink, uid, gid, size,
    atime, mtime, ctime). mode is 0x4000 for a directory, 0x8000 for a file;
    times are seconds (OC reports milliseconds)."""
    t = _abs(path)
    v = _v()
    if not v.exists(t):
        raise OSError("stat: no such file or directory: " + t)
    is_dir = v.is_directory(t)
    mode = 0x4000 if is_dir else 0x8000
    size = 0 if is_dir else (v.size(t) or 0)
    mtime = (v.last_modified(t) or 0) // 1000
    return (mode, 0, 0, 0, 0, 0, size, mtime, mtime, mtime)


def statvfs(path="/"):
    """MicroPython-style 10-tuple with 1-byte blocks: (bsize, frsize, blocks,
    bfree, bavail, files, ffree, favail, flags, namemax)."""
    v = _v()
    total = v.space_total() or 0
    used = v.space_used() or 0
    free = total - used if total > used else 0
    return (1, 1, total, free, free, 0, 0, 0, 0, 255)


# -- mutations ---------------------------------------------------------------

def mkdir(path):
    t = _abs(path)
    v = _v()
    if v.exists(t):
        raise OSError("mkdir: already exists: " + t)
    if not v.make_directory(t):
        raise OSError("mkdir: cannot create: " + t)


def makedirs(path, exist_ok=False):
    t = _abs(path)
    v = _v()
    if v.exists(t):
        if exist_ok and v.is_directory(t):
            return
        raise OSError("makedirs: already exists: " + t)
    # build each missing level; OC's makeDirectory is single-level on some
    # filesystems, so walk the segments explicitly
    parts = [p for p in t.split("/") if p]
    cur = ""
    for p in parts:
        cur = cur + "/" + p
        if v.exists(cur):
            if not v.is_directory(cur):
                raise OSError("makedirs: not a directory: " + cur)
            continue
        if not v.make_directory(cur):
            raise OSError("makedirs: cannot create: " + cur)


def rmdir(path):
    """Remove an EMPTY directory (unlike OC's recursive remove)."""
    t = _abs(path)
    v = _v()
    if not v.is_directory(t):
        raise OSError("rmdir: not a directory: " + t)
    if v.list(t):
        raise OSError("rmdir: directory not empty: " + t)
    if not v.remove(t):
        raise OSError("rmdir: cannot remove: " + t)


def remove(path):
    """Remove a FILE (directories need rmdir, matching CPython)."""
    t = _abs(path)
    v = _v()
    if not v.exists(t):
        raise OSError("remove: no such file: " + t)
    if v.is_directory(t):
        raise OSError("remove: is a directory (use rmdir): " + t)
    if not v.remove(t):
        raise OSError("remove: cannot remove: " + t)


def rename(src, dst):
    s = _abs(src)
    d = _abs(dst)
    v = _v()
    if not v.exists(s):
        raise OSError("rename: no such file or directory: " + s)
    if not v.rename(s, d):
        raise OSError("rename: cannot rename: " + s + " -> " + d)
