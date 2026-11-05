---
navigation:
  title: "Cyclic Patterns: Palladium"
  parent: index.md
  icon: gregtech:gt.blockmachines:22182
  keywords:
    - cyclic pattern
    - balancing
    - seed
    - growth
    - chance pattern
    - palladium
item_ids:
  - "<gregtech:gt.blockmachines:22182>"
---

# Cyclic Patterns: Palladium

The <ItemLink id="gregtech:gt.blockmachines:22182" showIcon="left" /> runs a group of patterns as one cycle. Some items of the group are both inputs and outputs; the hatch works out how many times each pattern runs and publishes the whole round to AE as a single pattern. An order supplies the net materials and programming circuits written on that pattern, and the items circulating in the cycle come from seeds the hatch keeps.

Enriched ammonia from upstream precious-metal processing is the net feed. Ammonia supports recycling, while metallic powder remains an internal intermediate. Palladium processing combines a cycle with chance outputs: <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> and <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> are used up as soon as they are made, and <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> comes out of a sifter by chance and goes back into the reactor.

## Concepts

- **Cyclic item**: an item that patterns of the same group both consume and produce.
- **Balancing**: run counts for the patterns that make every cyclic item's inputs and outputs cancel out. A balancing is published as one pattern listing net materials and the programming circuits required for execution.
- **Seed**: the cyclic items put in at the start of a round. The hatch keeps a seed stock and takes the round's seeds back when the products enter ME.
- **Chance pattern**: a pattern whose outputs are written at their expected amount. When the rolls produce less than the pattern says, a <ItemLink id="programmablehatches:chance_controller" showIcon="left" /> adds inputs and keeps the machine running until the written amount is produced.

## Material flow

<Flowchart align="center" width="340" height="358" canvasWidth="328" canvasHeight="348">
  <FlowNode id="enriched" x="52" y="98" size="72" shape="circle" color="#95cc75" textColor="#28352c" label="Enriched|ammonia" item='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' />
  <FlowNode id="enrich" x="164" y="98" size="76" shape="rect" color="#777f87" textColor="#ffffff" label="Chemical reactor|Ammonia recycling ×6" item='gregtech:gt.blockmachines:1169' />
  <FlowNode id="ammonia" x="276" y="98" size="72" shape="circle" color="#95cc75" textColor="#28352c" label="Ammonia" item='ae2fc:fluid_drop:0:{Fluid:"ammonia",DisplayOnly:1b}' />
  <FlowNode id="react" x="52" y="198" size="76" shape="rect" color="#777f87" textColor="#ffffff" label="Chemical reactor|Precipitation ×1" item='gregtech:gt.blockmachines:1169' />
  <FlowNode id="powder" x="164" y="198" size="72" shape="circle" color="#c93838" textColor="#ffffff" label="Palladium|metallic powder" item='bartworks:gt.bwMetaGenerateddust:53' />
  <FlowNode id="me" x="52" y="22" size="32" shape="rect" color="#e0e4e8" textColor="#343a40" label="Upstream" />
  <FlowNode id="raw" x="52" y="298" size="72" shape="circle" color="#e0c675" textColor="#343a40" label="Reprecipitated|palladium" item='bartworks:gt.bwMetaGenerateddust:54' />
  <FlowNode id="salt" x="164" y="298" size="72" shape="circle" color="#d5d8db" textColor="#343a40" label="Palladium|salt" item='bartworks:gt.bwMetaGenerateddust:55' />
  <FlowNode id="sift" x="276" y="298" size="76" shape="rect" color="#777f87" textColor="#ffffff" label="Sifter|Sift ×1" item='gregtech:gt.blockmachines:15542' />
  <FlowEdge from="ammonia" to="enrich" label="6k L" color="#7f8994" dashed="true" labelX="0" labelY="-9" />
  <FlowEdge from="powder" to="enrich" label="6" color="#578d45" dashed="false" labelX="8" labelY="0" />
  <FlowEdge from="enrich" to="enriched" label="6k L" color="#578d45" dashed="false" labelX="0" labelY="-9" />
  <FlowEdge from="enriched" to="react" label="9k L" color="#578d45" dashed="false" labelX="0" labelY="0" />
  <FlowEdge from="powder" to="react" label="9" color="#b47935" dashed="false" labelX="0" labelY="-7" />
  <FlowEdge from="react" to="salt" label="16" color="#b47935" dashed="false" labelX="8" labelY="-5" />
  <FlowEdge from="salt" to="sift" label="16" color="#b47935" dashed="false" labelX="0" labelY="-8" />
  <FlowEdge from="sift" to="powder" label="15" color="#b47935" dashed="false" labelX="7" labelY="-4" />
  <FlowEdge from="react" to="raw" label="2" color="#9b792e" dashed="false" labelX="8" labelY="0" />
  <FlowEdge from="me" to="enriched" label="3k L" color="#7f8994" dashed="true" labelX="19" labelY="0" />
