---
navigation:
  title: "Crafting Throughput: Dumpers, Interceptors and Multiblock CPUs"
  parent: index.md
  icon: programmablehatches:proghatches.craftingdumper.0
  keywords:
    - "Crafting Throughput: Dumpers, Interceptors and Multiblock CPUs"
    - "转储器"
    - "提取请求拦截器"
    - "多方块CPU"
    - "crafting dumper"
    - "CPU"
item_ids:
  - "<programmablehatches:proghatches.craftingdumper.0>"
  - "<programmablehatches:circuit_interceptor:0>"
  - "<programmablehatches:circuit_interceptor:1>"
  - "<gregtech:gt.blockmachines:22090>"
---

# Crafting Throughput: Dumpers, Interceptors and Multiblock CPUs

Identify whether a crafting bottleneck is ingredient dispatch, programming-circuit supply or the processing machine itself before choosing a throughput device.

## Crafting dumpers

A crafting dumper adds retries after a successful pattern dispatch without consuming additional co-processor operations for those retries. It suits receivers with enough buffer capacity to accept multiple input sets.

1. Use the appropriate dumper tier in the crafting CPU structure.
2. Prepare a stable processing pattern and an input hatch capable of buffering several sets.
3. Order a batch and compare how quickly the receiver fills.
4. Check that processing and output keep up.

The dumper improves dispatch throughput. Recipe execution still depends on the actual machine's power, parallelism and operating state.

## Large-provider amount and upgrade chip

For high programming-circuit demand, configure co-processors and amount per operation using the [large-provider guide](large_provider.md). Its upgrade chip enables instant-completion support while real templates stay in their providers.

Test supply and processing separately: request circuits directly, then request a product requiring them, to locate the waiting stage.

## CPU extraction request interceptors

The programming-circuit interceptor prevents CPUs from using stored programming circuits, directing those requirements through providers. The general version lets you select the intercepted item.

Connect it to the relevant network and inspect a small crafting plan. Provide a working crafting source for the intercepted material; existing inventory alone does not mean the CPU will use it.

## Multiblock crafting CPU

This multiblock allocates CPUs using crafting-storage components in its input bus. Storage is allocated on task submission and returned to the output bus after completion; singularity storage follows its special reusable behavior.

1. Complete the blueprint and provide power and maintenance.
2. Insert crafting-storage components and co-processors into the input bus.
3. Reserve output space and submit a small crafting job.
4. Observe allocation, execution and storage return.
5. Use the screwdriver operation described by the device to retrieve co-processors.

Allocated CPUs share the multiblock's co-processors. Observe two small concurrent jobs before configuring larger workloads.
