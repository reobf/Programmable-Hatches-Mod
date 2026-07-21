# mkdir <dir...>
if not args:
    tty.print("usage: mkdir <dir...>")
else:
    for a in args:
        p = sh.resolve(a)
        if not sh.fs.make_directory(p):
            tty.print("mkdir: cannot create " + p)
