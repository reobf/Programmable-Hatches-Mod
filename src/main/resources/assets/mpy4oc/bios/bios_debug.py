# 诊断版 bios: 每步 print, 输出会出现在服务器日志的 [mpy] ... 行
print("bios: start")

def _read_file(fs, path):
    try:
        h = component.invoke(fs, "open", path)
    except Exception as e:
        print("bios: open failed:", str(e))
        return None
    if not h:
        return None
    try:
        chunks = []
        while True:
            chunk = component.invoke(fs, "read", h, 4096)
            if chunk is None:
                break
            if isinstance(chunk, bytes):
                chunk = chunk.decode("utf-8")
            chunks.append(chunk)
        return "".join(chunks)
    finally:
        try:
            component.invoke(fs, "close", h)
        except Exception:
            pass

print("bios: listing all components")
for a in component.list():
    print("  comp:", a, "=", component.type(a))

print("bios: listing filesystems")
fs_list = []
for a in component.list("filesystem"):
    fs_list.append(a)
    print("  fs:", a)
print("bios: filesystem count =", len(fs_list))

print("bios: boot address =", computer.getBootAddress())

boot_fs = None
for a in fs_list:
    try:
        ex = component.invoke(a, "exists", "/init.py")
        print("  ", a, "has /init.py:", ex)
        if ex:
            boot_fs = a
            break
    except Exception as e:
        print("  exists() error on", a, ":", str(e))

if boot_fs is None:
    print("bios: NO bootable fs found")
    try:
        computer.beep(1000, 0.2)
    except Exception as e:
        print("bios: beep failed:", str(e))
else:
    print("bios: booting from", boot_fs)
    src = _read_file(boot_fs, "/init.py")
    if src is None:
        print("bios: could not read /init.py")
    else:
        print("bios: /init.py length =", len(src))
        g = globals()
        g["__boot_fs__"] = boot_fs
        print("bios: exec init.py now")
        exec(src, g)
        print("bios: init.py returned (this is normal if init has its own loop)")
