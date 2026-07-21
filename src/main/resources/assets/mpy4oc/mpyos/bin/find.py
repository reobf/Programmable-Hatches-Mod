# find [path] [name] -- recursively list paths; if `name` is given, only paths
# whose basename contains it.
root = sh.resolve(args[0]) if args else sh.cwd
needle = args[1] if len(args) > 1 else None


def walk(path):
    try:
        names = sh.fs.list(path)
    except Exception:
        return
    names.sort()
    for name in names:
        is_dir = name.endswith("/")
        clean = name[:-1] if is_dir else name
        child = path + ("" if path.endswith("/") else "/") + clean
        if needle is None or needle in clean:
            tty.print(child + ("/" if is_dir else ""))
        if is_dir:
            walk(child)


if not sh.fs.exists(root):
    tty.print("find: no such path: " + root)
else:
    walk(root)
