import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Created on Sep 1, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */
public class EDPMidiHandler {

	public static final String AURISIS_ID = "00 01 30";
	public static final String ECHOPLEX_ID = "0B";
	public static final String VERSION = "01";

	public static String DEVICE_BROADCAST = "7F";

	public static final String DEVICE_ID = "DEVICE_ID";
	public static final String COMMAND = "COMMAND";
	public static final String DATA = "DATA";
	
	private static final EDPMidiHandler fgInstance = new EDPMidiHandler();
	
	private static final Logger LOGGER = LogManager.getLogger(EDPMidiHandler.class);

	
	public static EDPMidiHandler instance() {
		return fgInstance;
	}

	final static String HEAD_HEAD_STRING =
		new MessageFormat("{0} {1} {2}").format(
			new Object[] {
				Integer.toHexString(SysexMessage.SYSTEM_EXCLUSIVE),
				AURISIS_ID,
				ECHOPLEX_ID });

	final static byte[] HEAD_HEAD = MidiMessages.buildBytes(HEAD_HEAD_STRING);

	public static final String TEMPLATE =
		new MessageFormat("{0} DEVICE_ID {1} COMMAND DATA {2}")
			.format(
				new Object[] {
					HEAD_HEAD_STRING,
					VERSION,
					Integer.toHexString(ShortMessage.END_OF_EXCLUSIVE)})
			.toString();

	public static String makeHead(String myHead) {
		final String[] KEYS = { DEVICE_ID, COMMAND, DATA, };
		for (int i = 0; i < KEYS.length; i++) {
			myHead = myHead.replaceAll(KEYS[i], "{" + i + "}");
		}
		return myHead;
	}

	/**
	 * Constructs a String containing the bytes of a sysex message in hex notation.
	 * @param device The EDP Device ID to which to send the sysex. 0x7F means broadcast.
	 * @param cmd The command to send in the sysex message.
	 * @param data The data associated with the command to send to the EDP.
	 * @return The String constructed of the given parameters. Will consist of
	 * hex strings representing the bytes in the Sysex message.
	 */
	public static String makeSysexString(
		final String device,
		final String cmd,
		final String data) {
		final String sysex =
			new MessageFormat(makeHead(TEMPLATE)).format(
				new Object[] { device, cmd, data });
		return sysex;
	}

	/**
	 * Same as other makeSysexString method using the default device.
	 * @param cmd The command to send in the sysex message.
	 * @param data The data associated with the command to send to the EDP.
	 * @return The String constructed of the given parameters. Will consist of
	 * hex strings representing the bytes in the Sysex message.
	 */
	public static String makeSysexString(final String cmd, final String data) {
		return makeSysexString(fDevice, cmd, data);
	}

	/**
	 * Sets the default target EDP Device ID.
	 * @param device The EDP Device ID to set.
	 */
	public static void setDevice(final String device) {
		fDevice = device;
	}

	/**
	 * Sets the default EDP Device ID to broadcast.
	 *
	 */
	public static void setDeviceBroadcast() {
		setDevice(DEVICE_BROADCAST);
	}

