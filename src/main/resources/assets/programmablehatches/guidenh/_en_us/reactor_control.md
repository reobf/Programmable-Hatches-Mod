---
navigation:
  title: "Control and Maintenance: Reactor Timing, Sensors and Golems"
  parent: index.md
  icon: programmablehatches:proghatches.reactor_syncer
  keywords:
    - "Control and Maintenance: Reactor Timing, Sensors and Golems"
    - "反应堆"
    - "更新同步器"
    - "维护傀儡"
    - "读卡器"
    - "reactor"
    - "golem"
    - "sensor"
item_ids:
  - "<programmablehatches:proghatches.reactor_syncer>"
  - "<programmablehatches:proghatches.card_reader>"
  - "<programmablehatches:proghatches.cover:1>"
---

# Control and Maintenance: Reactor Timing, Sensors and Golems

These devices connect machine state to control systems: the reactor synchronizer exposes update timing, the sensor-card reader supplies text readings to OC, and maintenance golems service multiblocks.

## Observe reactor update timing first

Use an isolated test reactor and record the synchronizer's redstone with an indicator or OC. Configure signal strengths for update and non-update ticks, then observe the reactor's 20-tick cycle.

Before controlling fuel or component changes, verify actuator, logistics and signal delays. Ordinary redstone follows its own update rhythm; use the documented redstone P2P, conduit or red-alloy options when single-tick response matters.

World reload changes the reactor update-counter starting point. The skip-first-update-cycle option leaves time to restore control state. Verify reload behavior in an isolated structure before using the timing in an operating control program.

## Read sensor cards

Insert configured sensor cards and connect the reader to OC. Its component type is `cardreader`, and its first card slot is zero.

```lua
local component = require("component")
local reader = component.proxy("full-card-reader-address")
print(reader.getTitle(0))
```

Verify the title first, then read the appropriate lines with `getText`. Handle units, missing values and field names according to the actual card contents.

## Maintenance golems

Install the maintenance core, bind the golem to the multiblock controller with a golemancer's bell, and provide a clear route to the maintenance hatch. Create an observable maintenance requirement and verify that the golem services the correct location.

Recheck binding and pathfinding after moving the machine or maintenance hatch.

## Wireless and stock-based control

The wireless-control cover offers public/private channels, inversion and a safe mode. Observe the signal before using it to enable a machine. For stock-driven operation, see [stock control](stock_control.md).

Verify sensing, decision logic and actuation separately so waiting can be traced to the reading, connection or actuator.
