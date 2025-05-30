# SkyeBlock Nether Island Guide

SkyeBlock now supports Nether Islands! This feature allows players to create a nether-themed island in a void nether world.

## Features

- Nether islands are created in a dedicated Nether void world
- The biome is set to Nether Wastes by default
- Islands include nether-specific blocks like netherrack, soul sand, and blackstone
- Players receive helpful nether starter items in a chest

## How to Create a Nether Island

Players can create a nether island using the command:
```
/island create nether
```

## Technical Details

### Configuration

Nether world settings are configured in `config.yml`:
```yaml
# Nether world settings
nether:
  name: "skyblock_nether"
  environment: "NETHER"
  biome: "NETHER_WASTES"
  enabled: true
```

### Island Template

The nether island template is defined in `schematics/island-nether.yml`. This template includes:

- A blackstone platform base
- A netherrack middle layer
- Soul sand for growing nether wart
- A chest with starter items:
  - Water buckets
  - Obsidian
  - Flint and steel
  - Nether wart
  - Blaze rods
  - Ghast tear
  - Cooked porkchop
  - Magma blocks

## Limitations and Future Improvements

- Currently, only the Nether Wastes biome is supported
- Future updates may include other nether biomes (Soul Sand Valley, Crimson Forest, etc.)
- Players cannot yet link nether islands to their overworld islands via portals

## Support

For issues or suggestions regarding nether islands, please contact the server administrators.
