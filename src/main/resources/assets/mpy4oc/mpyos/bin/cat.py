# cat <file...> -- print file contents.
if not args:
    tty.print("usage: cat <file...>")
else:
    for a in args:
        p = sh.resolve(a)
        data = sh.fs.read_file(p)
        if data is None:
            tty.print("cat: no such file: " + p)
        else:
            tty.write(data)
            if data and not data.endswith("\n"):
                tty.write("\n")
