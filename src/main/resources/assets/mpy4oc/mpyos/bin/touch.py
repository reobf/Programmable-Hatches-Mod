# touch <file...> -- create empty files (no-op if they exist).
if not args:
    tty.print("usage: touch <file...>")
else:
    for a in args:
        p = sh.resolve(a)
        if not sh.fs.exists(p):
            sh.fs.write_file(p, "")
