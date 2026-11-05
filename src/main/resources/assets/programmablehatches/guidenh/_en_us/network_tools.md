---
navigation:
  title: "Small Subnets and Channel-over-Wire"
  parent: index.md
  icon: programmablehatches:proghatches.exciter
  keywords:
    - "Small Subnets and Channel-over-Wire"
    - "线传频道"
    - "CoW"
    - "自组网络激励器"
    - "ME覆盖板"
    - "Subnet Exciter"
item_ids:
  - "<programmablehatches:proghatches.exciter>"
  - "<programmablehatches:proghatches.part.cow>"
  - "<programmablehatches:proghatches.cover:32>"
  - "<programmablehatches:proghatches.cover:33>"
  - "<programmablehatches:proghatches.cover:34>"
  - "<programmablehatches:proghatches.cover:35>"
  - "<programmablehatches:proghatches.cover:37>"
---

# Small Subnets and Channel-over-Wire

A Subnet Exciter powers a compatible small AE network. Channel-over-Wire panels connect two ME endpoints through GT wiring. They are useful for compact, distributed setups.

## Build a small independent subnet

1. Combine storage buses, interfaces and a few cables without an ME Controller.
2. Install a Subnet Exciter.
3. Check that nodes are online and channel demand fits a controller-free network.
4. Test actual storage access before adding the processing machine.

The exciter requires a network without a controller and does not export power. Energy cells, chargers and quartz fiber are incompatible with its operating conditions. Recheck network composition when expanding.

## Carry channels through GT wire

1. Install a Channel-over-Wire panel at each ME endpoint.
2. Connect the panels through GT wiring.
3. Bind the two endpoints with a memory card.
4. Check device connectivity and channel usage.

A pair carries up to 32 channels. A GT wire network supports at most two panels with the same frequency. Give independent links their own frequencies and endpoint pairs. Panels also fit on dense cables.

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="ME end" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="GT wire" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="ME end" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## Compact cover connections

An ME Interface cover puts interface functionality on a GT block face; its dual version provides the corresponding fluid support. P2P interface covers are output endpoints bound with a memory card. An ME cover passes channels through the hatch, within its advertised capacity.

Validate a small order with one interface and one machine before scaling. Covers, P2P routing and subnet storage boundaries serve separate roles, so inspect the route one segment at a time.
