"""shell.py -- the mpyos command shell.

Reads a line, resolves the command against /bin, and runs it. Commands are plain
Python files: /bin/<name>.py, exec'd with a small context (args, tty, sh). The
shell keeps the working directory and offers the few things that must be
built in because they mutate shell state (cd) or end the session (exit/reboot).

init.py calls run(tty, boot_fs).
"""

import fs


class Shell:
    def __init__(self, tty, boot_fs_addr, main_globals=None):
        self.tty = tty
        self.boot_addr = boot_fs_addr
        # Reuse the Vfs installed into `os` at boot (single mount state); build
        # our own only on a stripped image where osfs was not installed.
        try:
            import osfs
            self.fs = osfs.shared_vfs() or fs.Vfs(boot_fs_addr)
        except Exception:
            self.fs = fs.Vfs(boot_fs_addr)
        self.cwd = "/"
        self.history = []
        self.running = True
        # Commands' inner functions resolve globals against the MAIN module (a VM
        # scoping rule -- see run_command), so the context must be planted there.
        # init.py passes its globals() in; they are the main namespace.
        self.g = main_globals if main_globals is not None else globals()

    # -- paths --
    def resolve(self, path):
        """Absolute path stays; relative resolves against the working directory."""
        if not path:
            return self.cwd
        if path.startswith("/"):
            return fs.normalize(path)
        return fs.join(self.cwd, path)

    # -- command lookup --
    def find_command(self, name):
        """The source of /bin/<name>.py on the boot disk, or None."""
        cand = "/bin/" + name + ".py"
        if self.fs.exists(cand):
            return self.fs.read_file(cand)
        return None

    # -- execution --
    def run_command(self, name, args):
        # State-mutating and session commands live in the shell itself.
        if name == "cd":
            return self._cd(args)
        if name == "exit" or name == "shutdown":
            self.running = False
            computer.shutdown(False)
            return
        if name == "reboot":
            self.running = False
            computer.shutdown(True)
            return
        if name == "help":
            self.tty.print("built-in: cd exit reboot help; others come from /bin")
            return

        src = self.find_command(name)
        if src is None:
            self.tty.print(name + ": command not found")
            return

        # Each command runs in its own sandbox namespace: args/tty/sh are injected
        # there, functions the command defines keep that namespace as their scope
        # (so they see args/tty/sh too), and reads of component/computer/the libs
        # fall through to the system globals. Nothing the command defines leaks
        # back into the shell, so no save/restore of main globals is needed.
        ns = {"args": args, "tty": self.tty, "sh": self}
        try:
            exec_sandbox(src, ns)
        except SystemExit:
            pass
        except Exception as e:
            self.tty.print(name + ": " + str(e))

    def _cd(self, args):
        target = self.resolve(args[0]) if args else "/"
        if not self.fs.exists(target) or not self.fs.is_directory(target):
            self.tty.print("cd: no such directory: " + target)
            return
        self.cwd = target
        # keep os.getcwd() in step with the shell (shell is authoritative)
        try:
            import osfs
            osfs.chdir(target)
        except Exception:
            pass

    # -- main loop --
    def loop(self):
        while self.running:
            # Re-scan mounts so a disk inserted while the shell is running shows up
            # under /mnt (and a removed one disappears), the way OpenOS does.
            self.fs.refresh()
            # Colour the prompt: cwd in cyan, the '#' in green. Written directly so
            # read() starts capturing right after it (read tracks the cursor).
            import term as _term
            self.tty.write_color(self.cwd, _term.CYAN)
            self.tty.write_color(" # ", _term.GREEN)
            line = self.tty.read(None, self.history)
            if line is None:
                continue
            line = line.strip()
            if not line:
                continue
            self.history.append(line)
            parts = _split(line)
            if not parts:
                continue
            self.run_command(parts[0], parts[1:])


def _split(line):
    """Split a command line into words. Double and single quotes group words;
    no escapes, no globbing -- a shell for a floppy, not bash."""
    out = []
    cur = ""
    quote = None
    for ch in line:
        if quote is not None:
            if ch == quote:
                quote = None
            else:
                cur += ch
        elif ch == '"' or ch == "'":
            quote = ch
        elif ch == " " or ch == "\t":
            if cur:
                out.append(cur)
                cur = ""
        else:
            cur += ch
    if cur:
        out.append(cur)
    return out


def run(tty, boot_fs_addr, main_globals=None):
    sh = Shell(tty, boot_fs_addr, main_globals)
    sh.loop()
