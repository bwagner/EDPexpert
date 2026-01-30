import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

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
public class MidiDevices {
	
	/*
	 * 
	 * I can have a log4j2.xml lying around when I call the app like this:
	 * java -jar edpexpert.jar UM-ONE UM-ONE
	 * Log4j2 actually searches for the log4j2.xml in the classpath.
	 * 
	 * This does NOT work with log4j2.yaml.
	 * 
	 * I want to have a configuration where the app just runs by double-clicking
	 * the single jar.
	 * If anyone needs to peek into the log messages I'd like to give them the option
	 * to drop a log4j2.xml inside the directory where the edpexpert.jar lives,
	 * and call the app from the command line. Or find a logger in log4j2 that
	 * opens its own window, so the app can still be started by double clicking
	 * the jar. 
	 * 
	 * TODO:
	 * Improve the startup procedure so it doesn't read from the command line
	 * but rather lists all available devices and either picks a default or
	 * lets the user choose one and store that in a config file for later
	 * startups.
	 * 
	 * DONE:
	 * Sending SysEx doesn't work at all on OSX. It sends Notes instead!
	 * E.G. pressing Button "Reboot EDP", will send Note On Message on CH 1, 
	 * E3, 7F. Exactly the same with "Reset Global Params".
	 * https://stackoverflow.com/questions/8148898/java-midi-in-mac-osx-broken
	 * http://www.humatic.de/htools/mmj.htm
	 * https://bugs.openjdk.java.net/browse/JDK-8013365
	 * http://www.xfactory-librarians.co.uk/installation.html#OSX
	 * https://github.com/DerekCook/CoreMidi4J
	 * Simply adding coremidi4j-1.1.jar to the project solved this!
	 * 
	 * 
	 * Trace levels:
	 * see https://logging.apache.org/log4j/2.x/manual/architecture.html
	 * Search for "filtering"
	 * 
	 * If Event Level (the level in the source code) is FATAL, you'll see
	 * it in the log for all *configured* levels except "OFF".
	 * If Event level is TRACE, you'll see it only if configuration is
	 * set to TRACE, otherwise not. 
	 * 
	 */


