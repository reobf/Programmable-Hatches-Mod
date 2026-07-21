-- init.lua -- sentinel for the MPYOS floppy.
--
-- MPYOS is a Python OS: it boots from /init.py under the MPYOS BIOS. This file
-- exists only so that if someone boots this floppy with a *Lua* BIOS (the stock
-- OpenComputers BIOS runs /init.lua), they get a clear message instead of a
-- confusing failure. It does not start an OS -- it explains and halts.

local function first(kind)
  for addr in component.list(kind) do return addr end
  return nil
end

local msg = {
  "MPYOS: wrong BIOS.",
  "",
  "This is a MPYOS (Python) floppy. It must be booted with the",
  "MPYOS BIOS, not a Lua BIOS.",
  "",
  "  - Craft a MPYOS BIOS: any EEPROM + this MPYOS floppy.",
  "  - Or flash it: put the MPYOS BIOS EEPROM in the machine.",
  "",
  "Then reboot. (Under a Lua BIOS, MPYOS cannot run.)",
}

local gpu = first("gpu")
local screen = first("screen")
if gpu and screen then
  pcall(function()
    component.invoke(gpu, "bind", screen)
    local w, h = component.invoke(gpu, "getResolution")
    component.invoke(gpu, "fill", 1, 1, w, h, " ")
    for i = 1, #msg do
      component.invoke(gpu, "set", 1, i, msg[i])
    end
  end)
end

pcall(function() computer.beep(1000, 0.2) end)
pcall(function() computer.beep(1000, 0.2) end)

-- Halt: sit idle rather than error out (an error would just scroll past).
while true do
  pcall(function() computer.pullSignal(1) end)
end
