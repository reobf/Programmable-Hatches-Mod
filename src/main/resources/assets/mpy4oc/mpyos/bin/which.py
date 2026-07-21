# which <name> -- show the path of a shell command, or that it's a builtin.
if not args:
    tty.print("usage: which <name>")
else:
    name = args[0]
    path = "/bin/" + name + ".py"
    if sh.fs.exists(path):
        tty.print(path)
    else:
        tty.print(name + ": not found")
