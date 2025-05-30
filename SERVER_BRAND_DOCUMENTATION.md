# Server Brand Customization Feature

## Overview
This document describes the server brand customization feature implemented in the SkyeBlock plugin. This feature allows server administrators to change the server brand that players see in their F3 debug screen from the default "Paper" or "Spigot" to "LegitiSkyeSlimePaper".

## Implementation
The server brand feature uses multiple methods to ensure compatibility across different server implementations:

### 1. Direct Property Modification
The `ServerBrandUtil` class attempts to modify the server brand by:
- Updating the server.properties file
- Modifying server configuration fields via reflection
- Setting server name via console commands

### 2. Reflection-Based Approach
The `ServerBrandListener` class uses reflection to:
- Modify the server brand at the root level
- Send brand packets directly to players when they join
- Intercept and modify connection packets

### 3. Plugin Messaging Approach
The `ServerBrandChanger` class uses Bukkit's plugin messaging system to:
- Register a "minecraft:brand" channel
- Send custom brand messages to players
- Override brand requests

### 4. Spigot-Specific Integration
The `SpigotBrandModifier` class uses Spigot-specific methods to:
- Use the built-in setBrand method if available
- Modify key server fields
- Apply the brand periodically to ensure it doesn't get reset

## Client Experience
When a player joins the server and presses F3, they will see "LegitiSkyeSlimePaper" as the server brand instead of the default Paper/Spigot/Bukkit brand.

## Technical Details

### Packet Handling
The implementation intercepts and modifies the server brand packet (`PacketPlayOutCustomPayload`) that gets sent to clients when they join the server.

### Compatibility
- ✅ Works with Paper servers
- ✅ Works with Spigot servers
- ✅ Works with ASP servers (with built-in ASWM)
- ✅ Works with regular Bukkit servers

### Fallbacks
Multiple approaches are used to ensure at least one method successfully changes the brand:
1. If reflection fails, plugin messaging is attempted
2. If plugin messaging fails, direct packet sending is attempted
3. If all else fails, Spigot-specific methods are tried

### Persisting Brand Changes
The brand is updated at several key moments:
- When the plugin starts
- When players join the server
- Periodically (every 10 minutes)

## Configuration
The server brand feature can be configured in the plugin's `config.yml` file:

```yaml
server-brand:
  # Whether to enable custom server brand
  enabled: true
  # The custom brand name to show in the F3 debug screen
  name: "LegitiSkyeSlimePaper"
  # Whether to send brand updates periodically
  periodic-updates: true
  # Update interval in minutes
  update-interval-minutes: 10
```

## Commands
The plugin provides a command to manage the server brand feature:

- `/serverbrand` - View current settings
- `/serverbrand reload` - Reload configuration
- `/serverbrand update` - Update brand for all online players
- `/serverbrand set <name>` - Set custom brand name
- `/serverbrand toggle` - Enable/disable the feature

Permission: `skyeblock.admin.serverbrand`

## Testing
To test this feature:
1. Install the plugin on your server
2. Start/restart the server
3. Join as a player
4. Press F3 and look for your configured brand name in the debug screen
5. Check server logs for any messages related to server brand modification

You can also use the `test-server-brand.sh` script to verify the feature is working correctly.

## Troubleshooting
If the server brand modification isn't working:
1. Check server logs for any warnings or errors related to brand modification
2. Ensure your server allows plugin messaging on the "minecraft:brand" channel
3. Try with a Paper or Spigot server as they have the best compatibility
4. Note that some proxy servers may override the brand
