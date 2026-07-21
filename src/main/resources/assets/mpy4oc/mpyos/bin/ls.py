# ls [path] -- list a directory. Directories are shown with a trailing / and in
# colour (cyan for directories, default for files).
import term

target = sh.resolve(args[0]) if args else sh.cwd
if not sh.fs.exists(target):
    tty.print("ls: no such path: " + target)
elif not sh.fs.is_directory(target):
    tty.print(target)
else:
    names = sh.fs.list(target)
    names.sort()
    first = True
    for name in names:
        if not first:
            tty.write("  ")
        first = False
        if name.endswith("/"):
            tty.write_color(name, term.CYAN)
        else:
            tty.write(name)
    if names:
        tty.print("")
