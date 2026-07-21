# uptime -- how long the computer has been running.
secs = computer.uptime()
h = int(secs // 3600)
m = int((secs % 3600) // 60)
s = int(secs % 60)
tty.print("up " + str(h) + "h " + str(m) + "m " + str(s) + "s")
