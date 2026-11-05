---
navigation:
  title: "Chance patterns: gravel to flint"
  parent: index.md
  icon: programmablehatches:chance_controller
  keywords:
    - chance pattern
    - refill
    - sifter
    - gravel
    - flint
    - deposit
item_ids:
  - "<programmablehatches:chance_controller>"
  - "<programmablehatches:chance_toolkit:0>"
  - "<programmablehatches:chance_toolkit:1>"
  - "<gregtech:gt.blockmachines:15542>"
---

# Chance patterns: gravel to flint

Sift <ItemLink id="minecraft:gravel" showIcon="left" /> in a <ItemLink id="gregtech:gt.blockmachines:15542" showIcon="left" />. A <ItemLink id="programmablehatches:chance_controller" showIcon="left" /> replenishes inputs until the chance pattern's promised flint output is fulfilled. Surplus flint becomes a deposit for later shortfalls.

## Recipe probabilities and pattern amounts

<RecipeFor id="minecraft:flint" handlerId="gt.recipe.sifter" input="minecraft:gravel" fallbackText="One gravel has six flint output slots at 100%, 90%, 80%, 60%, 33% and 25%." />

The six output slots roll at 100%, 90%, 80%, 60%, 33% and 25%. The mean is 3.88 flint per gravel, or 15.52 flint for four gravel. Encode this tutorial's processing pattern as:

4 gravel → 15 flint

The promised output is slightly below the recipe's actual expectation. A short batch receives extra inputs for further processing. Excess output is stored as a deposit and used first for later shortfalls.

## Preparation and connections

- Fit the Large Sifter with a <ItemLink id="gregtech:gt.blockmachines:22069" showIcon="left" /> and an ME output bus, plus the energy, maintenance and muffler hatches required by its structure.
- Connect the pattern hatch, output bus and Chance Pattern Controller to the same powered ME network with enough channels.
- Add a crafting CPU, storage cell and a supply of gravel. Refills use gravel beyond the initial pattern inputs.
- Carry <ItemLink id="programmablehatches:chance_toolkit:1" showIcon="left" />. Shift-right-click to select the mode that marks patterns during encoding.

## Encode and install the pattern

1. Create a processing pattern in an ME Pattern Terminal with 4 gravel as input and 15 flint as output. Encode while carrying the enabled Chance Pattern Toolkit.
2. Check the pattern tooltip for its chance-pattern marker.
3. Open the pattern hatch, click + to open its pattern window, and insert the pattern.
4. Order 15 flint from ME. AE sends four gravel to the hatch and books 15 flint as expected output.

## Demonstration: shortfall, refill and deposit

The scene replays a recorded production sequence. Input refills are initially paused so the shortfall is visible. The controller GUI then selects the stock-only mode and clicks Apply. Newly placed controllers use stock-only mode by default.

Play the scene to see the pattern window, recipe buffer and controller GUI. Watch the mode-button clicks, expected quantity, accumulated refills and deposits. Hover over items and buttons for tooltips. Each batch in actual gameplay rolls its own output.

<GameScene width="277" height="198" zoom={1.4} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/chance_sifter.snbt" formed={true} />
  <ImportPonder src="/assets/chance_sifter_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="2" from="200" to="360" scale="2" showMain="true">
    <MUI2Click at="240" target="open_patterns" />
  </MUI2Gui>
  <MUI2Gui x="1" y="1" z="2" from="360" to="540" scale="2" showMain="true">
    <MUI2Click at="380" target="open_buffer_0" />
  </MUI2Gui>
  <MUI2Gui x="0" y="1" z="3" from="550" to="1020" scale="2" showMain="true">
    <MUI2Click at="640" target="chance_mode" />
    <MUI2Click at="660" target="chance_apply" />
  </MUI2Gui>
</GameScene>

The demonstration uses a self-powered ME controller, debug energy hatch and creative maintenance hatch. Supply ordinary power and maintenance when building the machine for gameplay.

Refills use the machine's actual input amount per run. One run consumes one gravel here, so a one-gravel refill is recorded as 0.25 pattern copies and 3.75 promised flint.

## Reading the controller

| Field | Meaning |
| --- | --- |
| Expected quantity | Output still owed by the relevant chance buffers |
| Deposit | Surplus already stored in the controller for later shortfalls |
| Pending | Surplus already recorded and on its way back to ME |
| Output at pattern rate | Executed runs converted using the pattern's promised yield |
| Actual output | Total output rolled by the machine |
| Expected output at recipe rate | Mean output calculated from the real recipe probabilities |
| Accumulated refills | Additional inputs expressed as pattern copies and promised output |

Surplus flint is kept in the controller. When a chance buffer needs more flint, the controller releases deposits to waiting crafting jobs before replenishing the remaining inputs. Use Return deposits to put these stored outputs back into ordinary ME storage.

## Refill modes

| Mode | Input source |
| --- | --- |
| Off | Deposits still pay shortfalls; the player supplies inputs |
| Stock only | Extract gravel from ME storage; used in this tutorial |
| Craft only | Order inputs from the ME crafting system and refill after enough arrive |
| Stock, then craft | Use stock first and order the remainder |

Click Apply after choosing a mode. Crafting modes need input recipes and an idle crafting CPU for the replenishment orders.

## Configuration checks

- The marked chance pattern is in the pattern hatch's pattern slots.
- The controller, input hatch and output bus are on the same ME network.
- The output bus returns actual output to ME for the crafting CPU and surplus collection.
- Stock-only mode has extra gravel available; crafting modes have input recipes and an idle CPU.
- The machine is formed, powered and maintained.

Continue with the [palladium cycle](cyclic_patterns.md) to use the same refill mechanism for cyclic recipes with chance outputs.
