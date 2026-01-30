import junit.framework.TestCase;

/*
 * Created on Sep 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author bernhard.wagner
 *
 */
public class EDPexpertTestCase extends TestCase {

	/**
	 * Constructor for EDPexpertTestCase.
	 * @param arg0
	 */
	public EDPexpertTestCase(String arg0) {
		super(arg0);
	}
	
	public void testNibbleCorrectorMissingMiddle(){
		String msg =      "F0 0 1 30 b 7f 1 0  F7";
		String expected = "f0 00 01 30 0b 7f 01 00 f7";
		String newMsg = MidiMessages.completeNibbles(msg);
		assertEquals(expected, newMsg);	
	}
	
	public void testNibbleCorrectorMissingLeading(){
		String msg =      "0 00 01 30 0b 7f 01 0  F7";
		String expected = "00 00 01 30 0b 7f 01 00 f7";
		String newMsg = MidiMessages.completeNibbles(msg);
		assertEquals(expected, newMsg);	
	}
	
	public void testNibbleCorrectorMissingTrailing(){
		String msg =      "F0 00 01 30 0b 7f 01 0  7";
		String expected = "f0 00 01 30 0b 7f 01 00 07";
		String newMsg = MidiMessages.completeNibbles(msg);
		assertEquals(expected, newMsg);	
	}
	
	public void testHex(){
		String byteAsString = "f0";
		byte[] msg = MidiMessages.buildBytes(byteAsString);
		assertEquals(byteAsString, MidiMessages.asHexString(msg[0]));		
		byteAsString = "0f";
		msg = MidiMessages.buildBytes(byteAsString);
		assertEquals(byteAsString, MidiMessages.asHexString(msg[0]));		
		byteAsString = "10";
		msg = MidiMessages.buildBytes(byteAsString);
		assertEquals(byteAsString, MidiMessages.asHexString(msg[0]));		
	}
		
	public void testInfoData(){
		byte[] msg = MidiMessages.buildBytes("f0 00 01 30 0b 05 01 01 01 3f 6e 07 f7");
		String result = EDPMidiHandler.instance().decode(msg);
		String expected = makeString( new String[]{
			"System Exclusive INFO_DATA_CMD version:1 soundmemory size:3f 6e 7 "
		});
//		System.out.println("expected:<"+expected+">");
//		System.out.println("result:<"+result+">");
		assertEquals(expected, result);
	}
	public void testLocalParamDataFrom4Length3(){
		byte[] msg = MidiMessages.buildBytes("f0 00 01 30 0b 01 01 13 04 03 00 03 00 02 f7");
		String result = EDPMidiHandler.instance().decode(msg);
		String expected = makeString( new String[]{
			"System Exclusive LOCAL_PARAM_DATA_CMD ",
			"from:4 length:3 pset:0 <3 0 2 >",
			"SyncMode:SyncOut",
			"TrigThreshold:0",
			"RecordMode:Safe",
		});
//		System.out.println("expected:<"+expected+">");
//		System.out.println("result:<"+result+">");
		assertEquals(expected, result);
	}

	public void testLocalParamDataFrom4Length4(){
		byte[] msg = MidiMessages.buildBytes("f0 00 01 30 0b 01 01 13 04 04 00 03 00 02 01 f7");
		String result = EDPMidiHandler.instance().decode(msg);
		String expected = makeString( new String[]{
			"System Exclusive LOCAL_PARAM_DATA_CMD ",
			"from:4 length:4 pset:0 <3 0 2 1 >",
			"SyncMode:SyncOut",
			"TrigThreshold:0",
			"RecordMode:Safe",
			"OverdubMode:Sustain",
		});
//		System.out.println("expected:<"+expected+">");
//		System.out.println("result:<"+result+">");
		assertEquals(expected, result);
	}

	public void testLocalParamDataFull(){
		byte[] msg = MidiMessages.buildBytes("f0 00 01 30 0b 01 01 13 01 12 00 00 01 00 02 06 00 01 00 02 00 01 00 00 00 01 00 01 00 f7");
		String result = EDPMidiHandler.instance().decode(msg);
		String expected = makeString( new String[]{
			"System Exclusive LOCAL_PARAM_DATA_CMD ",
			"from:1 length:18 pset:0 <0 1 0 2 6 0 1 0 2 0 1 0 0 0 1 0 1 0 >",
			"Loop/Delay:LoopMode",
			"Time Quantize:Cycle",
			"8ths/Cycle:8",
			"SyncMode:SyncIn",
			"TrigThreshold:6",
			"RecordMode:Toggle",
			"OverdubMode:Sustain",
			"RoundMode:Off",
			"InsertMode:Replace",
			"MuteMode:Continuous",
			"Overflow:Stop",
			"MoreLoops:1",
			"AutoRecord:Off",
			"Next LoopCopy:Off",
			"SwitchQuant:Confirm",
			"Velocity:Off",
			"SamplerStyle:Once",
			"Tempo:Off",
		});
//			System.out.println("expected:<"+expected+">");
//			System.out.println("result:<"+result+">");
			assertEquals(expected, result);
		}
		
