---
title: Messages
description: Chat lines in config.yml.
sidebar:
  order: 3
---

Every line under `messages` accepts MiniMessage, legacy `&` codes, `&#RRGGBB`, and `#RRGGBB`.

`<prefix>` and `{prefix}` are replaced with `messages.prefix` before the line is parsed. Player names are inserted as literal text, so a name cannot inject a tag.

`messages.help` is a list. `/rexminions help` sends those lines.

Placeholders that show up in the matching line:

| Token | Used in |
|---|---|
| `{player}` | `gave` |
| `{type}` | `gave`, `placed`, `picked-up`, `unknown-type` |
| `{level}` | `gave`, `placed` |
| `{amount}` | `gave`, `balance-paid` |
| `{count}` and `{limit}` | `limit`, `list-header` |
| `{world}`, `{x}`, `{y}`, `{z}` | `list-line`, `list-line-admin`, `linked` |
| `{owner}` | `list-line-admin` |
| `{distance}` | `link-missing` |

Sounds are separate keys under `sounds`. A sound name can also contain `{player}` and `{type}`.
