---
title: Levels and limits
description: How level stats and place limits are chosen.
sidebar:
  order: 1
---

## Level

The level is stored on the minion item. `/rexminions give <player> <type> [level] [amount]` writes it. Level 1 is used when you omit the number.

The type file lists the stats:

```yaml
# range is in blocks. Hard cap is 32.
levels:
  1:
    speed-ticks: 80
    range: 4
  2:
    speed-ticks: 60
    range: 6
```

`speed-ticks` is the base wait between actions. Efficiency on the tool subtracts `settings.efficiency-ticks` for each level of the enchant, and the result never goes below `settings.minimum-ticks`.

`range` is a radius in blocks. Any configured range above 32 is treated as 32. A level number that is not in the file uses the closest lower entry. If the player is below every listed level, the lowest entry is used.

The tool itself is not the level. Put a pickaxe, sword, axe, or other item in the GUI tool slot. The minion item only chooses the type and the level.

`settings.use-durability` damages that tool once on a tick that did work. At zero remaining durability the tool breaks and the slot is cleared. Unbreaking can skip a hit.

## Limit

A player may place minions up to the highest `rexminions.limit.<n>` node they have.

| Nodes | Limit |
|---|---|
| none | `settings.default-limit` |
| `rexminions.limit.0` only | `settings.default-limit` |
| `rexminions.limit.3` and `rexminions.limit.8` | 8 |
| `rexminions.limit.0` and `rexminions.limit.6` | 6 |

The check reads permissions that are actually granted. It does not walk every number and ask the server if an operator has it.

`rexminions.admin` does not raise the limit by itself. Give that account a `rexminions.limit.<n>` node when you want a higher cap. Admins can still open and pick up someone else's minion.
