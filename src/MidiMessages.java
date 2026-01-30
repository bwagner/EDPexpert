import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Created on Sep 13, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */
public class MidiMessages {
	
	public static String getStringForStatus(int status) {
		return MESSAGES.get(status);
	}

	static void writeMsg(
		byte[] msg,
		StringBuffer txtBuffer,
		int index,
		final int LENGTH) {
		// write out the whole msg as hexnotation
		txtBuffer.append("<");
		for(int i = 0 ; i < LENGTH ; i++) {
			txtBuffer.append(Integer.toHexString(msg[index++]));
			txtBuffer.append(" ");					
		}
		txtBuffer.append(">");
		txtBuffer.append("\n");
	}
	
	/**
	 * Translates a byte into a String, prepending a "0" if the
	 * value fits into one hex notation character.
	 * @param b The byte to translate
	 * @return The Translated byte.
	 */
	static String asHexString(byte b) {
		return (b>0 && b<16 ? "0" : "")+Integer.toHexString(0xFF & b);
	}

	/**
	 * Transforms the hex String representation of bytes (e.g.
	 * "F0 00 01 30 0B 7F 01 12 01 02 7F F7" )
	 * into the respective byte array. The bytes must be represented
	 * consistently with 2 characters per byte.
	 * @param message 
	 * @return
	 */
	static byte[] buildBytes(String message) {
		message = completeNibbles(message);
		LOGGER.info("message as string:"+message);
		message = message.replaceAll(" ","");
		if(message.length() % 2 != 0){
			LOGGER.error("message has uneven number of nibbles!");
		}
		int nLengthInBytes = message.length() / 2;
		byte[] abMessage = new byte[nLengthInBytes];
		LOGGER.info("MidiMessages.buildBytes(");
		for (int i = 0; i < nLengthInBytes; i++) {
			abMessage[i] = (byte)Integer.parseInt(
				message.substring(i * 2, i * 2 + 2),
				16);
			LOGGER.info(asHexString(abMessage[i]));
		}
		LOGGER.info(")");
		return abMessage;
	}
	
	/**
	 * Prepends all single character hex representations with "0",
	 * e.g. "7" will be replaced by "07". Other character representations
	 * will stay unaffected. The string is then returned.
	 * @param msg The String to transform.
	 * @return The transformed String.
	 */
	public static String completeNibbles(String msg){
		StringTokenizer st = new StringTokenizer(msg);
		StringBuffer result = new StringBuffer();
		while(st.hasMoreTokens()){
			String token = st.nextToken(); 
			if (token.length() == 1) result.append("0");
			result.append(token);
			result.append(" ");
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString().toLowerCase();
	}

	private static Map<Integer, String> MESSAGES = new HashMap<Integer, String>();
	static {
		// Channel Voice Messages
		MESSAGES.put(ShortMessage.NOTE_OFF,              "Note Off");	                         // 0x80
		MESSAGES.put(ShortMessage.NOTE_ON,               "Note On");	                         // 0x90
		MESSAGES.put(ShortMessage.POLY_PRESSURE,         "Polyphonic Key Pressure (Aftertouch)");// 0xa0
		MESSAGES.put(ShortMessage.CONTROL_CHANGE,        "Control Change");	                  // 0xb0
		MESSAGES.put(ShortMessage.PROGRAM_CHANGE,        "Program Change");	                  // 0xc0
		MESSAGES.put(ShortMessage.CHANNEL_PRESSURE,      "Channel Pressure (Aftertouch)");	  // 0xd0
		MESSAGES.put(ShortMessage.PITCH_BEND,            "Pitch Bend");	                      // 0xe0
		
		// System Common Messages
		MESSAGES.put(ShortMessage.MIDI_TIME_CODE,        "MIDI Time Code Quarter Frame");	  // 0xf1
		MESSAGES.put(ShortMessage.SONG_POSITION_POINTER, "Song Position Pointer");	          // 0xf2
		MESSAGES.put(ShortMessage.SONG_SELECT,           "MIDI Song Select");	              // 0xf3
		MESSAGES.put(ShortMessage.TUNE_REQUEST,          "Tune Request");	                  // 0xf6
		MESSAGES.put(ShortMessage.END_OF_EXCLUSIVE,      "End of System Exclusive");	      // 0xf7

		// System Realtime Messages
		MESSAGES.put(ShortMessage.TIMING_CLOCK,          "Timing Clock");	                  // 0xf8
		MESSAGES.put(ShortMessage.START,                 "Start");	                          // 0xfa
		MESSAGES.put(ShortMessage.CONTINUE,              "Continue");	                      // 0xfb
		MESSAGES.put(ShortMessage.STOP,                  "Stop");	                          // 0xfc
		MESSAGES.put(ShortMessage.ACTIVE_SENSING,        "Active Sensing");	                  // 0xfe
		MESSAGES.put(ShortMessage.SYSTEM_RESET,          "System Reset");	                  // 0xff
		
		// System Exclusive Messages
		MESSAGES.put(SysexMessage.SYSTEM_EXCLUSIVE,      "System Exclusive");                 // 0xf0
	}
	
	private static final Logger LOGGER = LogManager.getLogger(MidiMessages.class);
}
