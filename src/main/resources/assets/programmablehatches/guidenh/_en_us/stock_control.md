---
navigation:
  title: "Stock Control: Maintain Supplies and Control Machines"
  parent: index.md
  icon: programmablehatches:amountmaintainer
  keywords:
    - "Stock Control: Maintain Supplies and Control Machines"
    - "数量维持总线"
    - "发信控制覆盖板"
    - "限制存储输入"
    - "优先级过滤"
    - "Amount Maintainer"
    - "restricted"
item_ids:
  - "<programmablehatches:amountmaintainer>"
  - "<programmablehatches:proghatches.cover:100>"
  - "<gregtech:gt.blockmachines:22163>"
  - "<gregtech:gt.blockmachines:22164>"
  - "<gregtech:gt.blockmachines:22173>"
  - "<gregtech:gt.blockmachines:22174>"
---

# Stock Control: Maintain Supplies and Control Machines

An Amount Maintainer keeps a selected item or fluid in an ordinary container at a target amount. A level-control cover switches a machine according to ME stock, while restricted stocking inputs limit the materials visible to recipe processing.

## Example: keep 64 iron ingots in a chest

1. Attach an Amount Maintainer to a chest and connect it to ME containing iron ingots.
2. Mark iron in the configuration slot and set the target to 64.
3. Remove 16 ingots from the chest and observe replenishment back to 64.
4. Put another item in the chest: it remains untouched because the maintainer adjusts only the selected type.
5. Set an update interval appropriate to the required transfer rate.

For fluids, mark a filled container and select fluid interpretation. Item interpretation maintains the container item itself.

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="ME stock" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="Maintain" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="Container" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## Recorded target changes, refill and return

The playback opens the Amount Maintainer GUI and changes its target from 16 to 64. It refills after 16 iron are removed, then returns an extra 16 iron to ME. Three gold ingots remain untouched throughout.

<GameScene width="277" height="198" zoom={1.6} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/stock_control.snbt" formed={true} />
  <ImportPonder src="/assets/stock_control_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="1" part="EAST" from="180" to="340" scale="2" showMain="true">
    <MUI2Click at="220" target="maintainer_amount" value="64" />
  </MUI2Gui>
</GameScene>

## Craft missing stock

Install a crafting card, provide a recipe for the target material and leave a CPU available. Test with a small target amount so that you can observe crafting, return to ME and delivery to the container.

A redstone card adds always-on, always-off, normal, inverted and edge-triggered control. A memory card bound to a redstone P2P can select a remote signal. Check both the maintain/clear setting and behavior when the P2P is offline.

## Control a machine from ME stock

Install a level-control cover on the machine and configure the material, threshold and comparison direction. It reads the machine's own ME network or the network available through an ME cover.

For common metal plates, allow processing below the target stock and stop further production after the target is reached. Change a small amount of stock first to verify the chosen comparison.

## Limit recipe-visible input amounts

Restricted stocking buses and hatches provide lower and upper bounds plus a multiples mode. The lower bound waits for enough material; the upper bound limits the visible amount. Multiples modes round down to an integer multiple or power-of-two multiple of the lower bound.

For a recipe needing four items, set a lower bound of four and inspect results with stocks of 3, 4, 7 and 8. The refresh button previews the current result; processing queries storage again when it runs.

Priority-filtered stocking inputs restrict retrieval to a selected storage priority. Assign distinct priorities to subnet storage buses and select the required priority at the input.
