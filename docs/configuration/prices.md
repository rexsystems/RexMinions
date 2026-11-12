---
title: Prices
description: How the seller prices items.
sidebar:
  order: 4
---

`prices.yml` is a map of material names to a price per item:

```yaml
prices:
  COBBLESTONE: 0.1
  DIAMOND: 40.0
```

A missing material is not sold. A price of 0 or below is ignored.

The seller does not call Vault or any other plugin. It removes a priced stack from the linked container, or from the minion's own storage when no container is linked, and adds `price * amount` to that minion's balance.

The owner clicks the balance button in the GUI. Each whole number becomes one `settings.currency` item. The fraction stays on the minion. One click pays at most 64 items.
