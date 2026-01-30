import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class MidiDeviceAndReceiver {

	/**
	 * @param midiDevice
	 * @throws MidiUnavailableException
	 */
	public MidiDeviceAndReceiver(MidiDevice.Info info)
			throws MidiUnavailableException {
		fMidiDevice = MidiSystem.getMidiDevice(info);
		if (fMidiDevice == null) {
			throw new MidiUnavailableException(
					"MidiDevices.openOutputDevice(\"" + info
							+ "\") wasn't able to retrieve MidiDevice");
		}
		LOGGER.info("MidiDeviceAndReceiver(\"" + info
				+ "\") retrieved: <" + fMidiDevice.getDeviceInfo().getName()
				+ "> (" + fMidiDevice.getDeviceInfo().getDescription() + ")");
		fMidiDevice.open();
		fReceiver = fMidiDevice.getReceiver();
	}

	/**
	 * @param message
	 * @param timeStamp
	 */
	public void send(MidiMessage message, long timeStamp) {
		fReceiver.send(message, timeStamp);
	}

	public void cleanUp() {
		if (fReceiver != null) {
			fReceiver.close();
		}
		if (fMidiDevice != null) {
			// attention: this sends sustain pedal off
			// and all note offs on all channels!
			// Bx 40 00 (sus pedal off) Bx 7B 00 (all notes off) x = 0 .. F
			fMidiDevice.close();
		}
	}

	public Receiver getReceiver() {
		return fReceiver;
	}

	private MidiDevice fMidiDevice;
	private Receiver fReceiver;
	private static final Logger LOGGER = LogManager.getLogger(MidiDeviceAndReceiver.class);

}
