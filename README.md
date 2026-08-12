# HopperNG

HopperNG is a configurable linked-hopper plugin for modern Paper servers. It replaces the original prototype with a deterministic, multi-world-safe implementation for Paper 1.21.10 and Java 21.

## Features

- Manage any hopper with **Shift + right-click using an empty hand** or `/hopperng manage`.
- Link a hopper to one or more inventory blocks.
- Multiple targets use **round-robin routing** and automatically skip unloaded/full targets.
- Configurable transfer amount: **1 / 12 / 64 items** per processing cycle.
- Optional **Fast-Tick** mode with a separate permission.
- Configurable ground-item collection radius.
- Item filters: Off, Material only, or Exact item including metadata.
- Pulls matching items from an inventory directly above the managed hopper.
- Redstone power disables processing, matching vanilla hopper behavior.
- Visual connection/radius preview using particles.
- Persistent `hoppers.yml` storage including the world name for every source and target.
- Safe cleanup when a tracked source/target block is broken or destroyed by an explosion.
- Does **not force-load chunks** during normal processing.
- Nearby-item collection uses a bounded nearby-entity query rather than scanning every item entity in the world.
- Build validation through GitHub Actions.

## Requirements

- Paper 1.21.10 or newer compatible Paper release
- Java 21+

## Build

```bash
mvn clean verify
```

The JAR is created as `target/HopperNG-1.0.0.jar`.

## Installation

1. Build or download the HopperNG JAR.
2. Put it into the server's `plugins/` directory.
3. Start the server once to generate `plugins/HopperNG/config.yml`.
4. Adjust the configuration if required and run `/hopperng reload`.

## Usage

By default, normal hopper placement remains vanilla. Register/manage a hopper with an empty main hand by sneaking and right-clicking it, or look at it and run `/hopperng manage`.

Set `auto-register-on-place: true` if every newly placed hopper should automatically become managed.

### Add targets

Open the hopper menu and choose **Ziel hinzufügen**. Then right-click any block that exposes an inventory. Sneaking while linking is active cancels the mode. Multiple targets are routed round-robin. Full or unloaded targets are skipped without force-loading their chunks.

### Filters

While the main menu is open, click an item in the lower player inventory to use it as the filter sample. Right-click the filter button to switch between `MATERIAL` and `EXACT`; Shift-click it to clear the filter. The filter applies to automated intake from the block above and to collected ground items. Manually inserted items are not rejected.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/hopperng manage` | Manage the hopper being looked at | `hopperng.use` |
| `/hopperng stats` | Show managed/loaded/fast hopper statistics | `hopperng.admin` |
| `/hopperng reload` | Reload `config.yml` and `hoppers.yml` | `hopperng.admin` |
| `/hopperng prune` | Remove invalid entries whose chunks are currently loaded | `hopperng.admin` |
| `/hopperng help` | Show command help | none |

Alias: `/minehopper`

## Permissions

- `hopperng.use` — default: everyone
- `hopperng.fasttick` — default: operators
- `hopperng.admin` — default: operators; includes the permissions above

## Configuration

```yaml
enabled-worlds: []
auto-register-on-place: false

defaults:
  transfer-amount: 12
  collect-radius: 1
  fast-tick: false

intervals:
  normal-ticks: 8
  fast-ticks: 1

limits:
  max-collect-radius: 15
  max-targets-per-hopper: 8

visualizer:
  duration-seconds: 30
  period-ticks: 10
```

An empty `enabled-worlds` list enables HopperNG in all worlds. Otherwise, only exact world names in that list are accepted.

## Persistence and chunk behavior

HopperNG stores source and target locations in `plugins/HopperNG/hoppers.yml`. Every location contains its world name and block coordinates. Processing only occurs when the relevant chunks are already loaded. This prevents linked hoppers from keeping remote areas active or causing chunk-load spikes.

## Upgrade note from the prototype

The original `0.1.0` prototype stored coordinates without a world identifier. HopperNG 1.0.0 therefore does not silently import the old `config.json`, because doing so could map a hopper to the wrong world. Keep a backup of the legacy file and recreate the managed hopper links once after upgrading.

## License

Apache License 2.0. See `LICENSE`.
