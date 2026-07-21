# free -- show the sandbox's memory usage (a rough estimate, like OpenOS).
import term

total = computer.totalMemory()
free = computer.freeMemory()
used = total - free
pct = (used * 100 // total) if total else 0


def _kb(n):
    return str(int(n) // 1024) + "K"


tty.write_color("memory  ", term.CYAN)
tty.print("used " + _kb(used) + "  free " + _kb(free) +
          "  total " + _kb(total) + "  (" + str(pct) + "%)")
