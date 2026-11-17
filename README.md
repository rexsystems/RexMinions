# RexMinions

Placeable minions for Paper, Purpur, and Folia. Each minion keeps its own timer at its block and works only while that chunk is loaded.

<p>
  <img src="https://img.shields.io/badge/Minecraft-1.21%2B-62B47A?style=for-the-badge" alt="Minecraft 1.21+">
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
</p>

## Features

- Seven types: slayer, miner, collector, fisher, farmer, seller, lumber
- Place the minion item to spawn an armor stand, or a marker if you change `settings.entity`
- A tool in the minion GUI. Efficiency shortens the wait. Fortune and Looting follow `settings.fortune` and `settings.looting`
- Link a chest, trapped chest, or barrel within `settings.link-distance`
- Seller prices live in `prices.yml`
- SQLite file `minions.db` restores placed minions after a restart
- PlaceholderAPI and WorldGuard are optional

A click is cancelled only when the item is a Rex minion item or the clicked entity is a Rex minion. Other clicks are left alone.

## Requirements

- Java 21
- Paper, Purpur, or Folia 1.21+
- PlaceholderAPI is optional
- WorldGuard is optional

## Install

1. Put the jar in `plugins/`
2. Restart
3. Edit `plugins/RexMinions/config.yml`, `prices.yml`, and the files in `types/`
4. Run `/rexminions reload`

## Commands

| Command | Permission |
|---|---|
| `/rexminions help` | everyone |
| `/rexminions give <player> <type> [level] [amount]` | `rexminions.give` |
| `/rexminions list` | `rexminions.use` |
| `/rexminions reload` | `rexminions.reload` |

Aliases: `/rminion`, `/minions`

## Documentation

The pages in [docs/](docs/index.md) drop into a Starlight site at `src/content/docs/rexminions/`. Paste `docs/sidebar.mjs` into the site sidebar.

Full docs: [https://rexsystems.me/docs/rexminions](https://rexsystems.me/docs/rexminions)

## License

Licensed under the [GNU Affero General Public License v3.0](LICENSE).
