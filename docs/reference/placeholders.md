---
title: Placeholders
description: PlaceholderAPI tokens from RexMinions.
sidebar:
  order: 3
---

These exist when PlaceholderAPI is installed at startup.

| Placeholder | Description |
|---|---|
| `%rexminions_count%` | How many minions that player has placed |
| `%rexminions_limit%` | That player's place limit. Offline players report `settings.default-limit` |
| `%rexminions_count_<type>%` | How many of that type they have, for example `%rexminions_count_miner%` |

Inside `config.yml`, chat lines use the `{token}` names listed on the [Messages](/rexminions/configuration/messages/) page. Those are not PlaceholderAPI tokens.
