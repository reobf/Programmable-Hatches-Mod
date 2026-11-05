---
navigation:
  title: "Programmed inputs: circuits and molds"
  parent: index.md
  icon: gregtech:gt.blockmachines:22033
  keywords:
    - programmable
    - buffered
    - programming circuit
    - provider
    - mold
    - NC
    - blocking
item_ids:
  - "<gregtech:gt.blockmachines:22000>"
  - "<gregtech:gt.blockmachines:22001>"
  - "<gregtech:gt.blockmachines:22002>"
  - "<gregtech:gt.blockmachines:22003>"
  - "<gregtech:gt.blockmachines:22004>"
  - "<gregtech:gt.blockmachines:22005>"
  - "<gregtech:gt.blockmachines:22006>"
  - "<gregtech:gt.blockmachines:22007>"
  - "<gregtech:gt.blockmachines:22008>"
  - "<gregtech:gt.blockmachines:22009>"
  - "<gregtech:gt.blockmachines:22010>"
  - "<gregtech:gt.blockmachines:22011>"
  - "<gregtech:gt.blockmachines:22012>"
  - "<gregtech:gt.blockmachines:22013>"
  - "<gregtech:gt.blockmachines:22014>"
  - "<gregtech:gt.blockmachines:22032>"
  - "<gregtech:gt.blockmachines:22033>"
  - "<gregtech:gt.blockmachines:22034>"
  - "<gregtech:gt.blockmachines:22035>"
  - "<gregtech:gt.blockmachines:22036>"
  - "<gregtech:gt.blockmachines:22037>"
  - "<gregtech:gt.blockmachines:22038>"
  - "<gregtech:gt.blockmachines:22039>"
  - "<gregtech:gt.blockmachines:22040>"
  - "<gregtech:gt.blockmachines:22041>"
  - "<gregtech:gt.blockmachines:22042>"
  - "<gregtech:gt.blockmachines:22043>"
  - "<gregtech:gt.blockmachines:22044>"
  - "<gregtech:gt.blockmachines:22045>"
  - "<gregtech:gt.blockmachines:22046>"
  - "<gregtech:gt.blockmachines:22048>"
  - "<gregtech:gt.blockmachines:22118>"
---

# Programmed inputs: circuits and molds

<ItemLink id="gregtech:gt.blockmachines:22001" showIcon="left" /> accepts items and fluids and uses a programming circuit in each pattern to select the virtual configuration. <ItemLink id="gregtech:gt.blockmachines:22033" showIcon="left" /> stores complete input sets in recipe buffers, together with their materials and virtual configuration.

## Connections and pattern placement

1. Install the hatch in a GregTech multiblock that supports dual inputs. Point an ME Interface output face at the hatch's front. Use a fluid-compatible ME Interface for fluid recipes.
2. Enable blocking mode on the ME Interface. Both the plain and buffered hatches use this setting to receive complete input sets.
3. Put processing patterns in the ME Interface's pattern slots. The hatch receives and handles their inputs.
4. Connect <ItemLink id="gregtech:gt.blockmachines:22048" showIcon="left" /> to the same ME network through its front face, with one channel available. Return machine outputs to ME and supply power to the machine and network.

## Non-consumable recipe inputs

NEI marks non-consumable inputs with NC. The recipe uses these items as selectors or catalysts and retains them during processing. Integrated circuits, extruder shapes, casting molds and lenses can all be represented by programming circuits.

This example uses the iron rod extrusion recipe:

<RecipeFor id="gregtech:gt.metaitem.01:23032" handlerId="gt.recipe.extruder" input="gregtech:gt.metaitem.01:32351" fallbackText="1 iron ingot with an Extruder Shape (Rod) makes 2 iron rods. The shape is marked NC." />

| Location | Item | Purpose |
| --- | --- | --- |
| Programming Circuit Provider | A real <ItemLink id='gregtech:gt.metaitem.01:32351' showIcon="left" /> template | Retains the template and offers crafting for its programming circuit |
| Processing pattern in the ME Interface | 1 <ItemLink id='minecraft:iron_ingot:0' showIcon="left" /> + 1 <ItemLink id='programmablehatches:prog_circuit:0:{targetCircuit:{Count:1b,Damage:32351s,string_id:"gregtech:gt.metaitem.01"}}' showIcon="left" /> → 2 <ItemLink id='gregtech:gt.metaitem.01:23032' showIcon="left" /> | Carries the required shape configuration with each input set |
| Hatch virtual slot | Extruder Shape (Rod) configuration | Matches the recipe's NC requirement |

