# components [filter...] [-l] -- list attached components (address and type).
#
# With no filter, lists everything; a filter shows only components whose type
# starts with it (e.g. `components file` -> filesystems). -l also lists each
# component's methods (with docs where available), like OpenOS.
import term

long = "-l" in args
filters = [a for a in args if a != "-l"]
if not filters:
    filters = [""]

# collect: address -> type, honoring the filters
found = {}
listing = component.list()          # {address: type}
for addr in listing:
    typ = listing[addr]
    for f in filters:
        if typ.startswith(f):
            found[addr] = typ
            break

if not found:
    tty.print("no matching components")
else:
    # width for aligning the type column
    width = 1
    for addr in found:
        if len(found[addr]) > width:
            width = len(found[addr])
    width += 2
    for addr in sorted(found.keys(), key=lambda a: found[a]):
        typ = found[addr]
        tty.write_color(typ + " " * (width - len(typ)), term.CYAN)
        tty.print(addr)
        if long:
            try:
                methods = sorted(component.methods(addr).keys())
            except Exception:
                methods = []
            for mname in methods:
                doc = component.doc(addr, mname)
                line = "  " + mname
                if doc:
                    line = line + " -- " + str(doc)
                tty.print(line)
