---
title: FAQ
description: Clicks, chunks, and WorldGuard.
sidebar:
  order: 1
---

## Clicks that are not on a minion are not cancelled

RexMinions cancels a click only in two cases: the item in the hand is a Rex minion item, or the entity you clicked is a Rex minion. Every other click returns without cancelling, including chests, doors, villager trades, and item frames when you are not holding a minion item.

Right-click a minion to open it. Sneak-right-click it, or hit it, to pick it up. Pickup removes the entity first. The item is given after that. If your inventory is full, the item drops at the minion. You do not keep the entity and also receive the item.

## Loaded chunks only

A minion runs its task on the region scheduler at its own coordinates. The task returns immediately when that chunk is not loaded. There is no global timer that walks every entity on the server.

After a restart, the row in `minions.db` is kept. The entity is spawned again when that chunk is loaded. Reload reads the yaml files and does not spawn a second entity for a minion that is already tracked.

## WorldGuard is optional

If WorldGuard is not installed, RexMinions skips the hook and minions work as usual.

If WorldGuard is installed, the hook class is loaded only after that check. A minion will not break blocks or kill mobs in a region where its owner is not a member. Wilderness, and the `__global__` region, are allowed. If the hook throws, RexMinions logs the failure once and keeps minions working without region checks.

## The seller did not call my economy plugin

The seller uses `prices.yml` only. It adds the price to the minion balance. Collect that balance from the GUI. One whole number becomes one `settings.currency` item, which is an emerald unless you change it.
