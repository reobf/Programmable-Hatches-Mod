"""term.py -- the mpyos terminal.

Wraps the GPU component into a scrolling text terminal: write text, move the
cursor, clear, scroll, and read a line of input from the keyboard. Corresponds to
the term + tty layer of OpenOS, kept small.

The GPU addresses the screen 1-indexed: the top-left cell is (1, 1). We track the
cursor ourselves and scroll by copying the screen up a row and clearing the last.
Input comes in as key_down / clipboard signals via event.pull.
"""

import event


# LWJGL scancodes we care about for line editing (see OpenOS lib/keyboard.lua).
KEY_BACK = 0x0E
KEY_TAB = 0x0F
KEY_ENTER = 0x1C
KEY_LEFT = 0xCB
KEY_RIGHT = 0xCD
KEY_UP = 0xC8
KEY_DOWN = 0xD0
KEY_HOME = 0xC7
KEY_END = 0xCF
KEY_DELETE = 0xD3

# Named colours (RGB), roughly the OpenComputers tier-3 palette. Handy for
# tty.write_color(text, term.CYAN) and friends.
WHITE = 0xFFFFFF
SILVER = 0xC0C0C0
GRAY = 0x808080
BLACK = 0x000000
RED = 0xFF4040
GREEN = 0x40FF40
YELLOW = 0xFFFF40
BLUE = 0x4040FF
CYAN = 0x40FFFF
MAGENTA = 0xFF40FF
ORANGE = 0xFFA000
GREENDARK = 0x00A000


