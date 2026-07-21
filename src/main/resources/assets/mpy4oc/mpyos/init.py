# init.py -- the mpyos entry point.
#
# The bios exec()s this in its own global namespace, so component, computer and
# __boot_fs__ are already here. Our job: bind a gpu to a screen, bring up the
# terminal and event system, and hand control to the shell.
#
# The installed system and the LiveCD run this same file; the only difference is
# whether the EEPROM holds a boot address (see bios.py / install.py).

import fs
import event
import term


def _first(kind):
    for addr in component.list(kind):
        return addr
    return None


def _bind_screen(gpu_addr):
    """Bind the gpu to a screen so it can draw, and return a Terminal, or None if
    there is no screen to draw on."""
    gpu = component.proxy(gpu_addr)
    screen_addr = _first("screen")
    if screen_addr is None:
        return None
    try:
        gpu.bind(screen_addr)
    except Exception:
        pass
    return term.Terminal(gpu)


def main():
    boot_fs = None
    try:
        boot_fs = __boot_fs__
    except NameError:
        boot_fs = _first("filesystem")

    gpu_addr = _first("gpu")
    if gpu_addr is None:
        # No display at all. Nothing to interact with; beep and stop.
        try:
            computer.beep(880, 0.3)
        except Exception:
            pass
        return

    tty = _bind_screen(gpu_addr)
    if tty is None:
        try:
            computer.beep(880, 0.3)
        except Exception:
            pass
        return

    tty.clear()
    tty.print_color("MPYOS", term.CYAN, end="")
    tty.print(" version 0.0.36")
    tty.print("mpyos ready.")
    tty.write("boot device: ")
    tty.print_color(str(boot_fs), term.YELLOW)
    tty.print("")

    # If we booted from a read-only medium (the LiveCD floppy), the system isn't
    # installed to a disk yet -- offer install. Once installed, boot is from a
    # writable disk, so we stay quiet. (Same signal OpenOS uses.)
    try:
        booted_ro = fs.Filesystem(boot_fs).is_readonly()
    except Exception:
        booted_ro = False
    if booted_ro:
        tty.write_color("This is a live system. ", term.GREEN)
        tty.write("Type ")
        tty.write_color("install", term.CYAN)
        tty.print(" to copy mpyos onto a hard disk.")
        tty.print("")

    # Plant the filesystem API into the standard `os` module (listdir/mkdir/
    # stat/os.path/...) over a shared Vfs. The shell reuses the same Vfs, so
    # mounts and state stay single-sourced.
    try:
        import os
        import osfs
        osfs.install(os, fs.Vfs(boot_fs))
    except Exception as e:
        tty.print("os install failed: " + str(e))

    # Try to hand off to the shell. If it is not present (a stripped image), fall
    # back to a tiny built-in read-eval loop so the machine is still usable.
    try:
        import shell
        shell.run(tty, boot_fs, globals())
    except Exception as e:
        _fallback_shell(tty, e)


def _fallback_shell(tty, why):
    tty.print("shell unavailable (" + str(why) + "); minimal console.")
    tty.print("type 'exit' to shut down.")
    history = []
    while True:
        line = tty.read("mpy> ", history)
        if line is None:
            continue
        line = line.strip()
        if not line:
            continue
        history.append(line)
        if line == "exit":
            computer.shutdown(False)
            return
        if line == "reboot":
            computer.shutdown(True)
            return
        # Evaluate a Python expression, or run a statement.
        try:
            try:
                result = eval(line, globals())
                if result is not None:
                    tty.print(repr(result))
            except SyntaxError:
                exec(line, globals())
        except Exception as e:
            tty.print("error: " + str(e))


main()
