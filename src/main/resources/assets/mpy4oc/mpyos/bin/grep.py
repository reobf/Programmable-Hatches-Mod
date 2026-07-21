# grep <pattern> <file> -- print lines of <file> containing <pattern>.
import term

if len(args) < 2:
    tty.print("usage: grep <pattern> <file>")
else:
    pattern = args[0]
    path = sh.resolve(args[1])
    data = sh.fs.read_file(path)
    if data is None:
        tty.print("grep: no such file: " + path)
    else:
        num = 0
        for line in data.split("\n"):
            num += 1
            if pattern in line:
                tty.write_color(str(num) + ": ", term.GREEN)
                tty.print(line)
