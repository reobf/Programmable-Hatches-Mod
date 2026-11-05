---
navigation:
  title: "Special Inputs: Data Orbs, Catalysts and Virtual Items"
  parent: index.md
  icon: gregtech:gt.blockmachines:22083
  keywords:
    - "Special Inputs: Data Orbs, Catalysts and Virtual Items"
    - "数据球"
    - "催化剂"
    - "数据访问仓"
    - "多电路"
    - "虚拟输入"
    - "data orb"
    - "catalyst"
item_ids:
  - "<gregtech:gt.blockmachines:22083>"
  - "<programmablehatches:orbswitcher>"
  - "<gregtech:gt.blockmachines:22084>"
  - "<gregtech:gt.blockmachines:22162>"
  - "<gregtech:gt.blockmachines:22087>"
  - "<gregtech:gt.blockmachines:22150>"
  - "<programmablehatches:proghatches.cover:90>"
---

# Special Inputs: Data Orbs, Catalysts and Virtual Items

Data orbs, catalysts and fixed non-consumed items require different supply mechanisms. Determine whether the machine needs data selection, a real item replacement or a virtual item mark.

## Select data orbs for replication

The <ItemLink id="gregtech:gt.blockmachines:22083" showIcon="left" /> stores data orbs for the large element replicator, and programmed circuits 1–16 in its circuit slot choose the active orb slot. The <ItemLink id="programmablehatches:orbswitcher" showIcon="left" /> replaces the orb in that slot according to what the input hatch receives, so one machine can process a different element for each order:

1. Set programmed circuit 1 in the orb hatch's circuit slot.
2. Place a storage bus on the orb hatch and connect its ME network to the switcher's green target port. This network holds the current orb and has its own power.
3. Store the data orb of every element in the ME network on the blue inventory port.
4. Use a wrench to point the switcher's front at the <ItemLink id="gregtech:gt.blockmachines:22001" showIcon="left" /> on the replicator, open the switcher and set "Match NBT" to true. Data orbs differ by NBT.
5. Put a matching data orb into a <ItemLink id="gregtech:gt.blockmachines:22048" showIcon="left" /> as a template and add its programming circuit to that element's processing pattern.
6. When an order arrives, the input hatch writes the orb carried by the programming circuit into its circuit slot. The switcher returns the old orb to the inventory network and sends the new orb to the target network.

The switcher reads the hatch's main circuit slot, so this setup uses a Programmable Dual Input Hatch; buffered hatches keep their configuration in each recipe buffer. See [Programmed inputs: circuits and molds](programmed_inputs.md) for the pattern and provider setup.

Order small amounts of two elements and verify both the orb swap and the product.

## Switch chemical-plant catalysts

The catalyst switcher maintains a target catalyst inventory according to the item in a GT block's circuit slot:

1. Connect the blue inventory port to the network holding candidate catalysts.
2. Connect the green target port to the network accessing the actual catalyst inventory.
3. Use a wrench to point the front at the GT block whose circuit slot selects the catalyst.
4. Mark the desired catalyst and configure NBT and damage matching.
5. Change the mark, observe replacement, and run the corresponding recipe.

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="Catalysts" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="Switcher" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="Target" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

The ME catalyst input reads the first catalyst in its network's active area. Its supply and replacement still depend on the inventory and switching arrangement. Verify the catalyst physically available to the recipe.

## Read research data from ME

The ME data-access hatch collects data sticks from its network, allowing centralized research storage. Put the required sticks into accessible ME storage and verify that the target recipe recognizes its research.

## Virtual items and multiple circuits

A phantom input bus marks multiple virtual items for machines that explicitly support such non-consumed inputs. Keep actually consumed ingredients on a normal supply path.

Multi-circuit buses and dual inputs provide several circuit slots. Circuit-holder covers store commonly used marks for selecting the circuit slot through the GUI. Verify simultaneous circuit choices and active-selection changes separately to ensure the intended recipe is selected.
