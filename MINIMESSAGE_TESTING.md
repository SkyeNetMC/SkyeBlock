# MiniMessage Testing Examples

## Quick Test Commands

Once the plugin is loaded on your server, you can test the MiniMessage functionality with these commands:

### Island Commands
```
/island create normal
/island tp
/island delete
/island help
/island list
```

### Hub Commands
```
/hub
```

## Expected Output Examples

### Before (Legacy Format):
```
§8[§6SkyeBlock§8] §aIsland created successfully! Teleporting you there...
§8[§6SkyeBlock§8] §cYou already have an island!
```

### After (MiniMessage Format):
```
[SkyeBlock] Island created successfully! Teleporting you there...
[SkyeBlock] You already have an island!
```

The colors will appear properly in-game with the MiniMessage formatting providing better color control and modern text components.

## Configuration Testing

You can modify the messages in `config.yml` to test different MiniMessage formats:

```yaml
messages:
  # Gradient example
  welcome: "<gradient:gold:yellow>Welcome to SkyeBlock!</gradient>"
  
  # Hover and click example
  info: "<hover:show_text:'Click to create an island'><click:run_command:'/island create'>Create Island</click></hover>"
  
  # Multiple formatting
  important: "<red><bold>IMPORTANT:</bold></red> <italic>Please read the rules</italic>"
  
  # Rainbow text
  rainbow: "<rainbow>This is rainbow text!</rainbow>"
```

## Debugging

If messages aren't displaying correctly:

1. Check server console for MiniMessage parsing errors
2. Verify the Adventure API is available on your server
3. Test with simple color tags first: `<red>test</red>`
4. Validate your MiniMessage syntax at: https://webui.adventure.kyori.net/
