# tree [path] -- print a directory tree.
import term

root = sh.resolve(args[0]) if args else sh.cwd


def walk(path, prefix):
    try:
        names = sh.fs.list(path)
    except Exception:
        return
    names.sort()
    n = len(names)
    for i in range(n):
        name = names[i]
        is_dir = name.endswith("/")
        clean = name[:-1] if is_dir else name
        last = (i == n - 1)
        branch = "'-- " if last else "|-- "
        tty.write(prefix + branch)
        if is_dir:
            tty.print_color(clean, term.CYAN)
            child = path + ("" if path.endswith("/") else "/") + clean
            walk(child, prefix + ("    " if last else "|   "))
        else:
            tty.print(clean)


if not sh.fs.exists(root):
    tty.print("tree: no such path: " + root)
else:
    tty.print_color(root, term.CYAN)
    walk(root, "")
