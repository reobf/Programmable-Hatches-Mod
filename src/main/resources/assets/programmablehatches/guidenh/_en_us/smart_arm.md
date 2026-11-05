---
navigation:
  title: "Smart Arms: Transfer by Slot Expressions"
  parent: index.md
  icon: programmablehatches:proghatches.smartarm:3
  keywords:
    - "Smart Arms: Transfer by Slot Expressions"
    - "智能机械臂"
    - "槽位映射"
    - "表达式"
    - "Smart Arm"
item_ids:
  - "<programmablehatches:proghatches.smartarm:3>"
---

# Smart Arms: Transfer by Slot Expressions

A Smart Arm maps slots between its host and an adjacent inventory using an expression. It is useful for fixed ingredient positions, reversed slot order or a selected range of slots.

## Start with matching slot numbers

1. Attach the arm to an inventory-bearing GT block facing another container.
2. Select outside-to-inside transfer.
3. Select the o→i mapping: o is the external slot number, and the expression returns the internal slot.
4. Enter `o`, choose static mode and recalculate.
5. Place small stacks in several external slots and inspect the corresponding internal positions.

Slots are numbered from zero. Mappings outside the internal inventory's bounds are skipped; inspect them with the probe window first.

## Three observable examples

| Goal | o→i expression | Result |
| --- | --- | --- |
| Same slot number | `o` | External 0, 1 and 2 map to internal 0, 1 and 2 |
| Reverse the first 16 slots | `15-o` | External 0 maps to internal 15; external 15 maps to internal 0 |
| Map only the first 16 | `o < 16 ? o : -1` | External 0–15 map directly; other mappings are ignored |

In i→o mode, use i for the internal slot and return the external slot. Mapping direction and transfer direction are separate settings.

## Static and dynamic evaluation

Static mode builds a table and runs that table. Recalculate after changing the expression or adjacent container, then check the ready state.

Dynamic mode evaluates during operation and can read machine enable state and wireless redstone information. Its minimum interval is five ticks; static mode can reach one tick. Arm tier also determines throughput.

## Probe and diagnose

Use the probe window to select a source slot and inspect its calculated destination. Syntax errors, invalid targets and out-of-range slots have diagnostic states. Verify one mapping before testing the whole range.

When other pipes or covers move the same items, pause them during the mapping check, then restore the complete logistics arrangement.