</Flowchart>

The diagram fits its panel initially. Scroll to zoom, drag to pan, and double-click to fit again. Hover over materials for item tooltips. Link quantities describe one round.

The cyclic hatch computes these quantities itself: once configured, it finds a feasible balancing by linear optimization.

**Palladium metallic powder has two destinations, forming two loops**:

- **Precipitation loop** (orange): metallic powder and enriched ammonia undergo **precipitation**, making reprecipitated palladium and palladium salt. **Sifting** returns the salt to metallic powder.
- **Ammonia recycling loop** (green): another portion of metallic powder reacts with ammonia, returning to enriched ammonia for precipitation.

The precipitation and sifting branch consumes 9 metallic powder and returns 15, a gain of 6. **Ammonia recycling gives this growing stock a return path** into enriched ammonia.

The cyclic hatch calculates the run counts of all three patterns and divides the powder between both paths. It keeps net surplus within the configured tolerance, reducing accumulation and waste. Of the 15 powder returned by sifting, 9 go to precipitation and 6 to ammonia recycling. The recycled 6000 L enriched ammonia joins 3000 L supplied upstream to feed precipitation. Each round also consumes 6000 L ammonia; reprecipitated palladium is the net product.

For chance marking, refill modes and deposits, see [Gravel to flint](chance_sifter.md).

## The three patterns

| Pattern | Machine | Inputs | Outputs |
| --- | --- | --- | --- |
| Precipitation | <ItemLink id="gregtech:gt.blockmachines:1169" showIcon="left" />, circuit 9 | 9 <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" />, 9000 L <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> | 16 <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" />, 2 <ItemLink id="bartworks:gt.bwMetaGenerateddust:54" showIcon="left" /> |
| Ammonia recycling | <ItemLink id="gregtech:gt.blockmachines:1169" showIcon="left" />, circuit 1 | 1 <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" />, 1000 L <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"ammonia",DisplayOnly:1b}' showIcon="left" /> | 1000 L <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> |
| Sifting | <ItemLink id="gregtech:gt.blockmachines:15542" showIcon="left" />, chance pattern | 16 <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> | 15 <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> |

<RecipeFor id="bartworks:gt.bwMetaGenerateddust:55" handlerId="gt.recipe.largechemicalreactor" input="bartworks:gt.bwMetaGenerateddust:53" fallbackText="Precipitation recipe: 9 palladium metallic powder and 9000 L palladium enriched ammonia, circuit 9." />

<RecipeUsage id="bartworks:gt.bwMetaGenerateddust:53" handlerId="gt.recipe.largechemicalreactor" output="!bartworks:gt.bwMetaGenerateddust:55" fallbackText="Ammonia recycling recipe: 1 palladium metallic powder and 1000 L ammonia, circuit 1, giving 1000 L palladium enriched ammonia." />

<RecipeFor id="bartworks:gt.bwMetaGenerateddust:53" handlerId="gt.recipe.sifter" fallbackText="Sift recipe: 1 palladium salt rolls palladium metallic powder by chance." />

**Set a chance pattern's output slightly below the actual expected yield**. Each salt yields 0.95 metallic powder on average, so 16 salt have an expected yield of 15.2. Write 15 powder into the sifting pattern.

The cyclic hatch balances against that promise of 15. When a roll falls short, the Chance Pattern Controller first credits stored surplus, then refills the sifter to cover the remaining deficit. Output beyond the promise goes into its deposit for later runs. The published cycle therefore returns a fixed powder amount, while extra output is accounted for separately and reused.

The hatch's balancing runs the ammonia recycling pattern 6 times and the precipitation and sifting patterns once each. The published pattern turns 3000 L enriched ammonia and 6000 L ammonia into 2 <ItemLink id="bartworks:gt.bwMetaGenerateddust:54" showIcon="left" />.

Programming circuits are execution inputs supplied by ME stock or a circuit provider. They are excluded from material balancing, then added to the published inputs according to the run counts: one circuit configured to 9 and six configured to 1 per round. The crafting multiplier scales these quantities as well. After receiving the complete inputs, the hatch applies each circuit configuration to its recipe buffer.

