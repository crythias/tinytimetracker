# TinyTimeTracker AI Development Guide

This guide helps AI agents understand and work with the TinyTimeTracker codebase effectively.

## Project Overview

TinyTimeTracker is a Java Swing application for tracking time spent on tasks and generating Excel-based timecards. Key features:

- Task time tracking with a minimalist UI
- Excel spreadsheet export using Apache POI
- Multi-language support through properties files
- Auto-start and system tray integration
- Configurable first day of week

## Architecture

### Core Components

1. `TimeTracker` (`src/tracker/TimeTracker.java`): Main UI class
   - Handles task input, timing, and Excel file generation
   - Uses Swing JDialog for the main window
   - Manages user preferences and timecard files

2. `TimecardSpreadsheet` (`src/tracker/TimecardSpreadsheet.java`): 
   - Creates/updates Excel files using Apache POI
   - Manages timecard formatting and calculations

### Key Integration Points

1. **File Storage**
   - Timecards stored in `~/timecards` by default
   - Excel files named by week starting date
   - Logs in `stdout.log` and `stderr.log`

2. **External Dependencies**
   - Apache POI for Excel manipulation (`lib/poi-*.jar`)
   - Java Swing for UI components
   - Java Preferences API for settings persistence

## Development Workflows


### Build Process

```bash
# Compile the project (requires JDK 1.8 or higher)
ant compile

# Create JAR file
ant jar

# Clean build artifacts
ant clean
```

### Dependencies

- Apache POI 5.4.1 is required. Place `poi-5.4.1.jar` in the `lib/` directory.

```

### Testing

- JUnit tests named with `*Test.java` pattern
- Exclude test files from main jar using build.xml filters

## Key Patterns

1. **Internationalization**
   - All user-facing strings in `messages_*.properties`
   - Use `Messages.getString("key")` for lookups
   - Strings marked with `//$NON-NLS-1$` comments

2. **UI Event Handling**
   - Mouse/keyboard events for task switching
   - Timer-based updates for elapsed time
   - System tray integration for background operation

3. **File Operations**
   - Excel file per week using Apache POI
   - User preferences stored via Java Preferences API
   - Log files for stdout/stderr in timecard directory

## Common Tasks

1. **Adding UI Features**
   - Extend `TimeTracker.java` for main window changes
   - Add message keys to `messages.properties`
   - Handle both mouse and keyboard input methods

2. **Modifying Excel Output**
   - Update `TimecardSpreadsheet.java` for format changes
   - Test with POI's Excel viewer/validator
   - Consider backwards compatibility

3. **Adding Settings**
   - Use Java Preferences API via `prefs` object
   - Add UI elements to popup menu
   - Include default values in `install()` method

4. **Debugging**
   - Check `~/timecards/stdout.log` and `stderr.log`
   - Use `-console` flag to see output in terminal
   - Set `debug="on"` in build.xml for debug info

## Best Practices

1. Always handle internationalization strings
2. Preserve time tracking accuracy in calculations
3. Consider Excel compatibility when modifying formats
4. Maintain minimal UI footprint and system tray support