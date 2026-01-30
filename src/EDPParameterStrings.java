import java.util.HashMap;
import java.util.Map;

/*
 * Created on Sep 13, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */
public class EDPParameterStrings {
	
	public static Map<Integer, String> EDP_COMMANDS = new HashMap<Integer, String>();
	public static final int INFO_REQUEST_CMD         = 0x00;
	public static final int INFO_DATA_CMD            = 0x01;
	public static final int GLOBAL_PARAM_REQUEST_CMD = 0x10;
	public static final int GLOBAL_PARAM_DATA_CMD    = 0x11;
	public static final int LOCAL_PARAM_REQUEST_CMD  = 0x12;
	public static final int LOCAL_PARAM_DATA_CMD     = 0x13;
	public static final int ALL_PARAM_REQUEST_CMD    = 0x14;
	public static final int GLOBAL_PARAM_RESET_CMD   = 0x20;
	public static final int LOCAL_PARAM_RESET_CMD    = 0x21;
		
	static {
	
		EDP_COMMANDS.put(INFO_REQUEST_CMD        , "INFO_REQUEST_CMD"); // request
		EDP_COMMANDS.put(INFO_DATA_CMD           , "INFO_DATA_CMD"); // response
		EDP_COMMANDS.put(GLOBAL_PARAM_REQUEST_CMD, "GLOBAL_PARAM_REQUEST_CMD"); // request
		EDP_COMMANDS.put(GLOBAL_PARAM_DATA_CMD   , "GLOBAL_PARAM_DATA_CMD"); // response
		EDP_COMMANDS.put(LOCAL_PARAM_REQUEST_CMD , "LOCAL_PARAM_REQUEST_CMD");
		EDP_COMMANDS.put(LOCAL_PARAM_DATA_CMD    , "LOCAL_PARAM_DATA_CMD"); // response
		EDP_COMMANDS.put(ALL_PARAM_REQUEST_CMD   , "ALL_PARAM_REQUEST_CMD"); // request
		EDP_COMMANDS.put(GLOBAL_PARAM_RESET_CMD  , "GLOBAL_PARAM_RESET_CMD");
		EDP_COMMANDS.put(LOCAL_PARAM_RESET_CMD   , "LOCAL_PARAM_RESET_CMD");

	}
	

	public static final String[][] fgGlobalParameters = new String[][]{
		{"Previous preset"},
		{"Current preset"},
		{"MIDI Channel"},
		{"Control Source", "Off", "Notes", "Controllers"},
		{"Source#"},
		{"VolumeCont"},
		{"FeedBkCont"},
		{"LoopTrig#"},
		{"Device ID"},
		{"Sample Number Hi"},
		{"Sample Number Lo"},
		{"Sample Number"},

	};

	public static final String[][] fgLocalParameters = new String[][]{
	{ // 0
		"Loop/Delay",
		"LoopMode",
		"DelayMode",
		"ExpertMode",
		"StutterMode",
		"OutMode",
		"InputMode",
		"ReplaceMode",
		"FlipMode",
	}, { // 1
		"Time Quantize",
		"Off",
		"Cycle",
		"8th",
		"Loop",
	}, { // 2
		"8ths/Cycle",
		"8",   "4",  "2",  "6", "12", "16", "32", "64", "128",
		"1",   "2",  "3",  "4",  "5",  "6",  "7",  "8",  "9", "10", 
		"11", "12", "13", "14", "15", "16", "17", "18", "19", "20", 
		"21", "22", "23", "24", "25", "26", "27", "28", "29", "30", 
		"31", "32", "33", "34", "35", "36", "37", "38", "39", "40", 
		"41", "42", "43", "44", "45", "46", "47", "48", "49", "50", 
		"51", "52", "53", "54", "55", "56", "57", "58", "59", "60", 
		"61", "62", "63", "64", "65", "66", "67", "68", "69", 
	}, { // 3
		"SyncMode",
		"Off",
		"OutUserStartSong",
		"SyncIn",
		"SyncOut",
	}, { // 4
		"TrigThreshold",
		"0",
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
	}, { // 5
		"RecordMode",
		"Toggle",
		"Sustain",
		"Safe",
	}, { // 6
		"OverdubMode",
		"Toggle",
		"Sustain",
	}, { // 7
		"RoundMode",
		"Off",
		"Round",
	}, { // 8
		"InsertMode",
		"InsertOnly",
		"Rehearse",
		"Replace",
		"Substitute",
		"Reverse",
		"Half",
		"Sustain",
	}, { // 9
		"MuteMode",
		"Continuous",
		"Start",
	}, { // 10
		"Overflow",
		"Play",
		"Stop",
	}, { // 11
		"MoreLoops",
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
		"9",
		"10",
		"11",
		"12",
		"13",
		"14",
		"15",
		"16",
	}, { // 12
		"AutoRecord",
		"Off",
		"On",
	}, { // 13
		"Next LoopCopy",
		"Off",
		"Timing",
		"Sound",
	}, { // 14
		"SwitchQuant",
		"Off",
		"Confirm",
		"CycleQuantize",
		"ConfirmCycle",
		"LoopQuant",
		"ConfirmLoop",
	}, { // 15
		"Velocity",
		"Off",
		"On",
	}, { // 16 
		"SamplerStyle",
		"Run",
		"Once",
		"Start",
		"Attack",
	}, { // 17
		"Tempo",
	}
};
}
