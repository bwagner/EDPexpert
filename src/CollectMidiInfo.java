/*
 * Created on Aug 31, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CollectMidiInfo {
	
	private static final String TRANSMITTER = "transmitter";
	private static final String RECEIVER =    "receiver";
	private static final String SYNTHESIZER = "synthesizer";
	private static final String SEQUENCER =   "sequencer";
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	CollectMidiInfo() throws MidiUnavailableException, InvalidMidiDataException{
		collectDeviceInfos();
		LOGGER.info("---");
		LOGGER.info(fDeviceInfosByVendor);
		LOGGER.info(fDeviceInfosByName);
		LOGGER.info(fDeviceInfosByFeatures);
		LOGGER.info("---");
		ShortMessage myMsg = new ShortMessage();
		// Play the note Middle C (60) moderately loud
		// (velocity = 93)on channel 4 (zero-based).
		myMsg.setMessage(ShortMessage.NOTE_ON, 4, 60, 93); 
		Synthesizer synth = MidiSystem.getSynthesizer();
		synth.open();
		Receiver synthRcvr = synth.getReceiver();
		synthRcvr.send(myMsg, -1); // -1 means no time stamp
//		myMsg.setMessage(ShortMessage.NOTE_OFF, 4, 60, 93); 
//		synthRcvr.send(myMsg, 100000); // -1 means no time stamp
		synth.close();
		System.exit(0);
	}
	
	private void collectDeviceInfos() throws MidiUnavailableException, InvalidMidiDataException {
		fDeviceInfos = MidiSystem.getMidiDeviceInfo();
		fDeviceInfosByVendor       = new HashMap<String, Set<MidiDevice.Info>>();
		fDeviceInfosByName         = new HashMap<String, Set<MidiDevice.Info>>();
		fDeviceInfosByFeatures     = new HashMap<String, Set<MidiDevice.Info>>();
		MidiDevice md;

		for(int i=0;i<fDeviceInfos.length;i++){
			LOGGER.info("Device "+i);
			LOGGER.info("\tVendor:       "+fDeviceInfos[i].getVendor());
			storeMdi(fDeviceInfos[i], fDeviceInfos[i].getVendor(), fDeviceInfosByVendor);
			LOGGER.info("\tName:         "+fDeviceInfos[i].getName());
			storeMdi(fDeviceInfos[i], fDeviceInfos[i].getName(), fDeviceInfosByName);
			LOGGER.info("\tVersion:      "+fDeviceInfos[i].getVersion());
			LOGGER.info("\tDescription:  "+fDeviceInfos[i].getDescription());
			md = MidiSystem.getMidiDevice(fDeviceInfos[i]);

			if (md.getMaxTransmitters() == -1) {
				LOGGER.info("\tTransmitters: unlimited");
			} else if (md.getMaxTransmitters() > 0) {
				LOGGER.info("\tTransmitters: "+md.getMaxTransmitters());
			}
			if(md.getMaxTransmitters() != 0) {
				storeMdi(fDeviceInfos[i], TRANSMITTER, fDeviceInfosByFeatures);
			}
			
			if (md.getMaxReceivers() == -1) {
				LOGGER.info("\tReceivers:    unlimited");
			} else if (md.getMaxReceivers() > 0) {
				LOGGER.info("\tReceivers:    "+md.getMaxReceivers());
			}
			if(md.getMaxReceivers() != 0) {
				storeMdi(fDeviceInfos[i], RECEIVER, fDeviceInfosByFeatures);
			}

			boolean isSynth = md instanceof Synthesizer;
			boolean isSeq   = md instanceof Sequencer;
			if(isSynth) {
				storeMdi(fDeviceInfos[i], SYNTHESIZER, fDeviceInfosByFeatures);
			}
			if(isSeq) {
				storeMdi(fDeviceInfos[i], SEQUENCER, fDeviceInfosByFeatures);
			}
			
			LOGGER.info("\tInterfaces:  "+(isSynth ? "Synthesizer" : "")+(isSynth && isSeq ? ", " : "")+(isSeq ? "Sequencer" : ""));
			ShortMessage sm = new ShortMessage();
			sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
			long timeStamp = -1;
			try {
				md.open();
				md.getReceiver().send(sm, timeStamp);
			} catch (IllegalStateException e) {
				System.err.println(e);
			} catch (MidiUnavailableException e) {
				System.err.println(e);
			}
			md.close();
		}
	}
	
	private void storeMdi(MidiDevice.Info mdi, String key, Map<String, Set<MidiDevice.Info>> map) {
		Set<MidiDevice.Info> content = map.get(key);
		if(content == null) {
			content = new HashSet<MidiDevice.Info>();
			map.put(key, content);
		}
		content.add(mdi);
	}

	public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
		new CollectMidiInfo();
	}
	
	private MidiDevice.Info[] fDeviceInfos;
	private Map<String, Set<MidiDevice.Info>> fDeviceInfosByVendor;
	private Map<String, Set<MidiDevice.Info>> fDeviceInfosByName;
	private Map<String, Set<MidiDevice.Info>> fDeviceInfosByFeatures;
}
