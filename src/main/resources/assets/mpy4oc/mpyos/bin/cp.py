# cp <src> <dst> -- copy one file. If dst is a directory, copy into it.
import fs as _fs
if len(args) != 2:
    tty.print("usage: cp <src> <dst>")
else:
    src = sh.resolve(args[0])
    dst = sh.resolve(args[1])
    data = sh.fs.read_bytes(src)
    if data is None:
        tty.print("cp: no such file: " + src)
    else:
        if sh.fs.exists(dst) and sh.fs.is_directory(dst):
            dst = _fs.join(dst, _fs.basename(src))
        sh.fs.write_file(dst, data)
