import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Created on Aug 31, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */
public class MyReceiver implements javax.sound.midi.Receiver, Closeable {
	
	MyReceiver(final MidiDevice.Info info) throws MidiUnavailableException {
		LOGGER.trace("MyReceiver ctor");
		if(info == null) {
			throw new MidiUnavailableException("MyReceiver(null) called!");
		}
		fMidiDeviceAndTransmitter = new MidiDeviceAndTransmitter(info, this);
		EDPMidiHandler.instance().addNoteMidiListener(fNoteMidiListener = new NoteMidiListener());
		EDPMidiHandler.instance().addSysexMidiListener(fSysexMidiListener = new SysexMidiListener());
		EDPMidiHandler.instance().addSysexMidiListener(fCtrlMidiListener = new CtrlMidiListener());
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.Receiver#close()
	 */
	public void close() {
		EDPMidiHandler.instance().removeNoteMidiListener(fNoteMidiListener);
		EDPMidiHandler.instance().removeSysexMidiListener(fSysexMidiListener);
		EDPMidiHandler.instance().removeSysexMidiListener(fCtrlMidiListener);
		fMidiDeviceAndTransmitter.cleanUp();
		LOGGER.info("closed MIDI");
		LOGGER.info("received "+fTotalCount+" messages and "+fTotalBytes+" bytes.");
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	// 2007-01-19: TODO:
	// checking http://java.sun.com/j2se/1.5.0/docs/api/javax/sound/midi/SysexMessage.html
	// If the message is sent in one chunk the status byte is F0 and the data are
	// terminated by a F7.
	// If the message is sent in several chunks, the followup messages have
	// a status byte of F7 which must be removed!
	public void send(MidiMessage midiMessage, long timeStamp) {
		LOGGER.trace("1. MyReceiver.send");
//		final Throwable t = new Throwable();
//		t.printStackTrace(System.out);

		final byte[] msgBytes  = rebuildMidiMessage(midiMessage);
		final String msgString = createHexStringRep(msgBytes, timeStamp);

		if((msgBytes[0] & 0xFF) != 0xF8) { // filter Timing Clock TODO: find more elegant filter concept
			LOGGER.info(msgString+ " ("+msgBytes.length+" bytes) "+
				EDPMidiHandler.instance().decode(msgBytes));
		}
		fTotalCount++;
		fTotalBytes += msgBytes.length;
	}
	
	public void addMidiHandler(MidiHandler midiHandler){
		fMidiHandlers.add(midiHandler);
	}
	
	public void removeMidiHandler(MidiHandler midiHandler){
		fMidiHandlers.remove(midiHandler);
	}

	/**
	 * Creates hex String representation of the midi message.
	 * @param msgBytes The midi message as bytes.
	 * @param timeStamp the time stamp associated with the midi message
	 * @return hex String representation of the time-stamped midi message.
	 */
	private static String createHexStringRep(byte[] msgBytes, long timeStamp) {
		StringBuffer msg = new StringBuffer();
		msg.append(timeStamp+" ");
		for(int i=0;i<msgBytes.length;i++){
			msg.append(" ");
			msg.append(MidiMessages.asHexString(msgBytes[i]));
		}
		String msgString = MidiMessages.completeNibbles(msg.toString());
		return msgString;
	}

	private static byte[] rebuildMidiMessage(MidiMessage midiMessage) {
		MessageRebuilder builder = fMessageBuilders.get(midiMessage.getClass());
		builder = builder == null ? fDefaultMessageBuilder : builder;
		return builder.rebuildMessage(midiMessage);
	}

	interface MessageRebuilder {
		byte[] rebuildMessage(MidiMessage midiMessage);
	}
	
	static class DefaultMessageRebuilder implements MessageRebuilder {
		public byte[] rebuildMessage(MidiMessage midiMessage) {
			return midiMessage.getMessage();
		}
	}
	
	static class ShortMessageRebuilder implements MessageRebuilder {
		/**
		 * Rebuilds the entire short message by prepending the command and channel.
		 * @param midiMessage the midiMessage
		 * @return The entire short message including command and channel byte.
		 */
		public byte[] rebuildMessage(MidiMessage midiMessage) {
			byte[] msgBytes;
			msgBytes = new byte[midiMessage.getLength()+1];
			if(midiMessage.getLength() == 0){
				msgBytes[0] = (byte)((ShortMessage)midiMessage).getCommand();
				msgBytes[0] += (byte)((ShortMessage)midiMessage).getChannel();
			} else {
				msgBytes[0]= midiMessage.getMessage()[0];
				msgBytes[1]= (byte)((ShortMessage)midiMessage).getData1();
				if(midiMessage.getLength() > 1){
					msgBytes[2]= (byte)((ShortMessage)midiMessage).getData2();
				}
			}
			return msgBytes;
		}
	}
	
	static class SysexMessageRebuilder implements MessageRebuilder {
		/**
		 * Rebuilds the entire sysex message by prepending F0.
		 * @param sysexBytes the sysex data stripped of F0
		 * @return The entire sysex message prepended with F0.
		 */
		public byte[] rebuildMessage(MidiMessage midiMessage) {
			byte[] sysexBytes = ((SysexMessage)midiMessage).getData();
			byte[] msgBytes = new byte[sysexBytes.length + 1];
			msgBytes[0] = (byte)0xF0;
			System.arraycopy(sysexBytes, 0, msgBytes, 1, sysexBytes.length);
			return msgBytes;
		}
	}
	
	static Map<Class<?>, MessageRebuilder> fMessageBuilders;
	static MessageRebuilder fDefaultMessageBuilder = new DefaultMessageRebuilder();
	
	static {
		fMessageBuilders = new HashMap<Class<?>, MessageRebuilder>();
		fMessageBuilders.put(SysexMessage.class, new SysexMessageRebuilder());
		fMessageBuilders.put(ShortMessage.class, new ShortMessageRebuilder());
	}
	
	private MidiDeviceAndTransmitter fMidiDeviceAndTransmitter;
	private int fTotalCount;
	private int fTotalBytes;
	private NoteMidiListener fNoteMidiListener;
	private SysexMidiListener fSysexMidiListener;
	private CtrlMidiListener fCtrlMidiListener;
	private List<MidiHandler> fMidiHandlers;
	
	private static final Logger LOGGER = LogManager.getLogger(MyReceiver.class);

}