class Terminal:
    def __init__(self, gpu):
        """gpu is a component proxy (component.proxy(address))."""
        self.gpu = gpu
        self.x = 1
        self.y = 1
        # The current foreground colour we set, tracked here rather than read back
        # from the GPU: gpu.getForeground() returns TWO values (colour, from_palette)
        # and the palette case returns an index, not an RGB -- feeding that back into
        # setForeground (which wants an integer) throws "integer expected, got table".
        # We default to white and update it whenever we set a colour.
        self.fg = 0xFFFFFF
        try:
            self.w, self.h = gpu.getResolution()
        except Exception:
            self.w, self.h = 80, 25

    # -- geometry --
    def size(self):
        return self.w, self.h

    def refresh_size(self):
        self.w, self.h = self.gpu.getResolution()

    def set_cursor(self, x, y):
        self.x = max(1, min(x, self.w))
        self.y = max(1, min(y, self.h))

    def get_cursor(self):
        return self.x, self.y

    # -- screen ops --
    def clear(self):
        self.gpu.fill(1, 1, self.w, self.h, " ")
        self.x = 1
        self.y = 1

    def clear_line(self, y=None):
        if y is None:
            y = self.y
        self.gpu.fill(1, y, self.w, 1, " ")

    def scroll(self, lines=1):
        """Scroll the screen up `lines` rows, clearing the freed rows at the bottom.
        This is a GPU copy of the region up by `lines`, then a fill of the tail --
        the same primitive OpenOS uses."""
        if lines <= 0:
            return
        if lines >= self.h:
            self.gpu.fill(1, 1, self.w, self.h, " ")
            return
        # copy rows [lines+1 .. h] up to row 1
        self.gpu.copy(1, lines + 1, self.w, self.h - lines, 0, -lines)
        # clear the newly exposed rows at the bottom
        self.gpu.fill(1, self.h - lines + 1, self.w, lines, " ")

    def _newline(self):
        self.x = 1
        if self.y >= self.h:
            self.scroll(1)
            self.y = self.h
        else:
            self.y += 1

    # -- writing --
    def write(self, text):
        """Write text at the cursor, wrapping at the right edge and scrolling at the
        bottom. Handles \\n and \\r; \\t expands to the next 4-column stop."""
        for ch in text:
            if ch == "\n":
                self._newline()
                continue
            if ch == "\r":
                self.x = 1
                continue
            if ch == "\t":
                spaces = 4 - ((self.x - 1) % 4)
                self.write(" " * spaces)
                continue
            if ch == "\b":
                if self.x > 1:
                    self.x -= 1
                continue
            # printable
            if self.x > self.w:
                self._newline()
            try:
                self.gpu.set(self.x, self.y, ch)
            except Exception:
                pass
            self.x += 1

    def print(self, *parts, **kw):
        sep = kw.get("sep", " ")
        end = kw.get("end", "\n")
        self.write(sep.join([str(p) for p in parts]) + end)

    # -- colors --
    def set_foreground(self, rgb):
        self.fg = rgb
        self.gpu.setForeground(rgb)

    def set_background(self, rgb):
        self.gpu.setBackground(rgb)

    def write_color(self, text, rgb):
        """Write text in a colour, then restore the previous foreground so the
        colouring is local to this call. The previous colour is our tracked value,
        not gpu.getForeground() (which returns a (colour, from_palette) pair that
        can't be fed straight back into setForeground)."""
        prev = self.fg
        self.set_foreground(rgb)
        self.write(text)
        self.set_foreground(prev)

    def print_color(self, text, rgb, end="\n"):
        self.write_color(text + end, rgb)

    # -- input --
    def read(self, prompt=None, history=None, complete=None):
        """Read one line of input, echoing as the user types. Supports backspace,
        left/right, home/end, up/down history, and Tab completion. Returns the line
        without the trailing newline, or None on interrupt (Ctrl+D / Ctrl+C on
        empty).

        complete(text, pos) -- optional Tab callback: given the current line and
        cursor index, return a list of full-line candidates, each a (new_text,
        new_pos) tuple; empty list = no completion. One candidate: applied. Many:
        the candidates' common prefix is applied and names are listed below."""
        if prompt:
            self.write(prompt)

        buf = []          # list of characters
        pos = 0           # cursor index within buf
        start_x = self.x  # column where input begins on the current row
        start_y = self.y
        hist = list(history) if history else []
        hist_index = len(hist)   # points past the end

        def redraw():
            # Repaint the line from start to end, clearing leftovers.
            self.gpu.fill(start_x, start_y, self.w - start_x + 1, 1, " ")
            self.gpu.set(start_x, start_y, "".join(buf))
            self.set_cursor(start_x + pos, start_y)

        # --- blinking cursor ---------------------------------------------------
        # OC screens have no hardware cursor, so we fake one the way OpenOS does
        # (see openos core/cursor.lua): every BLINK seconds read the cell under the
        # cursor with gpu.get -> its character and its own fg/bg -> redraw it in
        # inverse video (fg/bg swapped), then plain again. event.pull waits with a
        # timeout so it wakes to toggle; a real keystroke still returns at once.
        BLINK = 0.5
        cursor_on = [False]

        def cursor_x():
            return start_x + pos

        def draw_cursor(on):
            cx = cursor_x()
            try:
                cell = self.gpu.get(cx, start_y)   # (char, fg, bg, ...)
                ch = cell[0] if cell else " "
                fg = cell[1] if cell and len(cell) > 1 else None
                bg = cell[2] if cell and len(cell) > 2 else None
            except Exception:
                ch, fg, bg = " ", None, None
            if on and fg is not None and bg is not None:
                # inverse video for one cell: draw char with fg/bg swapped
                self.gpu.setForeground(bg)
                self.gpu.setBackground(fg)
                self.gpu.set(cx, start_y, ch)
                self.gpu.setForeground(fg)
                self.gpu.setBackground(bg)
            else:
                # off (or colours unknown): the plain character
                self.gpu.set(cx, start_y, ch if ch else " ")
            cursor_on[0] = on

        def erase_cursor():
            # Restore the plain cell before editing/echoing.
            if cursor_on[0]:
                draw_cursor(False)

        while True:
            sig = event.pull(BLINK)
            if sig is None:
                # timeout: toggle the cursor and keep waiting
                draw_cursor(not cursor_on[0])
                continue
            erase_cursor()
            name = sig[0]

            if name == "clipboard":
                # ("clipboard", keyboardAddress, text) on real OC.
                paste = ""
                if len(sig) >= 3:
                    paste = sig[2]
                elif len(sig) > 1:
                    paste = sig[1]
                # A paste stops at the first newline (that submits the line).
                nl = paste.find("\n")
                if nl >= 0:
                    text = paste[:nl]
                else:
                    text = paste
                for ch in text:
                    buf.insert(pos, ch)
                    pos += 1
                redraw()
                if nl >= 0:
                    break
                continue

            if name != "key_down":
                continue

            # Real OC key_down signals carry the keyboard address first:
            #   ("key_down", keyboardAddress, char, code)
            # (Keyboard.signal sends char/code, and sendToReachable prepends the
            # source node's address.) So char/code start at index 2. Be tolerant of
            # a 3-tuple form too, in case a signal was pushed without an address.
            if len(sig) >= 4:
                ch = sig[2]
                code = sig[3]
            else:
                ch = sig[1] if len(sig) > 1 else 0
                code = sig[2] if len(sig) > 2 else 0
            ch = int(ch) if ch else 0

            if code == KEY_ENTER:
                break
            elif code == KEY_BACK:
                if pos > 0:
                    # NB: del buf[i] corrupts the list in this VM (leaves a None and
                    # keeps the length); rebuild via slicing instead. Same below.
                    buf = buf[:pos - 1] + buf[pos:]
                    pos -= 1
                    redraw()
            elif code == KEY_DELETE:
                if pos < len(buf):
                    buf = buf[:pos] + buf[pos + 1:]
                    redraw()
            elif code == KEY_LEFT:
                if pos > 0:
                    pos -= 1
                    self.set_cursor(start_x + pos, start_y)
            elif code == KEY_RIGHT:
                if pos < len(buf):
                    pos += 1
                    self.set_cursor(start_x + pos, start_y)
            elif code == KEY_HOME:
                pos = 0
                self.set_cursor(start_x, start_y)
            elif code == KEY_END:
                pos = len(buf)
                self.set_cursor(start_x + pos, start_y)
            elif code == KEY_UP:
                if hist and hist_index > 0:
                    hist_index -= 1
                    buf = list(hist[hist_index])
                    pos = len(buf)
                    redraw()
            elif code == KEY_DOWN:
                if hist and hist_index < len(hist) - 1:
                    hist_index += 1
                    buf = list(hist[hist_index])
                    pos = len(buf)
                    redraw()
                elif hist_index >= len(hist) - 1:
                    hist_index = len(hist)
                    buf = []
                    pos = 0
                    redraw()
            elif ch == 9:          # Tab: completion via the callback
                if complete:
                    try:
                        cands = complete("".join(buf), pos)
                    except Exception:
                        cands = []
                    if cands:
                        if len(cands) == 1:
                            c0 = cands[0]
                            buf = list(c0[0])
                            pos = c0[1]
                            redraw()
                        else:
                            # apply the common prefix of all candidates, then list
                            # them below and re-draw the prompt + line
                            texts = [c[0] for c in cands]
                            common = texts[0]
                            for t in texts[1:]:
                                n = 0
                                lim = len(common) if len(common) < len(t) else len(t)
                                while n < lim and common[n] == t[n]:
                                    n += 1
                                common = common[:n]
                            shown = []
                            for c in cands:
                                w = c[2] if len(c) > 2 else c[0]
                                shown.append(w)
                            self._newline()
                            self.write("  ".join(shown))
                            self._newline()
                            if len(common) > len(buf):
                                buf = list(common)
                                pos = len(buf)
                            # re-print the prompt + current line on the new row
                            if prompt:
                                self.write(prompt)
                            start_x = self.x
                            start_y = self.y
                            redraw()
            elif ch == 3:          # Ctrl+C
                self.write("^C\n")
                return None
            elif ch == 4:          # Ctrl+D
                if not buf:
                    return None
            elif ch >= 32:         # printable
                buf.insert(pos, chr(ch))
                pos += 1
                redraw()

        # move to the next line after the input
        self.set_cursor(start_x + len(buf), start_y)
        self._newline()
        return "".join(buf)
