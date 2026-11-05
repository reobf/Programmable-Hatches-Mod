---
navigation:
  title: "Remote Inputs, Mirrors and Mapping Mirrors"
  parent: index.md
  icon: gregtech:gt.blockmachines:22049
  keywords:
    - "Remote Inputs, Mirrors and Mapping Mirrors"
    - "远程输入"
    - "输入镜像"
    - "映射镜像"
    - "共享输入总线"
    - "remote input"
    - "mirror"
item_ids:
  - "<gregtech:gt.blockmachines:22049>"
  - "<gregtech:gt.blockmachines:22050>"
  - "<gregtech:gt.blockmachines:22067>"
  - "<gregtech:gt.blockmachines:22166>"
  - "<gregtech:gt.blockmachines:22167>"
  - "<gregtech:gt.blockmachines:22168>"
  - "<gregtech:gt.blockmachines:22169>"
  - "<gregtech:gt.blockmachines:22087>"
---

# Remote Inputs, Mirrors and Mapping Mirrors

These devices solve different routing problems: accessing a distant container, sharing recipe inputs, or expanding the entry points of an existing hatch.

| Device | Target | Use |
| --- | --- | --- |
| Remote input bus/hatch | Another container | Use items in a distant chest or fluids in a tank |
| Dual-input mirror | A programmable dual input hatch | Share isolated input groups across structure positions |
| Item/fluid input mirror | Matching contents of the master's first nonempty buffer | Access shared ingredients through an ordinary input position |
| Input-area mapping mirror | The master's staging input area | Add logistics or pattern entry points |

## Access a distant chest or tank

1. Record the target container with a tricorder.
2. Left-click the remote input with that tricorder to set its target.
3. Inspect the target information and accessible contents in the GUI.
4. Ensure extraction is allowed from the direction the remote input uses, and keep the target chunk loaded.
5. Run one real recipe and check deductions at the container and outputs at the machine.

Remote inputs work within one dimension. Their GUI previews show a limited number of slots or fluid types; actual access follows the target container's extraction rules.

## Bind an input mirror

Left-click the master dual input hatch with a data stick, then right-click the mirror. A dual-input mirror preserves input isolation. The item-only and fluid-only mirrors behave as ordinary input components during recipe checking.

Use a small amount of material to verify consumption, recipe selection and output routing before sharing inputs between several machines. Check the master's and mirror's buffer-order settings when prioritizing larger or smaller batches.

## Expand interface and pattern entry points

An input-area mapping mirror is external to the multiblock structure. Bind it to the master and attach an interface at the mapped position; the ME version offers additional pattern slots. Wire cutters configure allowed input sides.

Verify the target machine and coordinates in its GUI. Highlight locates the master, and the open-master button accesses the hatch that actually owns the buffers.

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="Interface" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="Mapping" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="Master" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## Share circuit settings

The shared-input-bus mirror cover synchronizes virtual circuit slots between buses on the same channel. Install a programming cover on one master bus and mirror covers on the others. Change the master circuit and observe the followers.

For copying a fixed collection of virtual items, use phantom input buses with a data stick: record target buses, then copy marks from a configured source. Keep target chunks loaded and verify the copied marks.
