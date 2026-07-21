# /bin/edit.py -- a small full-screen editor. edit <file>
#
# Arrows move, typing inserts, backspace/delete remove, enter splits the line.
# Ctrl+S saves, Ctrl+Q quits. The last screen row is a status bar. Enough to
# maintain init.py and write new /bin commands from inside the machine.
#
# Keyboard comes straight from event signals (not tty.read -- that is a line
# reader); the screen is driven through the Terminal's gpu.

import event
import term as _term

K = _term  # scancode constants live in term


def main():
    if not args:
        tty.print("usage: edit <file>")
        return

    path = sh.resolve(args[0])
    gpu = tty.gpu
    w, h = tty.size()
    text_h = h - 1          # last row is the status bar

    # -- load --
    data = sh.fs.read_file(path)
    if data is None:
        lines = [""]
        existed = False
    else:
        lines = data.split("\n")
        if not lines:
            lines = [""]
        existed = True

    cx = 0      # cursor column in the document, 0-based
    cy = 0      # cursor line in the document, 0-based
    top = 0     # first visible document line
    modified = False
    status_msg = ("" if existed else "(new file) ") + "^S save  ^Q quit"

    # -- drawing --
    def draw_line(i):
        """Repaint document line i if visible."""
        row = i - top
        if row < 0 or row >= text_h:
            return
        gpu.fill(1, row + 1, w, 1, " ")
        s = lines[i] if i < len(lines) else ""
        if s:
            gpu.set(1, row + 1, s[:w])

    def draw_all():
        for r in range(text_h):
            draw_line(top + r)

    def draw_status():
        gpu.fill(1, h, w, 1, " ")
        left = path + (" *" if modified else "")
        right = str(cy + 1) + "," + str(cx + 1) + "  " + status_msg
        gpu.set(1, h, left[:w])
        if len(right) < w:
            gpu.set(w - len(right) + 1, h, right)

    def place_cursor():
        tty.set_cursor(min(cx, w - 1) + 1, (cy - top) + 1)

    def clamp_scroll():
        """Keep the cursor line on screen; returns True if we scrolled."""
        nonlocal_top = top
        if cy < nonlocal_top:
            return cy
        if cy >= nonlocal_top + text_h:
            return cy - text_h + 1
        return nonlocal_top

    tty.clear()
    draw_all()
    draw_status()
    place_cursor()

    while True:
        sig = event.pull()
        if sig is None:
            continue
        name = sig[0]

        if name == "clipboard":
            # ("clipboard", keyboardAddress, text) on real OC.
            paste = sig[2] if len(sig) >= 3 else (sig[1] if len(sig) > 1 else "")
            for ch in paste:
                if ch == "\n":
                    rest = lines[cy][cx:]
                    lines[cy] = lines[cy][:cx]
                    lines.insert(cy + 1, rest)
                    cy += 1
                    cx = 0
                else:
                    lines[cy] = lines[cy][:cx] + ch + lines[cy][cx:]
                    cx += 1
            modified = True
            new_top = clamp_scroll()
            if new_top != top:
                top = new_top
            draw_all()
            draw_status()
            place_cursor()
            continue

        if name != "key_down":
            continue

        # ("key_down", keyboardAddress, char, code) on real OC.
        if len(sig) >= 4:
            _ch, code = sig[2], sig[3]
        else:
            _ch, code = (sig[1] if len(sig) > 1 else 0), (sig[2] if len(sig) > 2 else 0)
        ch = int(_ch) if _ch else 0

        structural = False   # needs a full redraw (split/join/scroll)

        if ch == 17:                       # Ctrl+Q
            break
        elif ch == 19:                     # Ctrl+S
            try:
                sh.fs.write_file(path, "\n".join(lines))
                modified = False
                status_msg = "saved. ^S save  ^Q quit"
            except Exception as e:
                status_msg = "save failed: " + str(e)
            draw_status()
            place_cursor()
            continue
        elif code == K.KEY_ENTER:
            rest = lines[cy][cx:]
            lines[cy] = lines[cy][:cx]
            lines.insert(cy + 1, rest)
            cy += 1
            cx = 0
            modified = True
            structural = True
        elif code == K.KEY_BACK:
            if cx > 0:
                lines[cy] = lines[cy][:cx - 1] + lines[cy][cx:]
                cx -= 1
                modified = True
                draw_line(cy)
            elif cy > 0:
                cx = len(lines[cy - 1])
                lines[cy - 1] = lines[cy - 1] + lines[cy]
                # del lines[i] corrupts the list in this VM; splice instead.
                lines = lines[:cy] + lines[cy + 1:]
                cy -= 1
                modified = True
                structural = True
        elif code == K.KEY_DELETE:
            if cx < len(lines[cy]):
                lines[cy] = lines[cy][:cx] + lines[cy][cx + 1:]
                modified = True
                draw_line(cy)
            elif cy + 1 < len(lines):
                lines[cy] = lines[cy] + lines[cy + 1]
                lines = lines[:cy + 1] + lines[cy + 2:]
                modified = True
                structural = True
        elif code == K.KEY_LEFT:
            if cx > 0:
                cx -= 1
            elif cy > 0:
                cy -= 1
                cx = len(lines[cy])
        elif code == K.KEY_RIGHT:
            if cx < len(lines[cy]):
                cx += 1
            elif cy + 1 < len(lines):
                cy += 1
                cx = 0
        elif code == K.KEY_UP:
            if cy > 0:
                cy -= 1
                cx = min(cx, len(lines[cy]))
        elif code == K.KEY_DOWN:
            if cy + 1 < len(lines):
                cy += 1
                cx = min(cx, len(lines[cy]))
        elif code == K.KEY_HOME:
            cx = 0
        elif code == K.KEY_END:
            cx = len(lines[cy])
        elif ch >= 32:                     # printable
            lines[cy] = lines[cy][:cx] + chr(ch) + lines[cy][cx:]
            cx += 1
            modified = True
            draw_line(cy)

        new_top = clamp_scroll()
        if new_top != top:
            top = new_top
            structural = True
        if structural:
            draw_all()
        draw_status()
        place_cursor()

    # leave the editor: clear and let the shell repaint its prompt
    tty.clear()


main()
