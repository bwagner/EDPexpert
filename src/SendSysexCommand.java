import java.lang.invoke.MethodHandles;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class SendSysexCommand extends AbstractCommand {
	
	public SendSysexCommand(String helpText, MidiDeviceAndReceiver midiDeviceAndReceiver, String device, String command, String data){
		super(helpText);
		fMIDI_DEVICE_AND_RECEIVER = midiDeviceAndReceiver;
		fcDEVICE = device;
		fcCOMMAND = command;
		fcDATA = data;
	}
	
	public SendSysexCommand(String helpText, MidiDeviceAndReceiver midiDeviceAndReceiver, String command, String data){
		this(helpText, midiDeviceAndReceiver, EDPMidiHandler.DEVICE_BROADCAST, command, data);
	}
	
	public void doIt(){
		LOGGER.info("doit sendsysex");
		try {
			sendSysex(EDPMidiHandler.makeSysexString(fcDEVICE, fcCOMMAND, fcDATA), fMIDI_DEVICE_AND_RECEIVER.getReceiver());
		} catch (final MidiUnavailableException ex) {
			LOGGER.error("sending sysex to MIDI device <"+fMIDI_DEVICE_AND_RECEIVER+"> failed:");
			LOGGER.error(Utils.getStackTrace(ex));
		} catch (final InvalidMidiDataException ex) {
			LOGGER.error("sending sysex to MIDI device <"+fMIDI_DEVICE_AND_RECEIVER+"> failed:");
			LOGGER.error(Utils.getStackTrace(ex));
		}
	}
	
	public static void sendSysex(
		String message, Receiver receiver)
		throws MidiUnavailableException, InvalidMidiDataException {
		
		deliverMessage(receiver, prepareSysEx(MidiMessages.buildBytes(message)));
		
	}

	private static void deliverMessage(
		Receiver receiver,
		SysexMessage sysexMessage) {
		 receiver.send(sysexMessage, -1);
	}

	private static SysexMessage prepareSysEx(byte[] abMessage)
		throws InvalidMidiDataException {
		SysexMessage sysexMessage = new SysexMessage();
		sysexMessage.setMessage(abMessage, abMessage.length);
		return sysexMessage;
	}

	private final MidiDeviceAndReceiver fMIDI_DEVICE_AND_RECEIVER;
	private final String fcDEVICE;
	private final String fcCOMMAND;
	private final String fcDATA;
	
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
}

