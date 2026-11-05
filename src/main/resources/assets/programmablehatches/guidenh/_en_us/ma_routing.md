---
navigation:
  title: "Remote Molecular Assembly: P2P, Conduits and Interfaces"
  parent: index.md
  icon: programmablehatches:proghatches.part.ma.p2p
  keywords:
    - "Remote Molecular Assembly: P2P, Conduits and Interfaces"
    - "分子装配"
    - "P2P"
    - "MA模式"
    - "分子装配导管"
    - "Molecular Assembler"
item_ids:
  - "<programmablehatches:proghatches.part.ma.p2p>"
  - "<programmablehatches:proghatches.ma_iface>"
---

# Remote Molecular Assembly: P2P, Conduits and Interfaces

MA routing transfers molecular-assembly requests. It allows pattern interfaces and executing devices to be placed separately.

## Link a remote assembler with MA P2P

1. Place an MA P2P input beside the pattern interface.
2. Place its output beside a remote Molecular Assembler.
3. Bind the pair with a memory card and supply channels and power.
4. Install a normal crafting-table pattern in the interface.
5. Order a small amount and observe remote assembly and return to the original network.

Begin with a crafting-table recipe, then expand the interface and assembler arrangement. Molecular-assembly conduits provide another request-routing option; inspect their endpoints and device orientation.

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="Interface" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="MA route" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="Assembler" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## Deliver requests to an ordinary container

A Molecular Assembler Interface receives MA requests on one side and transfers ingredients into the container opposite. Verify the request face and target container, and provide a separate return route.

This interface handles patterns containing at most one fluid. For multiple fluids, use an appropriate pattern input hatch.

## MA mode on dual input hatches

A hatch supporting MA mode receives pattern ingredients directly from a normal ME Interface. Use a normal interface and arrange neighboring containers according to the mode's instructions so the interface selects the intended request receiver.

Normal logistics and MA mode deliver ingredients through different paths. Inspect where one small order arrives before increasing production.
