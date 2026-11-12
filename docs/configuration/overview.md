---
title: Settings
description: Keys in config.yml.
sidebar:
  order: 1
---

`config.yml` holds the shared settings, the GUI layout, sounds, and messages. Type files and `prices.yml` are separate.

When a reload adds keys that your file is missing, RexMinions copies `config.yml` to `config.yml.bak.<timestamp>` first. Values you already set stay as they are. Type files and `prices.yml` are created only when the file is missing, and reload does not overwrite them.

## settings

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | When false, placed minions stay, but they do not act |
| `default-limit` | `5` | Used when the player has no `rexminions.limit.<n>` node, and when the highest node is `0` |
| `actions-per-tick` | `4` | How many kills, breaks, pickups, catches, harvests, sales, or log breaks one minion may do on its tick. Clamped to 0-64 |
| `blocks-per-tick` | `512` | How many block positions one minion inspects on its tick. The scan continues next time |
| `link-distance` | `8` | How far a chest, trapped chest, or barrel may be. Values above 32 are clamped |
| `storage-size` | `27` | Slots in the minion's own storage. Rounded down to a multiple of 9, from 9 to 54 |
| `replant` | `true` | Farmer resets a mature crop instead of breaking it |
| `use-durability` | `true` | Damage the GUI tool once on a tick that did work. Unbreaking can skip a hit |
| `fortune` | `true` | When false, Fortune on the tool is ignored |
| `looting` | `true` | When false, Looting on the tool is ignored |
| `efficiency-ticks` | `4` | Ticks removed from the level period for each Efficiency level |
| `minimum-ticks` | `10` | The period never goes below this |
| `entity` | `armor_stand` | `armor_stand` or `marker`. A marker has no helmet slot |
| `small-armor-stand` | `false` | Spawn a small armor stand |
| `show-name` | `true` | Show the type name and level |
| `currency` | `EMERALD` | Item paid when the owner collects seller balance. 1 balance = 1 item |

## gui

`gui.rows` is 1 to 6. Slots are `info`, `tool`, `link`, `storage`, `balance`, and `pickup`. A slot outside the inventory is moved onto the last slot, so keep the numbers inside the row count.

The tool slot is the only slot in the main GUI that accepts an item. Shift-click from your inventory into that GUI is cancelled so filler items are not replaced by accident.

## sounds

`sounds.place`, `sounds.pickup`, `sounds.error`, and `sounds.link` take Bukkit sound names such as `BLOCK_NOTE_BLOCK_PLING`. A blank value plays nothing. `{player}` and `{type}` are replaced before the name is resolved. Volume and pitch are `sounds.volume` and `sounds.pitch`.
