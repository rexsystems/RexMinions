---
title: Installation
description: Put RexMinions on a Paper, Purpur, or Folia server.
sidebar:
  order: 1
---

1. Download the jar.
2. Put it in the server `plugins/` folder.
3. Start the server once so `plugins/RexMinions/` is written.
4. Edit `config.yml`, `prices.yml`, and the files in `types/`.
5. Run `/rexminions reload`, or restart.

PlaceholderAPI is not required. If it is installed when RexMinions enables, the `%rexminions_...%` placeholders register on their own.

WorldGuard is not required. If the plugin is missing, minions do not check regions. If it is installed and the hook cannot load, RexMinions logs that once and keeps running.
