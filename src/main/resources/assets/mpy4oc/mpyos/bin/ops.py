# ops -- show the CPU's instruction budget for this tick.
#
# mpyos runs a bounded number of bytecode ops per tick, set by the CPU tier.
# Unused ops bank up (to 10x the per-tick rate), so yielding when idle buys you
# longer bursts later. This shows what's left right now.
import term

remaining = computer.ops()
share = computer.ops_share()
tty.write_color("ops  ", term.CYAN)
tty.print("remaining this tick: " + str(int(remaining)))
if abs(share - remaining) > 1:
    tty.write_color("     ", term.CYAN)
    tty.print("this coroutine's share: " + str(int(share)))
