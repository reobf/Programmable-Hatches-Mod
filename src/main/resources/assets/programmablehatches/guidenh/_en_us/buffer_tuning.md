---
navigation:
  title: "Buffers, Pattern Multiplication and Large Inputs"
  parent: index.md
  icon: gregtech:gt.blockmachines:22179
  keywords:
    - "Buffers, Pattern Multiplication and Large Inputs"
item_ids:
  - "<gregtech:gt.blockmachines:22179>"
---

# Buffers, Pattern Multiplication and Large Inputs

A buffered hatch classifies materials from its staging area into internal buffers and records one input set for each buffer. Later inputs can join when they form whole copies of that record. This record and the machine recipe map's minimum recipe are separate checks.

## Inspect staging and internal buffers

1. Deliver one previously verified processing pattern.
2. Inspect the external input area in the main GUI.
3. Open the internal buffer and inspect its recorded materials, current amounts and lock state.
4. Submit the same set again and observe it joining the matching buffer.
5. Submit another recipe and inspect its separate buffer.

Keep each input set's order and quantities consistent. Compare the pattern, staging area and buffer record when diagnosing a problem.

## Whole multiples of the recorded input

Suppose the first recorded set is A×2 and B×3:

| Later input | Relationship |
| --- | --- |
| A×2, B×3 | One copy |
| A×4, B×6 | Two copies |
| A×8, B×12 | Four copies |
| A×4, B×5 | Not a whole number of recorded copies |

The initial batch size matters. A multiplied first delivery becomes the basis of the record. Complete the old batch and inspect the record before testing a changed pattern multiplier.

To establish a new record in an ordinary buffer, finish or recover its materials and switch the lock icon to unlocked. Wait for the empty buffer record to clear before delivering the new complete batch.

Chance refill and cyclic buffers have additional quantity rules; use the [chance-pattern](chance_sifter.md) and [cyclic-pattern](cyclic_patterns.md) guides for those states.

## Multiplication and optimization

Pattern settings control whether optimization is allowed and whether it changes a multiplier or the pattern itself. Expand the resulting pattern tooltip, verify every input and output amount, and test one submission.

Treat programming circuits separately from consumed ingredients. If multiplication also increases circuit counts, use the applicable normalization mode to return them to one where appropriate; see [tools and upgrades](tools_upgrades.md).

## Buffer priority and queues

Buffer-order settings prioritize the largest or smallest recorded batch count. Queue one larger and one smaller batch and observe selection. Mirrors can follow the master or use their supported order setting.

Merging equivalent buffers changes their presentation to the controller. Elastic buffering adds temporary queue space. Test each setting with small batches before increasing orders.

## Quantities beyond ordinary integer limits

Internal storage, GUI display and the multiblock controller's supported input range are separate layers. Large-quantity storage does not by itself expand every controller's capabilities.

Use capacity-limit options to stay within the target machine's supported range. A new input is checked as a complete batch rather than relying on automatic splitting into int.max-sized slots to establish a record. Compare the record, delivered batch and limit settings when an input is rejected or reports a non-integer multiple.

Also verify remaining capacity during manual transfer. With capacity 100, stock 40 and a cursor stack of 64, the expected transfer is 60 inserted and four left on the cursor. Check exact GUI amounts, save and reopen.