	// TODO: avoid code duplication between getMidiIn and getMidiOut
	/**
	 * Retrieves the first midi device matching the given deviceName or
	 * description and having at least one transmitter. If none such is found
	 * null is returned else the MidiDevice.Info for the midi device. If none
	 * such is found null is returned else the found receiver.
	 * 
	 * @param deviceNameOrDescription
	 *            The device for which to get a receiver
	 * @return the MidiDevice.Info or null
	 */
	public static MidiDevice.Info getMidiIn(String deviceNameOrDescription) {
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		int i = 0;
		try {
			while (i < infos.length
					&& (infos[i].getName().indexOf(deviceNameOrDescription) == -1
							&& (infos[i].getDescription().indexOf(
									deviceNameOrDescription) == -1) || MidiSystem
							.getMidiDevice(infos[i]).getMaxTransmitters() == 0)) {
				int transmitters = MidiSystem.getMidiDevice(infos[i])
						.getMaxTransmitters();
				LOGGER.info(" getMidiIn(" + deviceNameOrDescription
						+ ") " + i + " didn't work out: " + infos[i].getName()
						+ " (" + infos[i].getDescription() + ") transmitters: "
						+ (transmitters == -1 ? "unlimited" : transmitters));
				i++;
			}
			if (i < infos.length) {
				int transmitters = MidiSystem.getMidiDevice(infos[i])
						.getMaxTransmitters();
				LOGGER.info("found in:" + i + " <" + infos[i].getName()
						+ "> (" + infos[i].getDescription()
						+ "), transmitters:"
						+ (transmitters == -1 ? "unlimited" : transmitters));
				return infos[i];
			} else {
				LOGGER.error("MidiDevices.getMidiIn(\""
								+ deviceNameOrDescription
								+ "\"): unable to retrieve appropriate MidiDevice.Info");
				return null;
			}
		} catch (MidiUnavailableException e) {
			LOGGER.error(Utils.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Retrieves the first midi device matching the given deviceName or
	 * description and having at least one receiver. If none such is found null
	 * is returned else the MidiDevice.Info for the midi device.
	 * 
	 * @param deviceNameOrDescription
	 *            The device for which to get a receiver
	 * @return the MidiDevice.Info or null
	 */
	private static MidiDevice.Info getMidiOut(String deviceNameOrDescription) {
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		int i = 0;
		try {
			while (i < infos.length
					&& (infos[i].getName().indexOf(deviceNameOrDescription) == -1
							&& (infos[i].getDescription().indexOf(
									deviceNameOrDescription) == -1) || MidiSystem
							.getMidiDevice(infos[i]).getMaxReceivers() == 0)) {
				int receivers = MidiSystem.getMidiDevice(infos[i])
						.getMaxReceivers();
				LOGGER.info(" getMidiOut(" + deviceNameOrDescription
						+ ") " + i + " didn't work out: " + infos[i].getName()
						+ " (" + infos[i].getDescription() + "), receivers: "
						+ (receivers == -1 ? "unlimited" : receivers));
				i++;
			}
			if (i < infos.length) {
				int receivers = MidiSystem.getMidiDevice(infos[i])
						.getMaxReceivers();
				LOGGER.info("found out:" + i + " <" + infos[i].getName()
						+ "> (" + infos[i].getDescription() + "), receivers:"
						+ (receivers == -1 ? "unlimited" : receivers));
				return infos[i];
			} else {
				LOGGER.error("MidiDevices.getMidiOut(\""
								+ deviceNameOrDescription
								+ "\"): unable to retrieve appropriate MidiDevice.Info");
				return null;
			}
		} catch (MidiUnavailableException e) {
			LOGGER.error(Utils.getStackTrace(e));
			return null;
		}
	}

	/**
	 * @param deviceName
	 * @return
	 * @throws MidiUnavailableException
	 */
	static MidiDeviceAndReceiver openOutputDevice(String deviceName)
			throws MidiUnavailableException {
		MidiDevice.Info info = getMidiOut(deviceName);
		return info != null ? new MidiDeviceAndReceiver(info) : null;
	}

	/**
	 * @param deviceNameOrDescription
	 * @return
	 * @throws MidiUnavailableException
	 */
	static MidiDeviceAndTransmitter openInputDevice(String deviceNameOrDescription,
			Receiver receiver) throws MidiUnavailableException {
		MidiDevice.Info info = getMidiIn(deviceNameOrDescription);
		return info != null ? new MidiDeviceAndTransmitter(info, receiver)
				: null;
	}

	public static void probe(boolean verbose) {
		MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
		MidiDevice md = null;
		for (int i = 0; i < devices.length; i++) {
			if (verbose) {
				LOGGER.info("device " + i);
				LOGGER.info("\tVendor:       " + devices[i].getVendor());
				LOGGER.info("\tName:         " + devices[i].getName());
				LOGGER.info("\tVersion:      " + devices[i].getVersion());
				LOGGER.info("\tDescription:  "
						+ devices[i].getDescription());
			}

			try {
				md = MidiSystem.getMidiDevice(devices[i]);
			} catch (MidiUnavailableException e1) {
				LOGGER.error(Utils.getStackTrace(e1));
			}

			if (verbose) {
				if (md.getMaxTransmitters() == -1) {
					LOGGER.info("\tTransmitters: unlimited");
				} else if (md.getMaxTransmitters() > 0) {
					LOGGER.info("\tTransmitters: "
							+ md.getMaxTransmitters());
				}
			}
			if (md.getMaxTransmitters() != 0) {
				fgInputs.add("device " + i + " <" + devices[i].getName() + ">");
			}
			if (verbose) {
				if (md.getMaxReceivers() == -1) {
					LOGGER.info("\tReceivers: unlimited");
				} else if (md.getMaxReceivers() > 0) {
					LOGGER.info("\tReceivers: " + md.getMaxReceivers());
				}
			}

			if (md.getMaxReceivers() != 0) {
				fgOutputs
						.add("device " + i + " <" + devices[i].getName() + ">");
			}
			if (verbose) {
				boolean isSynth = md instanceof Synthesizer;
				boolean isSeq = md instanceof Sequencer;

				LOGGER.info("\tInterfaces:  "
						+ (isSynth ? "Synthesizer" : "")
						+ (isSynth && isSeq ? ", " : "")
						+ (isSeq ? "Sequencer" : ""));
			}
			try {
				md.open();
			} catch (IllegalStateException e) {
				System.err.println(e);
			} catch (MidiUnavailableException e) {
				System.err.println(e);
			}
			md.close();
		}

		if (verbose) {
			LOGGER.info("Inputs:");
			final Iterator<String> iit = fgInputs.iterator();
			while (iit.hasNext()) {
				LOGGER.info(iit.next());
			}
			LOGGER.info("Outputs:");
			final Iterator<String> oit = fgOutputs.iterator();
			while (oit.hasNext()) {
				LOGGER.info(oit.next());
			}
		}
	}

	public static void main(String[] args) {
		probe(true);
		System.exit(0);
	}

	/**
	 * 
	 */
	public static void listOutputDevices() {
		LOGGER.info("Outputs:");
		System.out.println("Outputs:");
		if (fgOutputs.size() == 0)
			probe(false);
		final Iterator<String> oit = fgOutputs.iterator();
		while (oit.hasNext()) {
			final String next = oit.next();
			LOGGER.info(next);
			System.out.println(next);
		}
	}

	private static List<String> fgOutputs = new ArrayList<String>();
	private static List<String> fgInputs = new ArrayList<String>();
	
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

}
