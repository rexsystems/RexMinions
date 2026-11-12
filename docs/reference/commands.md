---
title: Commands
description: RexMinions commands and their permissions.
sidebar:
  order: 1
---

| Command | Description | Permission |
|---|---|---|
| `/rexminions` | Show help | everyone |
| `/rexminions help` | Show help | everyone |
| `/rexminions give <player> <type> [level] [amount]` | Give minion items to an online player | `rexminions.give` |
| `/rexminions list` | List your minions. Admins see every minion | `rexminions.use` |
| `/rexminions reload` | Reload `config.yml`, `prices.yml`, and type files | `rexminions.reload` |

Aliases: `/rminion`, `/minions`

`type` is one of `slayer`, `miner`, `collector`, `fisher`, `farmer`, `seller`, `lumber`. `level` defaults to 1. `amount` defaults to 1 and cannot be higher than 64.

Reload does not place the minions again. Rows already in memory stay as they are. A restart reads `minions.db` and spawns an entity only when that chunk is loaded and an entity with the same id is not already there.

Help text comes from `messages.help`.
