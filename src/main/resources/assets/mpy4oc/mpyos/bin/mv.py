# mv <src> <dst> -- move/rename within the filesystem.
import fs as _fs
if len(args) != 2:
    tty.print("usage: mv <src> <dst>")
else:
    src = sh.resolve(args[0])
    dst = sh.resolve(args[1])
    if not sh.fs.exists(src):
        tty.print("mv: no such path: " + src)
    else:
        if sh.fs.exists(dst) and sh.fs.is_directory(dst):
            dst = _fs.join(dst, _fs.basename(src))
        if not sh.fs.rename(src, dst):
            tty.print("mv: cannot move " + src + " -> " + dst)
