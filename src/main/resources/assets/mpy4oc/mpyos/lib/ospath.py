"""ospath.py -- the os.path namespace for mpyos.

Installed as `os.path` by osfs.install(). Pure path-string helpers plus the
filesystem queries (exists/isdir/...), which route through osfs's shared Vfs.
"""

import fs
import osfs

sep = "/"


def normpath(path):
    return fs.normalize(path)


def join(*parts):
    """POSIX join: a later absolute part discards everything before it."""
    out = ""
    for p in parts:
        if not p:
            continue
        if p.startswith("/"):
            out = p
        elif out:
            out = out + ("" if out.endswith("/") else "/") + p
        else:
            out = p
    return out


def dirname(path):
    return fs.dirname(path)


def basename(path):
    return fs.basename(path)


def split(path):
    return (fs.dirname(path), fs.basename(path))


def splitext(path):
    name = fs.basename(path)
    dot = name.rfind(".")
    if dot <= 0:              # no dot, or a leading-dot name like ".profile"
        return (path, "")
    head = path[: len(path) - (len(name) - dot)]
    return (head, name[dot:])


def abspath(path):
    return osfs._abs(path)


def isabs(path):
    return bool(path) and path.startswith("/")


def exists(path):
    return osfs._v().exists(osfs._abs(path))


def isdir(path):
    return osfs._v().is_directory(osfs._abs(path))


def isfile(path):
    p = osfs._abs(path)
    v = osfs._v()
    return v.exists(p) and not v.is_directory(p)


def getsize(path):
    p = osfs._abs(path)
    v = osfs._v()
    if not v.exists(p):
        raise OSError("getsize: no such file: " + p)
    return v.size(p) or 0
