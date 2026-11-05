---
navigation:
  title: Large Programming Circuit Provider
  parent: index.md
  icon: gregtech:gt.blockmachines:22116
  keywords:
    - large provider
    - prefabricated provider
    - provider chainer
    - parallel
    - programming circuit
item_ids:
  - "<gregtech:gt.blockmachines:22116>"
  - "<gregtech:gt.blockmachines:22117>"
  - "<gregtech:gt.blockmachines:22143>"
  - "<gregtech:gt.blockmachines:22144>"
  - "<gregtech:gt.blockmachines:22145>"
  - "<gregtech:gt.blockmachines:22146>"
  - "<gregtech:gt.blockmachines:22148>"
  - "<programmablehatches:proghatches.chip>"
---

# Large Programming Circuit Provider

<ItemLink id="gregtech:gt.blockmachines:22116" showIcon="left" /> combines several providers behind one ME connection. Its controller uses one channel and publishes programming circuits for integrated circuits, molds, extruder shapes and lenses.

Start with [Programming Circuit Providers and non-consumed items](programmed_inputs.md). An ordinary provider retains the real template while producing programming circuits that carry its identity.

## Build from front to back

The machine has a 3×3 cross-section and extends behind its controller. Use the construction preview for the selected length.

1. Build the front, with the controller in its center and energy and maintenance hatches in the front casing positions.
2. Add interior slices. Their centers contain Fluix blocks or crafting co-processing units.
3. Install provider components in the casing positions next to each interior center.
4. Complete the end cap. Its center can also hold a provider; with a central end-cap provider, keep the other positions in that slice as casing.
5. Connect the main ME network to the controller's front and supply EU and maintenance.

An ordinary provider embedded in the structure publishes through the large controller. Its standalone ME connection is disabled while it belongs to the multiblock.

## Three provider components

| Component | Configuration | Purpose |
| --- | --- | --- |
| Ordinary Programming Circuit Provider | Insert real circuits, molds or other templates | Supply custom programming circuits |
| Prefabricated Provider | Choose a component for the required category | Supply a preset collection |
| Provider Chainer | Connect its front to another provider network | Aggregate that network's providers |

Available preset categories:

- <ItemLink id="gregtech:gt.blockmachines:22143" showIcon="left" />: integrated circuits.
- <ItemLink id="gregtech:gt.blockmachines:22144" showIcon="left" />: molds.
- <ItemLink id="gregtech:gt.blockmachines:22145" showIcon="left" />: slicers.
- <ItemLink id="gregtech:gt.blockmachines:22146" showIcon="left" />: extruder shapes.
- <ItemLink id="gregtech:gt.blockmachines:22148" showIcon="left" />: lenses.

Prefabricated providers are components of the large structure. Ordinary providers add templates beyond those preset categories.

## Demonstration

The scene builds the machine in stages, then opens the ordinary provider to show its ingot mold. The component on the right supplies preset extruder shapes. The chainer accesses a separate network whose ordinary provider holds a plate mold.

The controller GUI then opens its multiplier popup and changes the amount to 4. The final stages demonstrate the upgrade chip and the stored-circuit cleanup toggle.

<GameScene width="277" height="198" zoom={1.6} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/large_provider.snbt" formed={true} />
  <ImportPonder src="/assets/large_provider_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="2" y="1" z="3" from="420" to="500" scale="2" showMain="true" />
  <MUI2Gui x="3" y="1" z="2" from="660" to="820" scale="2" showMain="true">
    <MUI2Click at="720" target="lpcp_open_parallel" />
    <MUI2Click at="750" target="lpcp_multiplier" value="4" />
  </MUI2Gui>
  <MUI2Gui x="3" y="1" z="2" from="920" to="1000" scale="2" showMain="true">
    <MUI2Click at="950" target="lpcp_cleanup" />
  </MUI2Gui>
</GameScene>

The recording uses debug power, creative maintenance and self-powered ME controllers. A survival build requires appropriate power and maintenance.

## Connect a Provider Chainer

Install an <ItemLink id="gregtech:gt.blockmachines:22117" showIcon="left" /> in the structure. Connect its front to the template-provider subnet, while the large controller connects to the network using those circuits. Power the subnet and supply channels to its ordinary providers and the chainer.

A shared template network can centralize common templates. Keep provider dependencies directed: the main network reads the template subnet, whose providers supply the requested templates. A circular access chain stops the large provider with a loop-detection message; inspect network membership and disconnect the circular route.

## Co-processors and amount per operation

Co-processing units increase the upper limit of the controller's amount setting. Open the multiplier popup and enter an amount; the server limits it to what the structure supports.

This setting controls the number of programming circuits produced by each offered pattern. At 4, one operation produces four circuits. Configure the downstream processing machine's parallelism separately.

The recorded test orders eight ingot-mold programming circuits using the four-per-operation pattern. The real mold stays in its ordinary provider.

## Power, upgrade chip and cleanup

Each provider or chainer component costs 256 EU/t. The scene contains three, so its running power is 768 EU/t. Update the energy supply when expanding the provider collection.

Place an <ItemLink id="programmablehatches:proghatches.chip" showIcon="left" /> in the controller's item slot. The machine consumes the chip and enables instant-completion support. The structure and GUI still determine the amount supplied per operation.

The stored-programming-circuit cleanup button periodically removes programming circuits from the connected ME inventory. Enable it for networks that generate these items on demand; leave it off when retaining a manual reserve. Real templates remain in their ordinary providers.
