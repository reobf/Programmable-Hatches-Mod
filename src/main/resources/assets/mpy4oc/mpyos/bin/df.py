# df -- show filesystem space usage for the boot disk and every mount.
import fs
import term


def _row(label, filesystem):
    try:
        total = filesystem.space_total()
        used = filesystem.space_used()
    except Exception:
        tty.print(label + "  (unavailable)")
        return
    free = total - used
    pct = (used * 100 // total) if total else 0
    tty.write_color(label, term.CYAN)
    tty.print("  used " + str(used) + "  free " + str(free) +
              "  total " + str(total) + "  (" + str(pct) + "%)")


# boot disk
_row("/", fs.Filesystem(sh.fs.address))
# mounts
sh.fs.refresh()
for name in sorted(sh.fs.mounts.keys()):
    _row("/mnt/" + name, fs.Filesystem(sh.fs.mounts[name]))
