# mpy BIOS -- stored in the EEPROM code section, run by MpyArchitecture at boot.
#
# Mirrors OpenComputers' bios.lua: find a bootable filesystem (honouring the boot
# address in the EEPROM data section, else scanning), read its /init.py, and run it.
# init.py then becomes the operating system -- exec() lets its definitions take over
# the global namespace, so control never returns here.
#
# This runs before mpyos exists, so it may only use the host primitives the
# architecture injects (component, computer, exec); no fs.py, no stdlib niceties.


def _read_file(fs, path):
    """Read a whole file from a filesystem component, or None if absent."""
    try:
        h = component.invoke(fs, "open", path)
    except Exception:
        return None
    if not h:
        return None
    try:
        chunks = []
        while True:
            chunk = component.invoke(fs, "read", h, 4096)
            if chunk is None:
                break
            # OC returns bytes for binary reads; decode to text for source.
            if isinstance(chunk, bytes):
                chunk = chunk.decode("utf-8")
            chunks.append(chunk)
        return "".join(chunks)
    finally:
        try:
            component.invoke(fs, "close", h)
        except Exception:
            pass


def _has_init(fs):
    try:
        return bool(component.invoke(fs, "exists", "/init.py"))
    except Exception:
        return False


def _exists(fs, path):
    try:
        return bool(component.invoke(fs, "exists", path))
    except Exception:
        return False


def _looks_like_lua(fs):
    """A Lua/OpenOS disk: has init.lua or the OpenOS layout, but no init.py.
    Used only to give a clearer error -- we cannot run Lua."""
    if _has_init(fs):
        return False
    return (_exists(fs, "/init.lua")
            or _exists(fs, "/boot")
            or _exists(fs, "/lib/core/boot.lua")
            or _exists(fs, "/OS.lua"))


def _find_boot_fs():
    """The filesystem to boot from: the boot address if set and valid, else the
    first filesystem that carries an /init.py (this is how the LiveCD is found)."""
    addr = computer.getBootAddress()
    if addr is not None and _has_init(addr):
        return addr
    # Boot address missing or stale: scan, and adopt the first bootable disk.
    for a in component.list("filesystem"):
        if _has_init(a):
            return a
    return None


def _fatal(msg):
    try:
        computer.beep(1000, 0.2)
    except Exception:
        pass
    print("mpy bios: " + msg)


def main():
    fs = _find_boot_fs()
    if fs is None:
        # Be helpful about the common mistake: a Lua/OpenOS disk in a MPYOS
        # machine. We can't run Lua -- point the player at the right BIOS/disk.
        for a in component.list("filesystem"):
            if _looks_like_lua(a):
                _fatal("this looks like a Lua/OpenOS disk. MPYOS BIOS runs "
                       "Python (/init.py), not Lua. Use a Lua BIOS for OpenOS, "
                       "or insert a MPYOS floppy/disk.")
                return
        _fatal("no bootable medium (need /init.py on a floppy or disk)")
        return

    src = _read_file(fs, "/init.py")
    if src is None:
        _fatal("could not read /init.py from " + str(fs))
        return

    # Hand the machine to init.py. It runs in this same global namespace, so it sees
    # component/computer and everything the bios has; its definitions replace ours.
    g = globals()
    g["__boot_fs__"] = fs          # let init know which disk it booted from
    exec(src, g)


main()
