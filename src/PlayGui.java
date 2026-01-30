import java.lang.invoke.MethodHandles;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created: Mar 4, 2009
 */

/**
 * @author bwagner
 * 
 * TODO:
 * Sourcenumber needs to be dynamically evaluated, because it can change at
 * runtime. This is important for the LEDListeners, since they listen to midi
 * note/ctrl events *relative* to Source#.
 *
 */
public class PlayGui {
	private static int MSG_TYPE = ShortMessage.CONTROL_CHANGE;
	
	private static int fgMidiChannel = 0;
	
	private final static int SOURCE_N_OFFSET = 36;
	
	private static int fgSourceNr = 0;

	private static class ComboListener implements ActionListener {
		
		
		private MidiDeviceAndReceiver fMdr;
		
		public ComboListener(final MidiDeviceAndReceiver mdr){
			fMdr = mdr;
		}
	
		public void actionPerformed(final ActionEvent event) {
			ShortMessage myMsg = new ShortMessage();
			final JComboBox<?> combo = (JComboBox<?>)event.getSource();
			final String currentPreset = (String)combo.getSelectedItem();
			final int PROG = createMidiPgmNumber(currentPreset);
			try {
				myMsg.setMessage(ShortMessage.PROGRAM_CHANGE, fgMidiChannel, PROG, 0);
			} catch (InvalidMidiDataException ex) {
				LOGGER.error(Utils.getStackTrace(ex));
			} 
			fMdr.send(myMsg, -1); // -1 means no time stamp			
		}
	
		private int createMidiPgmNumber(String currentPreset) {
			String presetNo = (currentPreset.split(" "))[1];
			// http://www.borg.com/~jglatt/tech/midispec/pgm.htm
			// MIDI programs are numbered 0-127, so we have to subtract 1:
			return Integer.parseInt(presetNo) - 1;
		}
		
	}

