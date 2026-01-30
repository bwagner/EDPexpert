import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/**
 * Created: Jan 13, 2007
 */

/**
 * @author bwagner
 *
 */
public class MidiDeviceAndTransmitter {

	/**
	 * @param info The MidiDevice.Info for which to retrieve the midi device.
	 * @param receiver The Receiver to connect to the transmitter
	 * @throws MidiUnavailableException
	 */
	public MidiDeviceAndTransmitter(MidiDevice.Info info, Receiver receiver)
		throws MidiUnavailableException {
		fMidiDevice = MidiSystem.getMidiDevice(info);
		if (fMidiDevice == null) {
			throw new MidiUnavailableException("MidiDevices.openOutputDevice(\""+info+"\") wasn't able to retrieve MidiDevice");
		}
		fMidiDevice.open();
		fTransmitter = fMidiDevice.getTransmitter();
		fTransmitter.setReceiver(receiver);
	}

	public void cleanUp() {
		if(fTransmitter != null) {
			fTransmitter.close();
		}
		if (fMidiDevice != null) {
			fMidiDevice.close(); 
		}
	}

	private MidiDevice fMidiDevice;
	private Transmitter fTransmitter;
}
