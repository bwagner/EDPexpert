import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

	// TODO: Filter for incoming messages, possibly using java infrastructure of
	// Transmitter / Receiver
	// TODO: GUI
	// TODO: MIDI-Device selection:
	// let the program start even if there's no MIDI Device given on the command
	// line.
	// show a dialog allowing to choose input/output devices.
	// TODO: figure out whether the EDP is configured ControlSource=Note/Ctr
	// TODO: Jess
	// TODO: Andy's bell idea
	// TODO: CVS
	// TODO: BPM determiner
	// TODO: Change the infrastructure so we can parse not only MIDI-Input, but
	// also saved *.syx-files!
	// TODO: Deal better with the problem if an EDP hasn't the ID set, see
	// EDPSysEx.decodeSysex
	// TODO: Test the code with AndreasHolstein's EDP, because his Device ID is
	// still 255.
	// TODO: Find out how to reset EDP Device ID to 255 which seems to be the
	// factory default.
	// Resetting the EDP by pressing the Param button deletes the Presets but
	// sets the
	// Device ID to 1 (not 255).
	// TODO: When running Main outside eclipse, the messages from EDP don't seem
	// to get parsed?!?!

	public static void main(String[] args) {

		if (args.length == 2) {
			int index = 0;
			final String deviceNameOut = args[index++];
			final String deviceNameIn = args[index++];
			startIt(deviceNameIn, deviceNameOut);
		} else {
			printUsageAndExit();
		}
	}

	public static void startIt(final String deviceNameIn,
			final String deviceNameOut) {
		// CmdParserStore.instance();
		final MidiDeviceAndReceiver midiDeviceAndReceiver;
		try {
			midiDeviceAndReceiver = MidiDevices.openOutputDevice(deviceNameOut);
		} catch (MidiUnavailableException e) {
			LOGGER.error("Main.main: could not acquire MIDI output device.");
			LOGGER.error("Try terminating other MIDI applications or ");
			LOGGER.error("Windows: cycle midi driver in the Device Manager");
			LOGGER.error("or check string passed to MidiDevices.getMdiOut(). Case sensitive!");
			LOGGER.error("Available devices:");
			MidiDevices.listOutputDevices();
			return;
		}

		final long PERIOD_MILLISECONDS = 500;
		Terminable mr = null;
		try {
			mr = new Terminable(new MyReceiver(
					MidiDevices.getMidiIn(deviceNameIn)), PERIOD_MILLISECONDS);
		} catch (final MidiUnavailableException e1) {
			if (midiDeviceAndReceiver != null)
				midiDeviceAndReceiver.cleanUp();
			LOGGER.error("Main.main: acquiring Receiver for MIDI input device failed: "
							+ e1.getMessage());
			LOGGER.error("Try terminating other MIDI applications or ");
			LOGGER.error("(Windows only) disable/enable midi driver in the Device Manager");
			LOGGER.error("Is "
							+ deviceNameIn
							+ " a valid MIDI device passed to MidiDevices.getMidiIn()?");
			MidiDevices.listOutputDevices();
			System.exit(1);
		}

		try {
			final Interactive interactive = new Interactive();
			PlayGui.buildGui(mr, midiDeviceAndReceiver);
			interactive
					.addCommand(
							// INFO_REQUEST (works)
							"i",
							new SendSysexCommand(
									"to stimulate EDP INFO_REQUEST",
									midiDeviceAndReceiver,
									Integer.toHexString(EDPParameterStrings.INFO_REQUEST_CMD),
									"" // no additional parameters. See
										// EDP-Manual, p. 11-3

							// Sysex ID (works)
							))
					.addCommand("d",
							new SetDeviceCommand("to set target EDP Device ID"

							// GLOBAL_PARAM_REQUEST (works)
							))
					.addCommand(
							"g",
							new SendSysexCommand(
									"to stimulate EDP GLOBAL_PARAM_REQUEST",
									midiDeviceAndReceiver,
									Integer.toHexString(EDPParameterStrings.GLOBAL_PARAM_REQUEST_CMD),
									"00 0B 7F" // from length pset, see
												// EDP-Manual, p.11-3, 11-8.
							))
					.addCommand(

							// ALL_PARAM_REQUEST (works)
							"a",
							new SendSysexCommand(
									"to stimulate EDP ALL_PARAM_REQUEST",
									midiDeviceAndReceiver,
									Integer.toHexString(EDPParameterStrings.ALL_PARAM_REQUEST_CMD),
									"" // no additional parameters. See
										// EDP-Manual p.11-3, 11-9.
							))
							.addCommand("r", new AbstractCommand("to reboot EDP") {

								@Override
								public void doIt() {
									try {
                                        LOGGER.info("About to send SysEx: f0 00 01 30 0b 7f 01 12 01 02 7f f7");
                                        SendSysexCommand.sendSysex("f0 00 01 30 0b 7f 01 12 01 02 7f f7",
										midiDeviceAndReceiver.getReceiver());
                                        LOGGER.info("SysEx sent successfully");
									} catch (MidiUnavailableException e) {
										LOGGER.error(Utils.getStackTrace(e));
									} catch (InvalidMidiDataException e) {
										LOGGER.error(Utils.getStackTrace(e));
									}
								}
								
							})
					.addCommand(

							// // GLOBAL_PARAM_RESET // does not seem to work.
							// EDP-Manual p.11-10 says "not implemented"
							// "gr", new SendSysexCommand(
							// "to stimulate EDP GLOBAL_PARAM_RESET_CMD",
							// midiDeviceAndReceiver,
							// Integer.toHexString(EDPParameterStrings.GLOBAL_PARAM_RESET_CMD),
							// "00"
							// )).addCommand(
							//
							// // LOCAL_PARAM_RESET // does not seem to work.
							// EDP-Manual p.11-10 says "not implemented"
							// "lr", new SendSysexCommand(
							// "to stimulate EDP LOCAL_PARAM_RESET_CMD",
							// midiDeviceAndReceiver,
							// Integer.toHexString(EDPParameterStrings.LOCAL_PARAM_RESET_CMD),
							// "00"
							// )).addCommand(

							// LOCAL_PARAM_REQUEST (works)
							"l",
							new SendSysexCommand(
									"to stimulate EDP LOCAL_PARAM_REQUEST",
									midiDeviceAndReceiver,
									Integer.toHexString(EDPParameterStrings.LOCAL_PARAM_REQUEST_CMD),
									"01 12 00" // from length pset // ok (= 00 =
												// currently selected Preset
												// says mgrob. bwa: verified!)
							// min from = 0x01
							// max from = 0x12
							// max length = 0x13 for from = 00 (but from = 00
							// doesn't make sense)
							// max length = 0x12 for from = 01
							// "01 02 7F" // from length pset (= 7F = currently
							// selected Preset manual says) // crashes EDP!
							// "10 03 01" // from length (max. 0x13 = 19) pset
							)).addCommand("k", new AbstractCommand("list MIDI devices") {

								@Override
								public void doIt() {
									System.out.println("list midi devs before");
									MidiDevices.listOutputDevices();
									System.out.println("list midi devs after");
								}});
			interactive.enterLoop();
		} finally {
			terminate(mr, midiDeviceAndReceiver);
		}
	}

	static final void terminate(final Terminable terminable,
			final MidiDeviceAndReceiver mdr) {
		terminable.terminate();
		mdr.cleanUp();
		LOGGER.info("main end.");
		System.exit(0);
	}

	private static void printUsageAndExit() {
		System.out.println("SendSysex: usage:");
		System.out.println("  java SendSysex [<device name>] <hexstring>");
		System.out.println("    <device name>   output to named device. If not given, Java Sound's default device is used.");
		System.out.println("    <hexstring>     the content of the message to send in hexadecimal notation");
		System.out.println("  example: java SendSysex  F0112233F7");
		System.exit(1);
	}

	static class SetDeviceCommand extends AbstractCommand {

		SetDeviceCommand(String helpMsg) {
			super(helpMsg);
		}

		/* (non-Javadoc)
		 * @see Command#doIt()
		 */
		public void doIt() {
			System.out
					.print("enter the device number in hex notation, e.g '0f': ");
			try {
				String device = new BufferedReader(new InputStreamReader(
						System.in)).readLine();
				EDPMidiHandler.setDevice(device);
				System.out.println("EDP Sysex Device ID set to : <" + device
						+ ">");
			} catch (IOException e) {
				e.printStackTrace(); // keep it on stdout, since interaction happens there, too.
				LOGGER.error(Utils.getStackTrace(e));
			}

		}
	}
	
	// private static final Logger LOGGER = LogManager.getLogger();
	private static final org.apache.logging.log4j.Logger LOGGER =
        org.apache.logging.log4j.LogManager.getLogger(Main.class);


	final static int SOURCE_N_OFFSET = 36; // TODO: this should be retrieved
											// from the connected EDP!

	static {
		LOGGER.trace("static section");
        LOGGER.info("Checking CoreMIDI4J status...");
        try {
            Class<?> coreMidiClass = Class.forName("uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider");
            LOGGER.info("CoreMIDI4J class found: " + coreMidiClass);
        } catch (ClassNotFoundException e) {
            LOGGER.error("CoreMIDI4J NOT found in classpath!");
        }

		EDPMidiHandler.instance().addSysexMidiListener(new SysexMidiListener());
	}
}
