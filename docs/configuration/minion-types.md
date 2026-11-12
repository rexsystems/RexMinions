---
title: Minion types
description: The seven type files under plugins/RexMinions/types.
sidebar:
  order: 2
---

Each type has its own file. The id is the file name without `.yml`.

| Id | Job |
|---|---|
| `slayer` | Kills hostile mobs in range. Tamed mobs are skipped. Looting on the tool adds bonus drops when `settings.looting` is true |
| `miner` | Breaks blocks whose material is listed under `generators` |
| `collector` | Picks up nearby item entities into the linked container, then into its own storage |
| `fisher` | Catches a fish when it sees water in range |
| `farmer` | Harvests mature crops. When `settings.replant` is true, the plant stays and the age resets |
| `seller` | Sells items from the linked container, or from its own storage, using `prices.yml` |
| `lumber` | Breaks logs that connect to leaves. A log pile with no leaves is left alone |

## Shared keys

```yaml
enabled: true
name: Miner
item-name: "<gray>Miner Minion"
material: IRON_PICKAXE
helmet: IRON_HELMET
chestplate: LEATHER_CHESTPLATE
leggings: ""
boots: ""
lore:
  - "<gray>Level {level}"
# range is in blocks. Hard cap is 32.
levels:
  1:
    speed-ticks: 60
    range: 3
```

`name` is the plain word used in chat. `item-name` and `lore` are what the item shows. `{level}`, `{range}`, `{speed}`, and `{type}` are filled in when the item is created.

`material` is the item you place. `helmet`, `chestplate`, `leggings`, and `boots` go on the armor stand. Leave a piece blank to skip it. A marker entity does not wear armor.

`levels` maps a level number to `speed-ticks` and `range`. Range is in blocks. The hard cap is 32, including a value written higher than that. A level that is not listed uses the closest lower level, then the lowest level in the file.

## Miner

`generators` is the material list the miner is allowed to break. Cobblestone, stone, and the other defaults are generator blocks, not every ore in the world.

## Lumber

`leave-bottom: true` keeps the lowest log in the tree. `replant: true` places a sapling when that bottom log is actually broken. With `leave-bottom` on, the stump stays and no sapling is added.

The lumber minion only breaks logs that are part of a tree. It treats a connected group as a tree when a leaf block touches one of those logs.

## Farmer

Replant is `settings.replant` in `config.yml`, not a key in `farmer.yml`. Melons and pumpkins are broken as fruit. The stem stays.
