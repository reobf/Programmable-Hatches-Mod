---
navigation:
  title: "Recipe-Map Input Hatch: Publish Machine Recipes"
  parent: index.md
  icon: gregtech:gt.blockmachines:22180
  keywords:
    - "Recipe-Map Input Hatch: Publish Machine Recipes"
    - "配方表输入总成"
    - "自动生成样板"
    - "Recipe Map"
item_ids:
  - "<gregtech:gt.blockmachines:22180>"
---

# Recipe-Map Input Hatch: Publish Machine Recipes

The recipe-map input hatch generates processing patterns from a marked GT machine's recipe map. It is useful for processing stations with many recipes and a shared configuration.

## Start with one machine

1. Build the multiblock, install the hatch, and provide power, maintenance and output.
2. Hold a GT machine item representing the desired recipe map and right-click the hatch to mark it.
3. Wait for generation and check the reported pattern count. Use a screwdriver to switch between available recipe maps.
4. Connect the hatch to ME storage and a crafting CPU, then inspect the craftable outputs in a terminal.
5. Order one easy-to-check operation and verify ingredients, programming circuits and returned outputs.

The held machine selects the recipe map. The actual multiblock containing the hatch still performs processing and must satisfy voltage, heat and other recipe requirements.

## Choose which recipes to publish

Right-click the hatch with a supported wireless terminal to bind it. In the recipe list, click unwanted patterns to exclude them. When several recipes produce the same item, select routes suited to your factory's available ingredients.

Wildcard damage inputs expand into existing subtypes, up to 32 generated patterns per original recipe. Check that the needed subtype appears in the resulting list.

## Programming circuits and fluids

Zero-count, non-consumed recipe inputs become programming circuits. Supply them through [ordinary or large providers](large_provider.md), retaining the real templates where required.

Fluid ingredients and products are published as fluid stacks. Store the ingredients in accessible ME storage and return outputs to the ordering network.

## Begin with guaranteed outputs

The generator includes guaranteed item outputs and fluid outputs. Configure randomized item production separately using the [chance-pattern guide](chance_sifter.md), with a deliberate promised output amount.

After changing the marked machine or recipe map, recheck generated patterns, exclusions and the processing machine. Validate a small order before scaling up.
