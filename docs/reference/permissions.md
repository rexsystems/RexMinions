---
title: Permissions
description: Permission nodes shipped with RexMinions.
sidebar:
  order: 2
---

| Permission | Default | Description |
|---|---|---|
| `rexminions.use` | true | Place a minion and open your own |
| `rexminions.give` | op | Give minion items |
| `rexminions.reload` | op | Reload the configuration |
| `rexminions.admin` | op | Open, pick up, and list any minion |
| `rexminions.limit.<n>` | | Highest granted `n` is how many minions that player may place |

`rexminions.limit.0` means `settings.default-limit`. A player with no limit node also uses `settings.default-limit`.

See [Levels and limits](/rexminions/guides/levels-and-limits/) for the examples.
