---
navigation:
  title: "OC Automation: IO Hubs and Task-Based Input"
  parent: index.md
  icon: programmablehatches:proghatches.iohub
  keywords:
    - "OC Automation: IO Hubs and Task-Based Input"
    - "IO枢纽"
    - "OpenComputers"
    - "OC"
    - "无线外设"
    - "dualhatch"
    - "iohub"
item_ids:
  - "<programmablehatches:proghatches.iohub>"
  - "<gregtech:gt.blockmachines:22165>"
  - "<programmablehatches:proghatches.peripheral_station>"
  - "<programmablehatches:proghatches.oc.peripheral_card>"
  - "<programmablehatches:proghatches.oc.redstone>"
  - "<programmablehatches:proghatches.oc.api>"
---

# OC Automation: IO Hubs and Task-Based Input

The IO Hub combines inventory, tanks, ME access and transfer functions in an OC component. The programmable ME dual-input hub provides task-based extraction using database entries.

## Confirm component connectivity

Power the OC computer, connect the device with OC cable, and connect the device to the intended ME network. List component names and addresses:

```lua
local component = require("component")
for address, kind in component.list() do
  print(kind, address)
end
```

The IO Hub component type is `iohub`; the dual-input hub is `dualhatch`. Use full addresses when several devices of the same type are present.

## Begin with IO Hub queries

An IO Hub has 32 item slots and eight fluid slots, plus adjacent-inventory and ME operations. Query inventory size, stack size or tank level before testing a small transfer.

`transferItem` and `transferFluid` move materials between adjacent containers. `getCustomName` reads the hub name. The ME upgrade's database-recording operation is named `storeAE` here to distinguish it from inventory-control `store`.

Name hubs and log both name and address to verify the active device.

## Submit precise input tasks

Record a test item in the first OC database entry and provide sufficient ME stock and hatch space. This example requests four items:

```lua
local component = require("component")
local hatch = component.proxy("full-dual-input-hub-address")
local database = "full-database-address"

local task = assert(hatch.addTask(database, 0, 4, false))
print(task.id, task.state)

for _, result in ipairs(hatch.submitTask()) do
  print(result.id, result.state)
end
```

This device numbers database entries from zero. The last argument selects items when false or the fluid represented by a recorded container when true.

`addTask` queues a task; `state` queries a queued task; `cancelTask` cancels one; `submitTask` submits the current queue. `executeTask` executes a single request.

Inspect each returned state: executed, insufficient materials, disconnected network or partial execution. Query actual contents after a partial result and supply only the missing amount. Use `isEmpty` and `refund` to inspect the input and return leftovers.

## Wireless peripherals and optional cards

Connect target devices to a wireless peripheral station, bind its bridge card by right-clicking, and install the card in the computer. Keep the link one-to-one, within 16 blocks and in the same dimension. The station-side OC network must contain only one computer.

The GT advanced wireless-redstone card accesses GT wireless redstone; bind its UUID before using private channels. The item API card adds ore-dictionary queries and numeric/string item-ID conversion.
