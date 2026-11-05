---
navigation:
  title: "Storage and Output: Bulk Storage, Buffered Outputs and Fluid Filling"
  parent: index.md
  icon: gregtech:gt.blockmachines:22119
  keywords:
    - "Storage and Output: Bulk Storage, Buffered Outputs and Fluid Filling"
    - "超级箱"
    - "超级缸"
    - "量子箱"
    - "量子缸"
    - "存储输出"
    - "库存输出"
    - "装填机"
    - "水提供器"
    - "super chest"
    - "tank"
item_ids:
  - "<gregtech:gt.blockmachines:22119>"
  - "<gregtech:gt.blockmachines:22131>"
  - "<gregtech:gt.blockmachines:22177>"
  - "<gregtech:gt.blockmachines:22178>"
  - "<programmablehatches:stockingexport:0>"
  - "<programmablehatches:stockingexport:1>"
  - "<gregtech:gt.blockmachines:22081>"
  - "<programmablehatches:autofillerMKII>"
---

# Storage and Output: Bulk Storage, Buffered Outputs and Fluid Filling

Machine outputs can remain in a local buffer while being exposed as ME inventory. Bulk storage, buffered outputs and exporting an entire subnet have different devices.

## ME super chests and tanks

ME super/quantum chests store a bulk item type; their tank counterparts store a fluid type. Connect them to ME and configure priority and sticky behavior.

1. Start with one material and observe transfer from input buffers into main storage.
2. Insert and extract a small amount through a terminal.
3. Test where new injections go with sticky mode enabled. Use the empty-container auto-unlock option when desired.
4. After painting a device, connect matching-color or default-color ME cables.

The tank's item input slots accept fluid packets and decode them into its internal tank. Verify recovery arrangements against terminal stock and actual stored amounts.

## Storage output buses and hatches

These outputs store products internally and expose a read-only ME inventory. They suit products that should remain near a machine until requested.

The item version defaults to 1600 items, and the fluid version to 128000 mB. Storage cells change buffer capacity; wire cutters switch ME connection mode.

Run one recipe, observe the internal output, find it in ME, then retrieve it through a terminal. Leave enough buffer capacity for continuous production and configure output protection.

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="Products" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="Buffer" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="ME use" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## Retained samples and filtered outputs

A tenacious output bus retains one real item per slot during automatic output; a filtered output bus retains a virtual item mark. Combined with machine output protection, these output positions can indirectly restrict recipe selection.

Use a test machine with only that output arrangement, configure every participating slot for the intended product, then compare the intended recipe with other recipes. Recheck output routing after changing samples. Ingredient buffers also provide cover-compatible staging containers for Smart Arm routing.

## Export a whole subnet

An ME Stocking Export Bus attempts to export all stored items; the fluid version exports all stored fluids. They suit dedicated subnets or forwarding already-classified materials.

Isolate the intended inventory in a subnet, verify that the destination accepts it, and then add acceleration cards.

## Water and fluid filling

A Water Provider advertises water crafting to ME. Connect it to the crafting network and supply actual machines through their normal fluid input paths.

The advanced fluid autofiller can generate filling patterns for fluids that are craftable but currently absent from storage. Provide fluid recipes, containers and a CPU, then order one filled container to verify production and filling.

For a fixed supply beside a machine, use an [Amount Maintainer](stock_control.md). For gas withdrawal during processing, use the [ME fluid stocking upgrade](fluid_stocking.md).
