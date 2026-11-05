---
navigation:
  title: "ME Fluid Stocking Upgrade: EBF Gases"
  parent: index.md
  icon: programmablehatches:prog_upgrades:2
  keywords:
    - ME fluid stocking upgrade
    - fluid pulling
    - blast furnace
    - nitrogen
    - argon
item_ids:
  - "<programmablehatches:prog_upgrades:2>"
---

# ME Fluid Stocking Upgrade: EBF Gases

Install an <ItemLink id="programmablehatches:prog_upgrades:2" showIcon="left" /> in the blast furnace's dual input hatch to supply its gas directly from ME storage. Processing patterns describe the solid ingredients and programming circuits. Configure the gas separately in the shared fluid slot.

Changing the marked gas updates the supply for every compatible pattern in that hatch. Keep the existing patterns when switching between recipes with identical solid inputs, circuit configuration and outputs.

## Example: Phosphorus-Doped Monocrystalline Silicon

Both variants below require the same solids, circuit 3, and 2484 K coil heat.

| Configuration | Solid inputs | Gas per operation | Base duration | Output |
| --- | --- | --- | --- | --- |
| Nitrogen | 64 solar-grade silicon dust, 2 small gallium arsenide crystals, 8 phosphorus dust; circuit 3 | 8000 L nitrogen | 600 seconds | 1 phosphorus-doped monocrystalline silicon |
| Argon | Same | 6800 L argon | 480 seconds | Same |

<ItemLink id='ae2fc:fluid_drop:0:{Fluid:"nitrogen",DisplayOnly:1b}' showIcon="left" /> and <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"argon",DisplayOnly:1b}' showIcon="left" /> have their own recipe costs and durations. Voltage, coil heat and overclocking affect the actual processing time.

## Encode the pattern

Set the processing inputs to 64 <ItemLink id="gregtech:gt.metaitem.01:2856" showIcon="left" />, 2 <ItemLink id="gregtech:gt.metaitem.03:32191" showIcon="left" />, 8 <ItemLink id="gregtech:gt.metaitem.01:2021" showIcon="left" />, and one programming circuit carrying integrated circuit 3. Set the output to one <ItemLink id="gregtech:gt.metaitem.03:32031" showIcon="left" />. Leave gas supply to the hatch's ME fluid slot.

The programming circuit remains a pattern input. Place a real integrated circuit configured to 3 in a Programming Circuit Provider to supply it through the network. See [Programmed Dual Input Hatches](programmed_inputs.md).

AE plans the solids and programming circuit. Stock gas in the same network beforehand, or maintain it with another production line. Each time the machine starts a recipe, the hatch withdraws the actual required gas amount.

## Install and configure

1. Install the input hatch in the EBF. Connect a pattern input hatch directly to ME. For an ordinary or buffered programmable dual input hatch, place an ME Interface against its front face; gas comes from that interface's network.
2. Hold the ME Fluid Stocking Upgrade and right-click the hatch. Each upgrade adds one fluid marking slot; all stocking upgrade types share a limit of four.
3. Open the main GUI and click its circuit icon to open the shared slots window.
4. Hold a container of nitrogen and left-click the fluid mark on the left. It records the fluid type while the container stays on the cursor. The slot on the right shows the corresponding ME stock.
5. Install the processing pattern, stock nitrogen in ME, and order the silicon crystal.

All input buffers in the hatch share the same fluid mark. The recipe determines consumption; the left slot only specifies which fluid to use.

## Demonstration: one pattern, two gases

Play the scene to see upgrade installation, the pattern window and the shared slots window. The mark changes from nitrogen to argon, with the stock display showing actual deductions from each completed operation. The final pattern window shows the original pattern. Hover over patterns or fluids for tooltips.

<GameScene width="277" height="198" zoom={1.7} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/fluid_stocking.snbt" formed={true} />
  <ImportPonder src="/assets/fluid_stocking_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="2" y="0" z="2" from="150" to="260" scale="2" showMain="true">
    <MUI2Click at="200" target="open_patterns" />
  </MUI2Gui>
  <MUI2Gui x="2" y="0" z="2" from="260" to="800" scale="2" showMain="true">
    <MUI2Click at="270" target="open_shared" />
  </MUI2Gui>
  <MUI2Gui x="2" y="0" z="2" from="820" to="940" scale="2" showMain="true">
    <MUI2Click at="850" target="open_patterns" />
  </MUI2Gui>
</GameScene>

The recording uses a self-powered ME controller, a debug energy hatch and creative maintenance to speed up the demonstration. A survival build needs suitable coil heat, voltage, power and maintenance.

## Change the gas

After the current operation completes, open the shared slots window. Left-click the nitrogen mark with an empty cursor to clear it, then left-click with an argon container to mark argon. Check the argon stock on the right and continue ordering with the same pattern.

Each operation in this example consumes either 8000 L nitrogen or 6800 L argon. The remaining nitrogen stays in ME after the switch.

## Check the recipe variants

- Use NEI to confirm that the selected gas variant has the same solid inputs, outputs and circuit configuration.
- Supply the required coil temperature and voltage.
- Keep the gas in the ME network accessible to the input hatch. Processing waits when that gas is insufficient.
- Patterns with different circuit configurations still need their own matching programming circuits.
- Choose a gas supported by all recipes sharing the mark; separate input hatches can supply recipes with different gas requirements.

The hatch becomes the single place to change a shared protective gas while retaining compatible processing patterns.
