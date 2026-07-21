"""event.py -- the event loop for mpyos, built on computer.pullSignal.

OpenComputers delivers everything -- key presses, component hotplug, timers, a
signal another program pushed -- as signals. This module turns that single stream
into a small dispatcher: register callbacks with listen(), wait for one specific
signal with pull(), or run a blocking loop with the shell.

It corresponds to OpenOS's event.lua, kept deliberately small.
"""



# name -> list of callbacks. A callback returns False to unregister itself.
_listeners = {}

# timers: list of [deadline, interval_or_None, callback]
_timers = []

# next timer id
_next_timer = [1]
_timer_ids = {}


def listen(name, callback):
    """Call callback(name, *args) whenever a signal `name` arrives. Returns True
    if newly registered. A callback that returns False is removed."""
    lst = _listeners.setdefault(name, [])
    if callback in lst:
        return False
    lst.append(callback)
    return True


def ignore(name, callback):
    """Stop calling callback for signal `name`."""
    lst = _listeners.get(name)
    if lst and callback in lst:
        lst.remove(callback)
        if not lst:
            del _listeners[name]
        return True
    return False


def timer(interval, callback, times=1):
    """Call callback() after `interval` seconds. times>1 repeats; times=0 or a
    huge number repeats indefinitely. Returns an id usable with cancel()."""
    tid = _next_timer[0]
    _next_timer[0] += 1
    deadline = computer.uptime() + interval
    entry = [deadline, interval, callback, times]
    _timers.append(entry)
    _timer_ids[tid] = entry
    return tid


def cancel(tid):
    entry = _timer_ids.pop(tid, None)
    if entry is not None and entry in _timers:
        _timers.remove(entry)
        return True
    return False


def _run_due_timers():
    now = computer.uptime()
    # iterate over a copy: callbacks may add/cancel timers
    for entry in list(_timers):
        if now >= entry[0]:
            deadline, interval, callback, times = entry
            try:
                callback()
            except Exception as e:
                _report(e)
            if times is not None and times != 0:
                times -= 1
            entry[3] = times
            if times is not None and times <= 0:
                if entry in _timers:
                    _timers.remove(entry)
            else:
                entry[0] = now + interval


def _dispatch(signal):
    """Deliver one signal to its listeners. signal is (name, *args) or None."""
    if signal is None:
        return
    name = signal[0]
    args = signal[1:]
    lst = _listeners.get(name)
    if not lst:
        return
    for cb in list(lst):
        try:
            keep = cb(name, *args)
        except Exception as e:
            _report(e)
            keep = True
        if keep is False:
            ignore(name, cb)


def pull(timeout=None, name=None):
    """Wait for the next signal, optionally only one matching `name`. Returns the
    signal tuple (name, *args), or None on timeout. Timers and other listeners
    still fire while waiting."""
    deadline = None if timeout is None else computer.uptime() + timeout
    while True:
        # Figure out how long pullSignal may block: until the next timer, or the
        # caller's deadline, whichever is sooner, so timers stay roughly on time.
        budget = _time_to_next_timer()
        if deadline is not None:
            remaining = deadline - computer.uptime()
            if remaining <= 0:
                return None
            budget = remaining if budget is None else min(budget, remaining)

        sig = computer.pullSignal(budget)
        _run_due_timers()
        if sig is None:
            if deadline is not None and computer.uptime() >= deadline:
                return None
            continue

        # Always let registered listeners see it.
        _dispatch(sig)
        if name is None or sig[0] == name:
            return sig


def push(name, *args):
    """Queue a signal, as if hardware produced it."""
    return computer.pushSignal(name, *args)


def _time_to_next_timer():
    if not _timers:
        return None
    now = computer.uptime()
    soonest = None
    for entry in _timers:
        dt = entry[0] - now
        if dt < 0:
            dt = 0
        if soonest is None or dt < soonest:
            soonest = dt
    return soonest


def _report(exc):
    # Errors in a callback must not kill the event loop.
    try:
        print("event: callback error: " + str(exc))
    except Exception:
        pass