For encoding, provider templates and virtual slots, see [Programmed inputs: circuits and molds](programmed_inputs.md).

## Setup

- <ItemLink id="gregtech:gt.blockmachines:1169" showIcon="left" />: a <ItemLink id="gregtech:gt.blockmachines:22182" showIcon="left" />, an <ItemLink id="gregtech:gt.blockmachines:2710" showIcon="left" /> and an <ItemLink id="gregtech:gt.blockmachines:2713" showIcon="left" />. The enriched ammonia returns to the network through the ME output hatch, where the hatch takes it back as a seed.
- <ItemLink id="gregtech:gt.blockmachines:15542" showIcon="left" />: a <ItemLink id="gregtech:gt.blockmachines:22069" showIcon="left" /> and an ME output bus.
- Both machines' input buffers, output buses and output hatch sit on one ME network, together with a <ItemLink id="programmablehatches:chance_controller" showIcon="left" />.
- Connect an <ItemLink id="gregtech:gt.blockmachines:22048" showIcon="left" /> to the same ME network. Place integrated circuit templates configured to 1 and 9 in it to supply both programming circuits.
- Tools: <ItemLink id="programmablehatches:prog_toolkit" showIcon="left" />, <ItemLink id="programmablehatches:chance_toolkit:1" showIcon="left" /> and <ItemLink id="gregtech:gt.metaitem.01:32708" showIcon="left" />.

Below is the whole line as the in-game test builds it. The scene opens on the overview; press play to see it step by step: ME network, reactor, reactor hatches, sifter, sifter hatches, network link. Fluix ME glass cables connect the hatches to the network. The test uses a self-powered ME controller; supply power to the ME network and both machines when building the line.

<GameScene width="277" height="198" zoom={1.4} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/palladium_line.snbt" formed={true} />
  <ImportPonder src="/assets/palladium_line_ponder.json" />
  <MUI2Gui x="1" y="1" z="7" from="620" to="1480" scale="2">
    <MUI2Click at="680" target="open_patterns" />
    <MUI2Click at="880" target="cyclic_lanes_tab" />
    <MUI2Click at="980" target="cyclic_lane_4" />
    <MUI2Click at="1060" target="cyclic_bind_tab" />
    <MUI2Click at="1180" target="cyclic_bind_1" x="1" y="1" z="2" />
    <MUI2Click at="1260" target="cyclic_lanes_tab" />
    <MUI2Click at="1360" target="cyclic_config_tab" />
  </MUI2Gui>
</GameScene>

After the build sequence, the sifter input hatch is highlighted with a data-stick icon and a left-click prompt to record its coordinates. Once recorded, the main window opens and the demonstration pointer clicks the plus button. The precipitation, sifting and ammonia recycling patterns enter one group, then the pointer changes sifting from lane 0 to blue lane 1, carries a recorded data stick onto its binding slot, and confirms the target. Scrub the timeline to inspect each stage. Hover for tooltips and hold Shift for pattern details. The demonstration controls all window actions.

## Step 1: encode the patterns

1. Carry the Programming Toolkit MK.II in Normal Mode (Shift+right click switches modes) and encode the ammonia recycling and precipitation patterns with NEI's one-click transfer at a pattern terminal. The recipes' circuits are written into the patterns as programming circuits. The cyclic hatch sets the circuit for every run from the programming circuit in its pattern, so both patterns share one reactor.
2. Carry the Chance Pattern Toolkit in the "Marks patterns on encoding" mode and encode the sifting pattern: 16 palladium salt in, 15 palladium metallic powder out. The encoded pattern's tooltip reads "Chance pattern: refilled by a Chance Pattern Controller".

## Step 2: insert the patterns

Open the cyclic hatch and click the <FloatingImage src="gregtech:textures/gui/overlay_button/plus_large.png" x="0" y="0" width="18" height="18" displayWidth="9" wrap="inline" title="Insert Patterns" /> button at the top right to open the pattern window.

Put the three patterns into the first column. The 36 slots of the pattern window are split into four groups in column order, 9 slots each by default, so the first column is group 1. The patterns of a group are analysed as one cycle.

## Step 3: route sifting to the sifter

Lane 0 runs patterns in this hatch. Lanes 1–8 each bind another machine's input hatch.

1. Hold a data stick and left-click the programmable pattern input hatch on the Large Sifter to record its position.
2. Open the machine assignment page. Patterns start on lane 0. Scroll or use the left/right mouse buttons over the sifting pattern to adjust its lane. One left-click selects lane 1 and turns the slot blue. Keep precipitation and ammonia recycling on lane 0.
3. Open the lane bindings page, pick up the recorded data stick and click the lane 1 binding slot. The target icon and connected status appear.

