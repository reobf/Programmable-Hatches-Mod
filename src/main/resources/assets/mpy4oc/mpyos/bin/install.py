# /bin/install.py -- install mpyos from the LiveCD onto a hard disk.
#
# What OpenOS's install does, distilled: pick the first empty writable disk,
# copy every file from the boot medium onto it, point the EEPROM's boot address
# at it, and reboot. After that the machine starts from the disk and the floppy
# can come out. The installed system is the same set of files -- the only thing
# that makes a disk "the system disk" is the boot address.
#
# Runs under the shell command protocol: args, tty, sh are provided.

import fs


def _copy_tree(src, dst, path):
    """Recursively copy `path` (a directory) from src to dst. Returns file count."""
    count = 0
    for name in src.list(path):
        is_dir = name.endswith("/")
        clean = name[:-1] if is_dir else name
        child = fs.join(path, clean)
        if is_dir:
            dst.make_directory(child)
            count += _copy_tree(src, dst, child)
        else:
            data = src.read_bytes(child)
            if data is None:
                tty.print("  skip (unreadable): " + child)
                continue
            dst.write_file(child, data)
            tty.print("  " + child)
            count += 1
    return count


def main():
    src = sh.fs   # the disk we booted from (the LiveCD)

    # Explicit target as an argument, else the first empty writable disk.
    if args:
        target = fs.get(args[0])
        try:
            if target.is_readonly():
                tty.print("install: target " + args[0] + " is read-only")
                return
        except Exception:
            tty.print("install: no such filesystem: " + args[0])
            return
    else:
        target = fs.find_empty_writable(exclude=src.address)
        if target is None:
            tty.print("install: no empty writable disk found")
            tty.print("(add a hard disk, or pass a filesystem address explicitly)")
            return

    tty.print("installing mpyos")
    tty.print("  from: " + str(src.address))
    tty.print("  to:   " + str(target.address))

    if target.address == fs.tmp_address():
        tty.print("install: refusing target " + str(target.address))
        tty.print("(that is the machine's built-in temporary filesystem -- a 64KB")
        tty.print(" memory disk that vanishes on power-off. add a hard disk.)")
        return

    # Space precheck: total source bytes plus OC's per-file overhead, against
    # what the target has free. Failing here beats dying mid-copy with a
    # half-written system left on the disk.
    def _tree_size(f, path):
        total = 0
        nfiles = 0
        for name in f.list(path):
            is_dir = name.endswith("/")
            clean = name[:-1] if is_dir else name
            child = fs.join(path, clean)
            if is_dir:
                t, n = _tree_size(f, child)
                total += t
                nfiles += n
            else:
                total += f.size(child)
                nfiles += 1
        return total, nfiles

    need, nfiles = _tree_size(src, "/")
    need += nfiles * 512          # OC charges a per-file minimum (fileCost)
    free = target.space_total() - target.space_used()
    if free < need:
        tty.print("install: not enough space on " + str(target.address))
        tty.print("  need about " + str(need) + " bytes (" + str(nfiles)
                  + " files incl. per-file overhead), free " + str(free))
        return

    count = _copy_tree(src, target, "/")
    tty.print(str(count) + " files copied.")

    computer.setBootAddress(target.address)
    tty.print("boot address set to " + str(target.address) + ".")

    answer = tty.read("reboot now? [Y/n] ")
    if answer is None or answer.strip() == "" or answer.strip().lower() in ("y", "yes"):
        computer.shutdown(True)
    else:
        tty.print("not rebooting. run 'reboot' when ready.")


main()
