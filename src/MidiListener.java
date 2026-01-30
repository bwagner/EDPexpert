import org.apache.logging.log4j.LogManager;

/*
 * Created on Sep 1, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */

public interface MidiListener {
	public String notifyMidiMessage(byte[] rawMsg);
}

class NoteMidiListener implements MidiListener {
	public String notifyMidiMessage(byte[] rawMsg){
		if ((rawMsg[2] & 0xFF) == 0x00) {
			// Velocity == 0 is defined as "Note Off"
			return "NoteMidiListener:Note Off";
		} else {
			return "NoteMidiListener:Note On";
		}
	}
}

class CtrlMidiListener implements MidiListener {
	public String notifyMidiMessage(byte[] rawMsg){
		if ((rawMsg[2] & 0xFF) == 0x00) {
			return "CtrlMidiListener:Off";
		} else {
			return "CtrlMidiListener:"+rawMsg[2];
		}
	}
}

class SysexMidiListener implements MidiListener {
	public String notifyMidiMessage(byte[] rawMsg){
		LogManager.getLogger().info("3. SysexMidiListener.notifyMidiMessage");
		int i = 0;
		StringBuffer msg = new StringBuffer();
		while (i < EDPMidiHandler.HEAD_HEAD.length
			&& ((EDPMidiHandler.HEAD_HEAD[i] & 0xFF) == (rawMsg[i] & 0xFF))) {
			i++;
		}
		if (i == EDPMidiHandler.HEAD_HEAD.length) {
			msg.append(EDPMidiHandler.decodeSysex(rawMsg));
		} else {
			msg.append("[unknown Sysex]");
		}
		return msg.toString();
	}
}
