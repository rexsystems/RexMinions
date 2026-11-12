---
title: RexMinions
description: Placeable minions for Paper, Purpur, and Folia.
---

RexMinions adds seven placeable workers. You place a minion item, and an armor stand or a marker stays there and repeats one job.

Each minion schedules its own task at its block. It does nothing while that chunk is unloaded. A click is cancelled only when the item in the hand is a Rex minion item, or the entity you click is a Rex minion.

Messages use MiniMessage. Legacy `&` codes, `&#RRGGBB`, and `#RRGGBB` also work.

## Files

```
plugins/RexMinions/
  config.yml
  prices.yml
  minions.db
  types/
    slayer.yml
    miner.yml
    collector.yml
    fisher.yml
    farmer.yml
    seller.yml
    lumber.yml
```

A type file is written from the jar only when that file is missing. Reload reads the files again and does not spawn a second copy of a minion that is already placed.

## Next

- [Installation](/rexminions/getting-started/installation/)
- [Minion types](/rexminions/configuration/minion-types/)
- [Levels and limits](/rexminions/guides/levels-and-limits/)
