# SkyeBlock Plugin - CI/CD and MiniMessage Documentation

## Overview

This document outlines the new features and automated build system for the SkyeBlock plugin.

## Recent Updates

### MiniMessage Integration

The plugin has been updated to use modern MiniMessage formatting instead of legacy color codes:

**Before (Legacy):**
```yaml
message: "&8[&6SkyeBlock&8] &aWelcome!"
```

**After (MiniMessage):**
```yaml
message: "<dark_gray>[<gold>SkyeBlock</gold>]</dark_gray> <green>Welcome!</green>"
```

### Benefits of MiniMessage:
- More readable and maintainable message formatting
- Better color and formatting control
- Support for modern text components
- Improved compatibility with newer Minecraft versions

## GitHub Actions Workflow

The project now includes an automated CI/CD pipeline that:

### On Every Commit:
1. **Builds** the plugin automatically
2. **Runs tests** to ensure code quality
3. **Creates beta releases** with version `beta-{build-number}`
4. **Uploads artifacts** for easy download

### On Release Commits:
When you commit with the message format `release: x.y.z` (e.g., `release: 1.2.0`):

1. **Automatically detects** it's a release commit
2. **Updates version** in pom.xml to the specified version
3. **Generates changelog** from git commits since last release
4. **Creates full release** with proper version tagging
5. **Uploads release JAR** to GitHub releases

## How to Use the Release System

### For Beta Releases:
Just commit and push your changes normally:
```bash
git add .
git commit -m "Add new island template feature"
git push
```
This will automatically create a beta release.

### For Full Releases:
Use the specific commit message format:
```bash
git add .
git commit -m "release: 1.2.0"
git push
```
This will create a full release version 1.2.0.

## Workflow Features

### Code Quality Checks:
- Compiles code to check for errors
- Runs Maven tests
- Caches dependencies for faster builds
- Optional code formatting checks

### Artifact Management:
- Uploads JAR files for each build
- 90-day retention for artifacts
- Automatic versioning based on commit type

### Release Management:
- Automatic changelog generation
- Proper semantic versioning
- GitHub releases with downloadable JARs
- Beta vs. stable release distinction

## Dependencies

The plugin now includes the MiniMessage dependency:
```xml
<dependency>
    <groupId>net.kyori</groupId>
    <artifactId>adventure-text-minimessage</artifactId>
    <version>4.14.0</version>
</dependency>
```

## Development Workflow

1. **Make changes** to your code
2. **Test locally** using `mvn clean package`
3. **Commit changes** with descriptive messages
4. **Push to GitHub** - automatic beta release is created
5. **When ready for release** - commit with `release: x.y.z` format

## Configuration

All messages in `config.yml` now use MiniMessage format. Examples:

```yaml
messages:
  prefix: "<dark_gray>[<gold>SkyeBlock</gold>]</dark_gray> "
  success: "<green>Operation completed successfully!</green>"
  error: "<red>An error occurred!</red>"
  warning: "<yellow>Warning: Check your configuration</yellow>"
  info: "<blue>Information: This is a notice</blue>"
```

## Code Changes

### SkyeBlockPlugin.java
- Added MiniMessage support
- New helper methods: `getMessageComponent()` and `sendMessage()`
- Centralized message handling

### IslandCommand.java & HubCommand.java
- Converted from legacy ChatColor to MiniMessage
- Updated all message sending to use plugin helper methods
- Improved error handling and user feedback

## Troubleshooting

### Build Issues:
- Check that Java 17 is being used
- Verify all dependencies are available
- Review Maven output for specific errors

### Workflow Issues:
- Ensure GitHub repository has proper permissions
- Check that commit messages follow the correct format
- Verify GitHub Actions are enabled in repository settings

## Future Enhancements

- [ ] Add automated testing framework
- [ ] Implement code coverage reporting
- [ ] Add Discord webhook notifications for releases
- [ ] Create development/staging branches workflow
- [ ] Add automatic dependency updates
