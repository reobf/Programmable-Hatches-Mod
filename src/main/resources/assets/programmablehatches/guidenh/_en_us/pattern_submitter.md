---
navigation:
  title: "Pattern Submitter: Repeat a Sequence of Orders"
  parent: index.md
  icon: programmablehatches:proghatches.submitter
  keywords:
    - "Pattern Submitter: Repeat a Sequence of Orders"
    - "循环样板提交器"
    - "重复下单"
    - "Pattern Submitter"
item_ids:
  - "<programmablehatches:proghatches.submitter>"
---

# Pattern Submitter: Repeat a Sequence of Orders

The pattern submitter dispatches pattern tasks in sequence, which suits repeated requests for several products. [Cyclic pattern input hatches](cyclic_patterns.md) handle cycle balancing and seeds; this page covers the submitter's order controls.

## Start with two small orders

1. Connect the submitter to ME with ingredients, recipes and crafting CPUs.
2. Insert two patterns whose outputs are easy to distinguish and select the submission-quantity mode.
3. Configure redstone control and enable submission.
4. Watch the GUI cursor, idle/failed/submitted state and CPU jobs.
5. Read the current pattern index with a comparator and verify order.

Validate one round with a small material supply, then increase repetition after outputs return correctly.

## Continue after a failed submission

Retry suits a sequence that must remain complete and ordered. Skip-failed-pattern suits independent products where another order may proceed first.

Inspect ingredients, CPU availability and recipe feasibility before choosing how the sequence should continue.

## Wait for products or cancel after dispatch

Normally retain tracking until products finish. The cancel-after-dispatch option is for workflows that deliberately need that dispatch behavior. Confirm where ingredients go, where products are stored and when another submission is permitted before enabling it.

The alternate power-button action stops submission and moves the cursor while abandoning tracking of the current crafting task. Inspect existing CPU jobs before moving the cursor.

## Related workflows

Use [Amount Maintainers](stock_control.md) for stock-driven replenishment and the [palladium cycle](cyclic_patterns.md) for intermediate balancing and chance refill.