The provider produces a programming circuit that records the item and its configuration. The real shape stays in the provider. The network pays AE energy to obtain the circuit; the hatch consumes that circuit and writes its configuration into a virtual slot. Its icon represents the configuration available to the machine.

Integrated circuits work the same way: put a circuit set to the required number in the provider, and use the matching programming circuit in the pattern. Prepare a template for each required shape, lens or circuit number.

## Encoding with the Programming Toolkit

Carry <ItemLink id="programmablehatches:prog_toolkit:1" showIcon="left" /> in your inventory, set it to normal mode, and use NEI recipe transfer in an ME Pattern Terminal. The toolkit converts NC inputs into their matching programming circuits.

Check the encoded inputs and outputs: 1 iron ingot + 1 programming circuit (Rod shape) → 2 iron rods. With the template in the provider, AE can supply the circuit for a crafting job. To clear a previous virtual configuration before a recipe with no NC input, use the toolkit mode that adds an empty programming circuit to such recipes.

## Plain hatch: configure each input set

Play the scene to open the provider GUI, place the rod and plate templates in sequence, and see them retained after supplying a circuit. Then the plain hatch GUI shows delivery, virtual shape selection, consumption and the next pattern selecting a plate shape. Hover over templates and hatch items to read their tooltips.

The scene shows the input side. Install the hatch in the positions permitted by the actual multiblock. The demonstration uses a self-powered ME controller.

<GameScene width="277" height="198" zoom={1.7} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/programmed_inputs.snbt" />
  <ImportPonder src="/assets/programmed_inputs_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="2" from="60" to="280" scale="2" showMain="true" />
  <MUI2Gui x="3" y="1" z="1" from="340" to="900" scale="2" showMain="true">

  </MUI2Gui>
</GameScene>

One input set in a plain hatch uses one virtual configuration. Blocking mode waits for the previous set to finish before sending the next. The next programming circuit selects its configuration, allowing one machine to alternate between rod and plate patterns.

## Buffered hatch: input area and recipe buffers

Keep the same interface connection, blocking setting, patterns and provider templates. The buffered hatch has two areas:

- Input area: receives one complete set of items and fluids from the interface.
- Recipe buffer: records the input types, amounts, slot order and fluids, and holds the materials and virtual configuration for the machine.

Once the first set enters a buffer, the input area clears and the interface can send another. Matching sets accumulate in that buffer for the machine to consume according to its throughput and parallel capacity. Models with several buffers keep separate recipes and configurations.

<GameScene width="277" height="198" zoom={1.7} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/buffered_inputs.snbt" />
  <ImportPonder src="/assets/buffered_inputs_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="2" from="60" to="280" scale="2" showMain="true" />
  <MUI2Gui x="3" y="1" z="1" from="340" to="900" scale="2" showMain="true">
    <MUI2Click at="500" target="open_buffer_0" />
  </MUI2Gui>
</GameScene>

The scene first opens the provider GUI to show the shape templates, then switches to the buffered hatch GUI. The main input area clears after the first ingot and circuit enter the buffer. Clicking + at the top left opens buffer 0, showing the ingot and virtual shape on the right. A second matching set raises the ingot count to 2 while the buffer retains its shape configuration.

Send recipe-specific circuits, shapes and lenses through each pattern. The main window's shared virtual configuration is suitable for conditions common to all buffers.

## Configuration checks

- The provider has ME connectivity, a channel, and the real circuit, shape or lens templates.
- NC inputs in each pattern have been converted to matching programming circuits.
- The ME Interface uses blocking mode and points at the hatch's front.
- Programming mode is enabled on the hatch.
- Use + to inspect buffered materials and virtual configuration; the main window shows incoming inputs.

[EBF gases and the ME Fluid Stocking Upgrade](fluid_stocking.md): configure shared gases at the hatch and retain compatible processing patterns when changing gas.
