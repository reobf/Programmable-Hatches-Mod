# rm <path...> -- remove files or directories (recursive, like OC's remove).
if not args:
    tty.print("usage: rm <path...>")
else:
    for a in args:
        p = sh.resolve(a)
        if not sh.fs.exists(p):
            tty.print("rm: no such path: " + p)
        elif not sh.fs.remove(p):
            tty.print("rm: cannot remove " + p)