## Step 4: set up the cyclic items

Open the "Balancing" page <FloatingImage src="gregtech:textures/gui/overlay_button/cyclic.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="Balancing" />. Group 1 lists the three cyclic items. The switch at the bottom left of each row is "Output", the one at the bottom right is "Input":

| Cyclic item | Output | Input |
| --- | --- | --- |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="no" /> no | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="no" /> no |
| <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="no" /> no | <FloatingImage src="gregtech:textures/gui/overlay_button/checkmark.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="yes" /> yes |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="no" /> no | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="no" /> no |

- An item with Output set to no is an intermediate; its net surplus in a round is limited by the tolerance.
- An item with Input set to yes has its net consumption in a round written into the published pattern and taken from ME for every craft.
- Reprecipitated palladium is only produced, so it is an output automatically.

Hover a group button or the group title to see the balancings and warnings. Once the settings are done, one balancing shows: 3000 L enriched ammonia and 6000 L ammonia into 2 reprecipitated palladium.

**Keep metallic powder as an internal intermediate with 0% tolerance**. The balancing is precipitation ×1, ammonia recycling ×6 and sifting ×1. The two paths consume 9 + 6 = 15 metallic powder and sifting returns 15, giving zero net consumption and zero surplus. Net inputs are 3000 L enriched ammonia and 6000 L ammonia; the output is 2 reprecipitated palladium.

A 5% powder tolerance produces the same minimum batch for these patterns. Setting it to 0% explicitly closes the powder balance. Excess chance output is kept by the Chance Pattern Controller and credited toward later runs.

## Step 5: put in the seeds

Auto seed capacity is on by default: the targets rise to what the balancing needs, the balancing is published, and AE shows a pattern for reprecipitated palladium. Balancings that run a chance pattern keep seeds by the chance seed multiplier, 4 times one craft's need by default:

| Cyclic item | One craft | Target |
| --- | --- | --- |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> | 15 | 60 |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> | 16 | 64 |
| <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> | 6000 L | 24000 L |

Store these seeds in the ME network and click "Adjust" on the "Config" page <ItemImage id="gregtech:gt.metaitem.02:31032" />; the hatch pulls seeds from ME up to the targets. The seed input slot at the top right of each balancing row takes seeds directly as well. While the seed stock is below one craft's need, a crafting job waits until the stock is filled and then pushes.

> [!TIP]
> The chance seed multiplier is set on the config page, from 1 upwards. More seeds kept means more crafts can run at the same time.

## Step 6: order

Order reprecipitated palladium in AE. Every craft takes 3000 L enriched ammonia, 6000 L ammonia, one programming circuit configured to 9 and six configured to 1 from ME.

1. The hatch takes one round of cyclic items from its seed stock and, together with the inputs AE sends, hands them to the reactor and the sifter by the balancing.
2. When the products enter ME, the hatch takes back the round's seeds and the reprecipitated palladium goes to AE's crafting job.
3. When the sifter makes fewer than 15 palladium metallic powder, the Chance Pattern Controller takes palladium salt from ME and refills the sifter until 15 are produced. The controller refills from storage by default, so keep some palladium salt in ME.
4. Extra palladium metallic powder is stored in the Chance Pattern Controller and pays the expected amount of later rounds. "Return deposits" in the controller's screen puts the deposits back into ME.

## Growth patterns

A pattern that only raises cyclic items is a growth pattern. A balancing with growth patterns runs in order: every craft pushes its growth patterns first and the other patterns once the cyclic items the growth patterns make are back, so one craft needs only the seeds the growth patterns take.

"Growth" on the config page sets the growth priority, from 0 to 100%, 50% by default:

- Growth target = growth priority × the cyclic items the queued other patterns consume.
- While the growth output kept is below the growth target, growth patterns are pushed first; at the growth target, the other patterns are.
- Above 0, seeds for one craft are enough and the hatch keeps accepting crafts of such balancings; the part of the growth target above the seed target is borrowed from ME and returned once the crafts are done.
- At 0, the hatch accepts crafts as its seed stock covers them.

## Checks

Hover a group button on the "Balancing" page for the group's balancings and warnings:

- **Seed need exceeds target, not published**: turn on auto seed capacity, or raise the target.
- **Stock is below need**: click "Adjust", or put seeds into the seed input slots.
- **Lane needs a usable target hatch**: bind the lane again with a data stick.
- **These patterns cannot form a cycle that yields it**: check the Output and Input settings of the cyclic items.
