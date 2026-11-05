---
navigation:
  title: "Tools and Upgrades: Encoding, Switching, Supply and Recovery"
  parent: index.md
  icon: programmablehatches:prog_toolkit
  keywords:
    - "Tools and Upgrades: Encoding, Switching, Supply and Recovery"
    - "编程工具箱"
    - "ME搋子"
    - "手动库存"
    - "无尽缓冲"
    - "标准化"
    - "toolkit"
    - "plunger"
item_ids:
  - "<programmablehatches:prog_toolkit>"
  - "<programmablehatches:prog_upgrades:4>"
  - "<programmablehatches:prog_upgrades:5>"
  - "<programmablehatches:prog_upgrades:3>"
  - "<gregtech:gt.blockmachines:22068>"
  - "<gregtech:gt.blockmachines:22085>"
  - "<gregtech:gt.blockmachines:22086>"
---

# Tools and Upgrades: Encoding, Switching, Supply and Recovery

This page collects common tools and upgrade entry points. Validate a new setup in order: encode a pattern, provide templates, supply materials, inspect results and recover leftovers.

## Programming Toolkit

Carry the toolkit while using NEI pattern transfer. Recipe inputs marked NC become their corresponding programming circuits.

Shift-right-click changes modes. Normal mode handles NC inputs; the reset-enabled mode adds an empty programming circuit when a recipe has no NC input, clearing the previous virtual circuit selection. A toolkit equipped in a bauble slot can use a configured mode-switch key.

Use the normalization tool described below to edit or clear circuits in existing interface patterns. Inspect a pattern's detailed inputs first to identify the circuits that should remain.

## Pattern maintenance tools

The circuit-pattern repair tool migrates applicable legacy patterns. The normalization tool standardizes programming-circuit data. After using the appropriate tool on a network, inspect the reported count and sample the resulting pattern details.

The normalization tool has set-to-one and clear operations, each with single-device and whole-network scope. Sneak-right-click changes modes. Single-device modes act on the clicked interface or input buffer. Network-wide modes first report the planned changes; clicking the same network again within ten seconds applies them. Select the intended operation and scope, then verify the result.

For a new production line, follow the [programmed-input guide](programmed_inputs.md) using actual recipes.

## Add programming to ordinary inputs

A programming cover lets supported GT input components and single-block machines use programming circuits to set their virtual circuit slot. Install the cover, test one programming circuit, then connect automation. Programmable dual inputs already include this function.

## Manual stocking upgrades

Manual item and fluid upgrades add real inventory shared by all buffers:

1. Hold the upgrade and right-click the hatch.
2. Open the main GUI and click its circuit icon to open shared slots.
3. Insert or extract items directly; use fluid containers to fill or empty fluid slots.
4. Run one recipe and observe its actual consumption from the shared slot.

Several buffers can use the same shared supply. Manual fluid capacity matches the hatch's own tank capacity, and all stocking upgrade types share a limit of four. Recover real materials before dismantling, particularly fluid in manual slots.

## ME stocking and elastic buffers

ME item/fluid upgrades mark resources and supply them from network stock. See the [EBF gas example](fluid_stocking.md).

The elastic-buffer upgrade unlocks temporary additional buffers on non-ME hatches. They accept more input when regular buffers are occupied, disappear when empty, and transfer their contents into regular buffers when space becomes available. Verify machine and output throughput before growing the queue.

## Recover leftovers

The ME plunger can return contents from supported dual-input buffers, item pipes and fluid containers to ME. Establish the required wireless-network binding and signal coverage, then test with a small amount.

Check the order and machine state before reclaiming leftovers. Confirm the result in both ME storage and the hatch GUI.

## Choose an input family

Ordinary, multi-fluid, super-fluid and stocking dual inputs serve different input sizes and supply methods. Match item slots, fluid types and delivery behavior to the recipe, then verify one operation. Stocking dual inputs read ME materials directly; manual or pattern-fed inputs receive materials through their corresponding delivery paths.
