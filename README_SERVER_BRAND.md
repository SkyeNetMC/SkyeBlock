# SkyeBlock Server Brand Feature

## Overview
This feature allows you to customize what players see in their F3 debug screen by changing the server "brand" from the default "Paper" or "Spigot" to "LegitiSkyeSlimePaper".

![Server Brand in F3 Screen](https://i.imgur.com/example.png)

## Implementation
The implementation uses multiple approaches to ensure the highest compatibility across different server types:

### Method 1: Direct Property Modification
- Modifies `server.properties`
- Changes server fields using reflection
- Uses console commands where available

### Method 2: Reflection Approach
- Intercepts and modifies packets using reflection
- Changes internal server brand fields
- Works with most Minecraft versions

### Method 3: Plugin Messaging
- Utilizes the standard `minecraft:brand` channel
- Sends custom brand information to the client
- Compatible with Bukkit, Spigot, and Paper servers

### Method 4: Spigot-Specific Method
- Uses Spigot's `setBrand` method (when available)
- Makes runtime modifications to Spigot server fields
- Periodically updates brand to prevent overrides

## How It Works

The server brand is changed at several key points:

1. **Server Startup**: When the plugin initializes, it attempts to modify the server brand using all available methods.
2. **Player Join**: When a player joins the server, all brand methods are re-applied for that specific player.
3. **Player Respawn**: After respawning, the brand is reapplied to ensure consistency.
4. **Periodic Updates**: The Spigot method runs periodically to prevent other plugins from overriding the brand.

## Files
- `ServerBrandChanger.java` - Plugin messaging channel implementation
- `ServerBrandListener.java` - Reflection-based packet interceptor
- `ServerBrandUtil.java` - Multiple approaches to changing server properties
- `SpigotBrandModifier.java` - Spigot-specific implementation
- `PlayerJoinListener.java` - Applies all methods when players join/respawn

## Compatibility

| Server Type | Compatibility | Notes |
|-------------|--------------|-------|
| Paper       | ✅ Excellent  | All methods should work |
| Spigot      | ✅ Excellent  | All methods should work |
| ASP (ASWM)  | ✅ Good      | Most methods should work |
| Bukkit      | ✅ Good      | Plugin messaging should work |
| Forge       | ⚠️ Limited   | May require additional configuration |
| Fabric      | ⚠️ Limited   | May require additional configuration |

## Testing

To test this feature:

1. Build the plugin:
   ```
   ./build.sh
   ```

2. Deploy and test:
   ```
   ./deploy-and-test.sh
   ```

3. Join the server and press F3 to see the custom brand.

## Troubleshooting

### Brand Not Showing
- Check server logs for errors related to brand modification
- Ensure `minecraft:brand` channel is registered and not blocked
- Try restarting the server completely
- Verify the client version is compatible

### Compatibility Issues
- For Paper/Spigot servers: All methods should work
- For non-Spigot servers: The plugin messaging method is most likely to work
- For proxied servers (BungeeCord/Velocity): Additional configuration may be required

## Future Improvements

Potential improvements for future versions:

- Make the brand name configurable in `config.yml`
- Add support for protocol-level brand modifications
- Create a command to reload/update the brand on-demand
- Add compatibility for more server types
