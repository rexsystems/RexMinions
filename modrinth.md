# RexMinions

Placeable minions for Paper, Purpur, and Folia. A minion works in its own loaded chunk, stores what it gathers, and can sell from `prices.yml`.

**Java 21. Paper, Purpur, or Folia 1.21+.** PlaceholderAPI and WorldGuard are optional.

## Features

- Slayer, miner, collector, fisher, farmer, seller, and lumber, each with its own type file
- Armor stand or marker entity, with the helmet and armor from the type file
- Tool slot. Efficiency changes the period. Fortune and Looting can be turned off in `config.yml`
- Link a chest within `settings.link-distance`, or use the minion's own storage
- Seller reads `prices.yml` and does not call another economy plugin
- Placed minions are stored in `plugins/RexMinions/minions.db`
- Clicks that are not on a minion are not cancelled

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rexminions help` | Show help | everyone |
| `/rexminions give <player> <type> [level] [amount]` | Give minion items | `rexminions.give` |
| `/rexminions list` | List your minions | `rexminions.use` |
| `/rexminions reload` | Reload files without respawning minions | `rexminions.reload` |

Aliases: `/rminion`, `/minions`

## Permissions

| Permission | Default | Description |
|---|---|---|
| `rexminions.use` | true | Place minions and open them |
| `rexminions.give` | op | Give minion items |
| `rexminions.reload` | op | Reload the configuration |
| `rexminions.admin` | op | Open and pick up any minion, and list every minion |
| `rexminions.limit.<n>` | | Place up to n minions. `rexminions.limit.0` uses `settings.default-limit` |

Documentation: https://docs.rexsystems.me/rexminions/