	public static void buildGui(final Terminable terminable, final MidiDeviceAndReceiver mdr) {
			Frame f = new Frame();
			f.setLayout(new BorderLayout());
			f.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					Main.terminate(terminable, mdr);
				}
			}
			);
			JPanel panel = new JPanel();
			panel.add(new JLabel("EDPexpert"));
			JPanel buttons = new JPanel();
			final String[] COMMANDS = new String[]{
				"Record",
				"Overdub",
				"Multiply",
				"Ins/Rev",
				"Mute",
				"Undo",
				"NextLoop",
				"Replace",
				"Substitute",
				"Insert",
				"SpeedToggle",
				"ReverseToggle",
				"SUSRecord",
				"SUSOverdub",
				"SUSRoundMultiply",
				"SUSRoundInsert",
				"SUSMute",
				"ShortUndo",
				"SUSNextLoop",
				"SUSReplace",
				"SUSSubstitute",
				"SUSToggleReverse",
				"SUSToggleSpeed",
				"Reset",
				"GeneralReset",
				"ExitParams",
				"SUSUnroundedMultiply",
				"SUSUnroundedInsert",
				"SUSMute-Retrigger",
				"LongUndo",
				"Forward",
				"Reverse",
				"FullSpeed",
				"HalfSpeed",
				"SamplePlay",
				"ReTrigger",
				"ReAlign",
				"MuteReAlign",
				"QuantMIDIStartSong",
				"MuteQuantMIDIStartSong",
				"StartPoint",
				"QuantStartPoint",
				"BeatTriggerSample",
				"MIDIBeatSync",
			};
			final int NUMBER_COMMANDS = COMMANDS.length;
			final int COLUMNS = 7;
			final int ROWS = NUMBER_COMMANDS / COLUMNS + 1;
			buttons.setLayout(new GridLayout(ROWS, COLUMNS));
			buttons.add(PlayGui.makeEdpButton("Parameters",           SOURCE_N_OFFSET, mdr));
			final int OFFSET = SOURCE_N_OFFSET + 2;
			for(int j=0; j<COMMANDS.length;j++){
				buttons.add(PlayGui.makeEdpButton(COMMANDS[j], OFFSET + j, mdr)); 
			}
			buttons.add(PlayGui.makeEdpButton("Loop 1",   84, mdr));
			buttons.add(PlayGui.makeEdpButton("Loop 2",   85, mdr));
			
			// http://www.loopers-delight.com/LDarchive/200409/msg00131.html
			buttons.add(PlayGui.makeSysExButton("Reboot EDP",          "F0 00 01 30 0B 7F 01 12 01 02 7F F7", mdr));

			buttons.add(PlayGui.makeSysExButton("Reset Global Params", "F0 00 01 30 0B 01 01 20 7F F7", mdr));
			buttons.add(PlayGui.makePresets(mdr));
			buttons.add(PlayGui.makeNoteCtrlSelector(mdr));
			buttons.add(new JLabel("MIDI Channel:", SwingConstants.RIGHT));
			buttons.add(PlayGui.makeMidiChannelChooser());
			buttons.add(PlayGui.makePgChButton("revert to last preset", 15, mdr));
			JButton quitButton = new JButton("Quit");
			buttons.add(quitButton);
	
			buttons.add(makeLedPanel());
			
			quitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Main.terminate(terminable, mdr);
				}
			});
			f.add(panel, BorderLayout.NORTH);
			f.add(buttons, BorderLayout.SOUTH);
			f.pack();
			f.setVisible(true);
		}

	/**
	 * @return
	 */
	private static JComponent makeLedPanel() {
		final int SUBCYCLE_OFFSET = -4; // see EDP Manual, p. 7-3
		final int LOOP_STARTPOINT_OFFSET = -3; // see EDP Manual, p. 7-3
//		final int GLOBAL_MIDI_STARTPOINT_OFFSET = -2; // see EDP Manual, p. 7-3
		final int CYCLE_STARTPOINT_OFFSET = -1; // see EDP Manual, p. 7-3
		final JComponent ledPanel = new JPanel();
		
		final List<Integer> al =
			new ArrayList<Integer>(
					Arrays.asList(
							LOOP_STARTPOINT_OFFSET,
							CYCLE_STARTPOINT_OFFSET,
							SUBCYCLE_OFFSET));

		for (int i: al) {
			final LedListener ll = new LedListener(SOURCE_N_OFFSET, i);
			EDPMidiHandler.instance().addCtrlMidiListener(ll);
			EDPMidiHandler.instance().addNoteMidiListener(ll);
			ledPanel.add(ll.getComponent());
		}
		
		//		LedListener gcl = new LedListener(SOURCE_N_OFFSET, GLOBAL_MIDI_STARTPOINT_OFFSET);
//		EDPSysex.instance().addNoteMidiListener(gcl);
//		ledPanel.add(gcl.getComponent());
		// 
		// Loop StartPoint
		// Cycle StartPoint
		// Sub-Cycle (8th note)
		return ledPanel;
	}

	private static JComboBox<?> makePresets(final MidiDeviceAndReceiver mdr){
		final JComboBox<String> comboBox = new JComboBox<String>();
		for(int i = 1 ; i < 16 ; ++i){
			comboBox.addItem("Preset " + i);			
		}
		comboBox.addActionListener(new ComboListener(mdr));
		return comboBox;
	}

	private static JComboBox<?> makeMidiChannelChooser(){
		final JComboBox<Integer> comboBox = new JComboBox<Integer>();
		for(int i = 1 ; i < 17 ; ++i){
			comboBox.addItem(i);			
		}
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				final JComboBox<?> combo = (JComboBox<?>)event.getSource();
				final int currentMidiChannel = (Integer) combo.getSelectedItem();
				fgMidiChannel = currentMidiChannel - 1;
				LOGGER.info("set MIDI Channel to " + currentMidiChannel);
			}
		});
		return comboBox;
	}

	private static JComboBox<?> makeSourceNrChooser(){
		final JComboBox<Integer> comboBox = new JComboBox<Integer>();
		for(int i = 0 ; i < 115 ; ++i){
			comboBox.addItem(i);			
		}
		comboBox.setSelectedItem(fgSourceNr);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				final JComboBox<?> combo = (JComboBox<?>)event.getSource();
				final int currentSourceNr = (Integer) combo.getSelectedItem();
				fgSourceNr = currentSourceNr;
				LOGGER.info("set Source# to " + currentSourceNr);
			}
		});
		return comboBox;
	}

	private static JComboBox<?> makeNoteCtrlSelector(final MidiDeviceAndReceiver mdr){
		final JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem("Note");			
		comboBox.addItem("Ctrl");			
		comboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				final JComboBox<?> combo = (JComboBox<?>)event.getSource();
				final String currentSelection = (String)combo.getSelectedItem();
				if("Note".equals(currentSelection)) {
					MSG_TYPE = ShortMessage.NOTE_ON;
				}else if("Ctrl".equals(currentSelection)){
					MSG_TYPE = ShortMessage.CONTROL_CHANGE;
				}else {
					throw new RuntimeException("unexpected combo selection:"+currentSelection);
				}
			}
			
		});
		return comboBox;
	}

	private static JButton makeSysExButton(String name, final String msg, final MidiDeviceAndReceiver mdr) {
		final JButton button = new JButton(name);
		button.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent arg0) {
				try {
					SendSysexCommand.sendSysex(msg, mdr.getReceiver());
				} catch (InvalidMidiDataException e) {
					LOGGER.error(Utils.getStackTrace(e));
				} catch (MidiUnavailableException e) {
					LOGGER.error(Utils.getStackTrace(e));
				}
			}
		});
		return button;
	}

	private static JButton makePgChButton(String name, final int PROG, final MidiDeviceAndReceiver mdr) {
		final JButton button = new JButton(name);
		button.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent arg0) {
				ShortMessage myMsg = new ShortMessage();
				try {
					myMsg.setMessage(ShortMessage.PROGRAM_CHANGE, fgMidiChannel, PROG, 0);
				} catch (InvalidMidiDataException e) {
					LOGGER.error(Utils.getStackTrace(e));
				} 
				mdr.send(myMsg, -1); // -1 means no time stamp
			}
		});
		return button;
	}

	private static JButton makeEdpButton(String name, final int PITCH, final MidiDeviceAndReceiver mdr) {
		JButton button = new JButton(name);
		button.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent arg0) {
				ShortMessage myMsg = new ShortMessage();
				try {
					final int VELOCITY = 93;
					myMsg.setMessage(MSG_TYPE, fgMidiChannel, PITCH, VELOCITY);
				} catch (InvalidMidiDataException e) {
					LOGGER.error(Utils.getStackTrace(e));
				} 
				mdr.send(myMsg, -1); // -1 means no time stamp
			}
	
			public void mouseReleased(MouseEvent arg0) {
				ShortMessage myMsg = new ShortMessage();
				try {
					final int VELOCITY = 0;
					myMsg.setMessage(MSG_TYPE, fgMidiChannel, PITCH, VELOCITY);
				} catch (InvalidMidiDataException e) {
					LOGGER.error(Utils.getStackTrace(e));
				} 
				mdr.send(myMsg, -1); // -1 means no time stamp
			}
		});
		return button;
	}

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
}
