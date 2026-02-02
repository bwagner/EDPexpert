# EDPexpert

Java MIDI control application for the Echoplex Digital Pro (EDP) looper.

## Requirements

**Tested Configuration:**
- macOS 15.2 (Sequoia) / macOS 14 (Sonoma) on Apple Silicon (M1/M2/M3/M4)
- OpenJDK 25.0.2 (or compatible Java 21+)
- Ant (for building)

**Note:** Earlier Java versions may work but are untested with this build configuration.

## Dependencies

All dependencies are included in `lib/`:
- **coremidi4j-1.6.jar** - CoreMIDI wrapper (required for SysEx on macOS)
- **log4j-api-2.25.3.jar** - Logging framework API
- **log4j-core-2.25.3.jar** - Logging framework implementation
- **junit-platform-console-standalone-6.0.2.jar** - Unit testing framework

## Building

```bash
# Compile only (incremental)
ant compile

# Build runnable jar
ant create_run_jar

# Clean and rebuild
ant rebuild

# Run tests
ant test
```

The build creates a fat jar at `dist/edpexpert.jar` containing all dependencies.

## Running

### Command Line

```bash
ant run -Dapp.args="\"<MIDI_OUT_DEVICE>\" \"<MIDI_IN_DEVICE>\""
```

### Direct JAR execution

```bash
java --enable-native-access=ALL-UNNAMED -jar dist/edpexpert.jar "<MIDI_OUT_DEVICE>" "<MIDI_IN_DEVICE>"
```

**Note:** The `--enable-native-access=ALL-UNNAMED` flag is required for CoreMIDI4J native library access in Java 21+.

## MIDI Device Configuration

### Finding Available Devices

Use the interactive `k` command after startup to list available MIDI devices.

### Important: CoreMIDI4J Device Names

**Critical:** When using this Java application, you must use device names with the `CoreMIDI4J - ` prefix to ensure SysEx messages work correctly.

**Example:**
```bash
# Correct - uses CoreMIDI4J wrapper (SysEx works)
ant run -Dapp.args="\"CoreMIDI4J - IAC Driver\" \"CoreMIDI4J - IAC Driver\""

# Wrong - uses Java's built-in MIDI (SysEx broken on macOS)
ant run -Dapp.args="\"Bus 1\" \"Bus 1\""
```

### Why CoreMIDI4J?

Java's built-in MIDI on macOS has a long-standing bug ([JDK-8013365](https://bugs.openjdk.org/browse/JDK-8013365), filed 2013, still unresolved) where SysEx messages are incorrectly sent as Note messages instead of proper System Exclusive data. CoreMIDI4J wraps the native CoreMIDI API to fix this issue.

**Device naming:**
- Audio MIDI Setup shows: "IAC Driver Bus 1"
- Native tools (sendmidi/receivemidi) use: "IAC Driver Bus 1"
- This Java app requires: "CoreMIDI4J - IAC Driver"

These all refer to the same underlying macOS MIDI device.

### Common Device Configurations

**Virtual MIDI (for testing):**
```bash
ant run -Dapp.args="\"CoreMIDI4J - IAC Driver\" \"CoreMIDI4J - IAC Driver\""
```

**Physical MIDI interface:**
```bash
# Replace with your actual MIDI interface name
ant run -Dapp.args="\"CoreMIDI4J - Your Interface\" \"CoreMIDI4J - Your Interface\""
```

## Interactive Commands

Once running, the application provides an interactive command prompt (in the shell):

- `?` - Show help
- `i` - Send INFO_REQUEST to EDP
- `d` - Set target EDP Device ID
- `g` - Send GLOBAL_PARAM_REQUEST
- `a` - Send ALL_PARAM_REQUEST
- `r` - Reboot EDP
- `l` - Send LOCAL_PARAM_REQUEST
- `k` - List available MIDI devices
- `x` - Exit application

## Logging Configuration

Log configuration is in `resources/log4j2.xml` (runtime) and `resources/log4j2-test.xml` (tests).

**Change log level:**
```xml
<Root level="info">  <!-- Options: trace, debug, info, warn, error -->
```

**For local development overrides**, create `resources/log4j2-local.xml` (gitignored).

## Development

### Project Structure

```
EDPexpert/
├── src/              # Java source files
├── resources/        # Images and log4j2 configuration
├── lib/              # JAR dependencies
├── bin/              # Compiled classes (generated)
├── dist/             # Built jar (generated)
└── build.xml         # Ant build configuration
```

### Running Tests

```bash
ant test
```

Tests are in `EDPexpertTestCase.java`.

## Troubleshooting

### "Unable to load native library" warnings

If you see architecture mismatch warnings about x86_64 vs arm64, but MIDI works, you can ignore them. Modern Java on ARM can sometimes handle MIDI without CoreMIDI4J, but **SysEx will not work** without CoreMIDI4J.

### SysEx not working

1. Verify you're using `CoreMIDI4J - ` prefixed device names
2. Check that `coremidi4j-1.6.jar` is in `lib/`
3. Verify Java is running with `--enable-native-access=ALL-UNNAMED`

### No MIDI devices found

1. Check MIDI devices are enabled in Audio MIDI Setup
2. If using IAC Driver, verify it's enabled and has at least one bus
3. Use the `k` command to list devices

### Log messages not appearing

1. Verify `resources/log4j2.xml` exists
2. Check log level is set to `info` or lower
3. Ensure resources are being packaged: `jar -tf dist/edpexpert.jar | grep log4j2.xml`

## License

MIT

## Credits

Created by Bernhard Wagner

Uses:
- [CoreMIDI4J](https://github.com/DerekCook/CoreMidi4J) for macOS MIDI/SysEx support
- [Apache Log4j 2](https://logging.apache.org/log4j/2.x/) for logging
