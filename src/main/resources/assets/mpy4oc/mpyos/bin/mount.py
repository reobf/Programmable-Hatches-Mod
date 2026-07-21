# mount -- list auto-mounted filesystems.
#
# mpyos mounts every filesystem component (except the boot disk and the tmpfs)
# under /mnt/<address-prefix>, OpenOS style. This shows what is mounted where.

sh.fs.refresh()
mounts = sh.fs.mounts
if not mounts:
    tty.print("no filesystems mounted under /mnt")
else:
    for name in sorted(mounts.keys()):
        tty.print("/mnt/" + name + "  ->  " + mounts[name])