	public String decode(final byte[] rawMsg) {
		final byte STATUS_BYTE = rawMsg[0];
		final boolean IS_CHANNEL_MESSAGE = (STATUS_BYTE & 0xFF) < 0xF0;
		final int MASK = IS_CHANNEL_MESSAGE ? 0xF0 : 0xFF;
		// 0xF0: cancel out MIDI Channel
		final int CHANNEL_FILTERED_STATUS_INT = (rawMsg[0] & MASK);
		final String stringForStatus = (String) MidiMessages.getStringForStatus(
				CHANNEL_FILTERED_STATUS_INT);

		if (stringForStatus == null) {
			return "[unknown MIDI message]";
		}
		final StringBuffer msg = new StringBuffer();
		msg.append(stringForStatus);
		
		// Note messages
		if (CHANNEL_FILTERED_STATUS_INT == ShortMessage.NOTE_ON || CHANNEL_FILTERED_STATUS_INT == ShortMessage.NOTE_OFF) {
			for(MidiListener ml: fNoteMidiListeners){
				msg.append(ml.notifyMidiMessage(rawMsg));
			}
			if ((rawMsg[2] & 0xFF) == 0x00 || CHANNEL_FILTERED_STATUS_INT == ShortMessage.NOTE_OFF) {
				// Velocity == 0 is defined as "Note Off"
				return "Note Off"; 
			}
			
		// Sysex messages
		} else if (CHANNEL_FILTERED_STATUS_INT == SysexMessage.SYSTEM_EXCLUSIVE) {
			for(MidiListener ml: fSysexMidiListeners){
				LOGGER.info("2. EDPMidiHandler.decode");
				msg.append(ml.notifyMidiMessage(rawMsg));
			}
		} else if (CHANNEL_FILTERED_STATUS_INT == ShortMessage.CONTROL_CHANGE) {
			for(MidiListener ml: fCtrlMidiListeners){
				LOGGER.info("2a. EDPMidiHandler.decode");
				msg.append(ml.notifyMidiMessage(rawMsg));
			}
		}
		return msg.toString();
	}

	/**
	 * @param rawMsg
	 * @return
	 */
	static String decodeSysex(final byte[] rawMsg) {
		LOGGER.info("4. EDPMidiHandler.decodeSysex");
		int index = HEAD_HEAD.length; // 5

		// Please note about deviceId:
		// If Device ID has never been set for your EDP,
		// sysex dumps don't send a version! Hence the parsing fails!
		// Checking the Device ID on your EDP:
		// From Reset Mode Press Parameters 3 times 
		//   then press NextLoop (Load: this enters MIDI dump Mode)
		//   then press Multiply. This will display your Device ID.
		//   If it has never been set, it displays 255.
		@SuppressWarnings("unused")
		final int deviceId = rawMsg[index++];

		@SuppressWarnings("unused")
		final int version = rawMsg[index++]; // See p. 11-2.

		final int command = rawMsg[index++];

		final String commandTxt =
			EDPParameterStrings.EDP_COMMANDS.get(command);
		if (commandTxt == null) {
			return "unknown EDP command:<" + (command & 0xFF) + ">";
		}
		final CmdParser parser = CmdParserStore.instance().getParser(command);
		if(parser == null) return "no parser found for:"+command;
		if(!parser.lengthCorrect(rawMsg.length)) return parser.lastError();
		final String decoded = parser != null ? parser.parse(rawMsg, index) : "";
		return " " + commandTxt + " " + decoded;
	}
	
	/**
	 * Adds a listener to the list of NoteListeners.
	 * These will be notified when a MIDI note command is received.
	 * @param ml The NoteListener to add
	 */
	public void addNoteMidiListener(final MidiListener ml){
		fNoteMidiListeners.add(ml);
	}
	
	public void removeNoteMidiListener(final MidiListener ml){
		fNoteMidiListeners.remove(ml);
	}
	
	/**
	 * Adds a listener to the list of CtrlListeners.
	 * These will be notified when a MIDI ctrl change command is received.
	 * @param ml The CtrlListeners to add
	 */
	public void addCtrlMidiListener(final MidiListener ml){
		fCtrlMidiListeners.add(ml);
	}
	
	public void removeCtrlMidiListener(final MidiListener ml){
		fCtrlMidiListeners.remove(ml);
	}
	
	/**
	 * Adds a listener to the list of SysexListeners.
	 * These will be notified when a sysex msg is received.
	 * @param ml The NoteListener to add
	 */
	public void addSysexMidiListener(final MidiListener ml){
		fSysexMidiListeners.add(ml);
	}
	
	public void removeSysexMidiListener(final MidiListener ml){
		fSysexMidiListeners.remove(ml);
	}
	
	private List<MidiListener> fCtrlMidiListeners = new ArrayList<MidiListener>();
	private List<MidiListener> fNoteMidiListeners = new ArrayList<MidiListener>();
	private List<MidiListener> fSysexMidiListeners = new ArrayList<MidiListener>();

	private static String fDevice = DEVICE_BROADCAST;

}