		public void _testGlobalParamDataFrom4Length8(){
			byte[] msg = MidiMessages.buildBytes(
						"f0 00 01 30 0b 01 01 11 02 08 7f 03 01 24 08 09 54 01 00 01 f7"
					//	"f0 00 01 30 0b 01 11 00 0b 7f 01 01 00 01 24 07 01 54 00 08 f7"
			);
			String result = EDPMidiHandler.instance().decode(msg);
			String expected = makeString( new String[]{
				"System Exclusive GLOBAL_PARAM_DATA_CMD from:2 length:8 pset:127 ",
				"MIDI Channel:4",
				"Control Source:Notes",
				"Source#:36",
				"VolumeCont:8",
				"FeedBkCont:9",
				"LoopTrig#:84",
				"Device ID:1",
				"Sample Number Hi:0",
			});
//				System.out.println("expected:<"+expected+">");
//				System.out.println("result:<"+result+">");
				assertEquals(expected, result);
		}
		
		public void testGlobalParamDataFull(){
		//                                                                         1  2  3  4  5  6  7  8  9 10
		byte[] msg = MidiMessages.buildBytes("f0 00 01 30 0b 01 01 11 00 0b 7f 01 01 00 01 24 07 01 54 01 00 08 f7");
		//                                     ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +-- EOX
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +----- SampleNumLo
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +-------- SampleNumHi
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +----------- Device ID
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +-------------- 54 LoopTrig#
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +----------------- 01 FeedBkCont
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +-------------------- 07 VolumeCont
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  +----------------------- 24 Source#
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  |  +-------------------------- 01 ControlSource
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  |  +----------------------------- 00 midi channel
		//                                     |  |  |  |  |  |  |  |  |  |  |  |  +-------------------------------- 01 current preset
		//                                     |  |  |  |  |  |  |  |  |  |  |  +----------------------------------- 01 previous preset
		//                                     |  |  |  |  |  |  |  |  |  |  +-------------------------------------- 7f pset (for future use)
		//                                     |  |  |  |  |  |  |  |  |  +----------------------------------------- 0b length:11
		//                                     |  |  |  |  |  |  |  |  +-------------------------------------------- 00 from:0
		//                                     |  |  |  |  |  |  |  +----------------------------------------------- 11 COMMAND: GLOBAL_PARAM_DATA
		//                                     |  |  |  |  |  |  +-------------------------------------------------- 01 version
		//                                     |  |  |  |  |  +----------------------------------------------------- 01 device id
		//                                     |  |  |  |  +-------------------------------------------------------- 0b echoplex
		//                                     |  |  |  +----------------------------------------------------------- 30 aurisis byte 3
		//                                     |  |  +-------------------------------------------------------------- 01 aurisis byte 2
		//                                     |  +----------------------------------------------------------------- 00 aurisis byte 1
		//                                     +-------------------------------------------------------------------- f0 sysex
		//									   see EDP-Manual, p. 11-4 or p.302 in the pdf (http://www.gibson.com/files/amps/EchoplexPlusManual12.pdf)


		String result = EDPMidiHandler.instance().decode(msg);
		String expected = makeString( new String[]{
			"System Exclusive GLOBAL_PARAM_DATA_CMD from:0 length:11 pset:127 ",
			"Previous preset:1",
			"Current preset:1",
			"MIDI Channel:1",
			"Control Source:Notes",
			"Source#:36",
			"VolumeCont:7",
			"FeedBkCont:1",
			"LoopTrig#:84",
			"Device ID:1",
			"Sample Number Hi:0",
			"Sample Number Lo:8",
		});
//			System.out.println("expected:<"+expected+">");
//			System.out.println("result:<"+result+">");
			assertEquals(expected, result);
	}

	private String makeString(String[] lines){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<lines.length;i++) {
			sb.append(lines[i]);
			sb.append("\n");
		}
		if(lines.length == 1) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	static {
		EDPMidiHandler.instance().addSysexMidiListener(new SysexMidiListener());
	}

}
