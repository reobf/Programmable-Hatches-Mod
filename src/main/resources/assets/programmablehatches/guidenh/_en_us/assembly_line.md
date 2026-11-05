---
navigation:
  title: "Assembly-Line Supply: Ingredient Distribution and Repeated Inputs"
  parent: index.md
  icon: gregtech:gt.blockmachines:22142
  keywords:
    - "Assembly-Line Supply: Ingredient Distribution and Repeated Inputs"
    - "原料分配器"
    - "诱骗输入总线"
    - "装配线"
    - "状态总线"
    - "Ingredient Distributor"
    - "decoy"
item_ids:
  - "<gregtech:gt.blockmachines:22142>"
  - "<gregtech:gt.blockmachines:22170>"
  - "<gregtech:gt.blockmachines:22171>"
  - "<gregtech:gt.blockmachines:22172>"
  - "<gregtech:gt.blockmachines:22173>"
  - "<gregtech:gt.blockmachines:22174>"
---

# Assembly-Line Supply: Ingredient Distribution and Repeated Inputs

An Ingredient Distributor sends one input set to multiple output buses and hatches in order. Assembly-line positions matter: when the same plate appears at several input positions, preserve each position and amount.

## Observe a real distribution

The recording loads 2 iron ingots, 3 gold ingots and 5 iron ingots in separate slots, plus 1000 L water and 500 L lava. The three item outputs receive 2 iron, 3 gold and 5 iron in that order; the two fluid outputs receive water and lava.

<GameScene width="277" height="198" zoom={1.4} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/ingredient_distributor.snbt" formed={true} />
  <ImportPonder src="/assets/ingredient_distributor_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="4" y="0" z="1" from="440" to="600" scale="2" showMain="true" />
  <MUI2Gui x="3" y="0" z="1" from="600" to="680" scale="2" showMain="true" />
  <MUI2Gui x="2" y="0" z="1" from="680" to="760" scale="2" showMain="true" />
  <MUI2Gui x="1" y="0" z="1" from="760" to="840" scale="2" showMain="true" />
</GameScene>

This scene verifies ingredient distribution using ordinary output components so their contents are easy to inspect. For an actual assembly line, connect each output to the corresponding recipe-input position.

## Build in stages

1. Follow the blueprint for the head, with the controller, energy, maintenance and one input bus, hatch or dual input.
2. Add output slices. Each accepts at most one item output bus and one fluid output hatch.
3. Complete the end frames and check the structure.
4. Arrange materials in the input according to the target recipe positions.
5. Starting nearest the head, check every item and fluid destination.

A base operation uses 120 EU/t for five seconds and supports overclocking. The recording uses debug power and creative maintenance to speed up verification.

## Blocking mode and ME subnets

Use terbium for every frame and ME versions for every output bus and hatch to enable blocking mode. Connect each output position to its corresponding material subnet.

Blocking checks output-component buffers and network contents: each must be empty or contain only the matching material, with enough room for the complete transfer. This supports batch-oriented assembly-line supply.

When transfer waits, inspect the numbered output network, internal buffer and available capacity shown by the GUI. Validate one set before increasing batch size.

## Repeated materials and decoy stocking inputs

When independent subnets supply the same item at several assembly-line positions, ordinary ME input-bus entries can overwrite each other. The amount in the last subnet does not represent the total across all positions.

For this topology:

1. Supply each position from a separate subnet and enable automatic retrieval on its input bus.
2. Use a decoy stocking input bus at the final position, with storage buses accessing all material subnets.
3. Assign subnet priorities in position order, with the final position highest.
4. Check the reserved-first-slot option so the highest-priority subnet corresponds to that final input position.
5. Use a small recipe with repeated ingredients and verify deductions in every subnet.

The decoy fluid hatch provides the corresponding fluid handling. Priority-filtered stocking inputs can restrict other positions to the intended storage priority.

## Coordinate multiple distributors

Communication ports coordinate occupation, yielding and recipe locking between distributors. Establish a working single-machine blocking setup before adding the ports and their redstone state connection.

Observe idle, acquiring, locked and yielded states. Other machines should wait while the current batch owns an output set, then proceed after those outputs are empty.
