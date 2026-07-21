# python -- run a Python file, or start an interactive session.
#
#   python <file> [args...]   run the file; its own args are in the `argv` list
#   python                    interactive REPL: eval prints the value, else exec
#
# Both run in a fresh sandbox namespace: reads fall through to the system globals
# (component / computer / fs / the mpyos libs are all directly usable), but writes
# stay in the sandbox and never touch the system namespace. Functions defined here
# keep the sandbox as their scope. The sandbox is discarded on exit.
#
# REPL extras: up/down history, Tab completion (names from the sandbox, the system
# globals and builtins; after a dot, attributes of the resolved object via dir()).

if args:
    # --- run a file ---
    path = sh.resolve(args[0])
    src = sh.fs.read_file(path)
    if src is None:
        tty.print("python: no such file: " + path)
    else:
        sandbox = {"argv": [path] + list(args[1:])}
        try:
            exec_sandbox(src, sandbox)
        except SystemExit:
            pass
        except Exception as _e:
            tty.print("python: " + str(_e))
else:
    # --- interactive REPL ---
    tty.print("Python (mpyos). 'exit' to leave.")
    sandbox = {}
    history = []

    def _ident_start(text, end):
        # walk back over [A-Za-z0-9_.] to find the start of the dotted token
        i = end
        while i > 0:
            c = text[i - 1]
            if c.isalpha() or c.isdigit() or c == "_" or c == ".":
                i -= 1
            else:
                break
        return i

    def _resolve(name):
        # Resolve exactly the way execution would: evaluate the dotted path in the
        # sandbox (falls through to the system globals). Membership tests against
        # sh.g don't work for injected globals like component/computer -- they live
        # in the VM globals, reachable only via name-lookup fall-through.
        try:
            return eval_sandbox(name, sandbox)
        except Exception:
            return None

    def _complete(text, pos):
        start = _ident_start(text, pos)
        token = text[start:pos]
        dot = token.rfind(".")
        names = []
        if dot < 0:
            # bare name: sandbox + system globals (builtins live there too)
            partial = token
            seen = []
            for k in list(sandbox.keys()):
                if k.startswith(partial) and k not in seen:
                    seen.append(k)
            for k in list(sh.g.keys()):
                if k.startswith(partial) and k not in seen:
                    seen.append(k)
            # injected globals and common builtins are not literal keys of sh.g
            # (they live in the VM globals, reached via fall-through), so offer
            # them explicitly
            for k in ("component", "computer", "print", "len", "dir", "range",
                      "repr", "type", "str", "int", "float", "list", "dict",
                      "set", "tuple", "getattr", "setattr", "hasattr", "sorted",
                      "sum", "min", "max", "abs", "enumerate", "isinstance"):
                if k.startswith(partial) and k not in seen:
                    seen.append(k)
            names = seen
            base_len = 0
        else:
            base = token[:dot]
            partial = token[dot + 1:]
            obj = _resolve(base)
            if obj is None:
                return []
            try:
                attrs = [str(x) for x in dir(obj)]
            except Exception:
                return []
            # the component module also completes to attached component types
            # (component.gpu / component.filesystem ... resolve as primaries)
            if base == "component":
                try:
                    for _t in component.list().values():
                        if _t not in attrs:
                            attrs.append(_t)
                except Exception:
                    pass
            seen = []
            for k in attrs:
                k = str(k)
                if k.startswith(partial) and not k.startswith("__") and k not in seen:
                    seen.append(k)
            names = seen
            base_len = dot + 1
        if not names:
            return []
        out = []
        for n in names:
            new_text = text[:start] + token[:base_len] + n + text[pos:]
            new_pos = start + base_len + len(n)
            out.append((new_text, new_pos, n))
        return out

    while True:
        line = tty.read(">>> ", history, _complete)
        if line is None:
            break
        line = line.strip()
        if not line:
            continue
        if line == "exit" or line == "exit()":
            break
        history.append(line)
        try:
            try:
                _r = eval_sandbox(line, sandbox)
                if _r is not None:
                    tty.print(repr(_r))
            except SyntaxError:
                exec_sandbox(line, sandbox)
        except Exception as _e:
            tty.print("error: " + str(_e))
