import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

/*
 * Created on Sep 13, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */
interface CmdParser {
	static final int LENGTH_IRRELEVANT = -1;
	String parse(final byte[] msg, final int OFFSET);
	boolean lengthCorrect(final int length);
	String lastError();
}
	
abstract class AbstractCmdParser implements CmdParser {
	AbstractCmdParser(final int cmd, final int expectedLength){
		LogManager.getLogger(MethodHandles.lookup().lookupClass()).info("adding " + cmd + " " + this);
		fExpectedLength = expectedLength;
		CmdParserStore.instance().putParser(cmd, this);
	}
	AbstractCmdParser(final int cmd){
		this(cmd, LENGTH_IRRELEVANT);
	}
	public boolean lengthCorrect(final int length){
		boolean lenCorrect = fExpectedLength == LENGTH_IRRELEVANT || 
			length == fExpectedLength;
		if(!lenCorrect){
			fLastError = "Length incorrect. Expected:"+fExpectedLength+
			", but was:"+length;
		}
		return lenCorrect;
	}
	public String lastError(){
		return fLastError;
	}
	private String fLastError = "";
	final private int fExpectedLength;
}
	
class LocalParamDataCmdParser extends AbstractCmdParser {
	
//	private static final int LOCAL_PARAM_LEN = 30 or 31;
	
	// when requested via LOCAL_PARAM_REQUEST LOCAL_PARAM_LEN is 30
	// because from is 1 and length is 18
// f0 ... 13 01 12 00 00 00 00 03 00 02 00 00 02 01 00 03 00 00 05 00 02 00 f7 

	// when requested via ALL_PARAM_REQUEST LOCAL_PARAM_LEN is 31
	// because from is 0 and length is 19
// f0 ... 13 00 13 00 00 00 00 00 03 00 02 00 00 02 01 00 03 00 00 05 00 02 00 f7 (31 bytes) System Exclusive LOCAL_PARAM_DATA_CMD 

	LocalParamDataCmdParser(final int cmd){
//		super(cmd, LOCAL_PARAM_LEN); // so we can't rely on the length!
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see EDPSysex.CmdParser#parse(byte[], int)
	 */
	public String parse(final byte[] msg, final int OFFSET) {
		final StringBuffer txt = new StringBuffer();
		final DefaultParameterParser dpp = new DefaultParameterParser(EDPParameterStrings.fgLocalParameters);
		final ParameterParser[] PARSERS = {
			dpp, // Loop/Delay
			dpp, // Time Quantize
			new EighthPerCycleParameterParser(EDPParameterStrings.fgLocalParameters),
			dpp, // SyncMode
			dpp, // TrigThreshold
			dpp, // RecordMode
			dpp, // OverdubMode
			dpp, // RoundMode
			dpp, // InsertMode
			dpp, // MuteMode
			dpp, // Overflow
			dpp, // MoreLoops
			dpp, // AutoRecord
			dpp, // Next LoopCopy
			dpp, // SwitchQuant
			dpp, // Velocity 
			dpp, // SamplerStyle
			new TempoParameterParser(EDPParameterStrings.fgLocalParameters)
		};
		int index = OFFSET;
		txt.append("\nfrom:");
		int from = msg[index++];
		final int FROM = from > 0 ? from : 1;
		txt.append(FROM);
		txt.append(" ");
		txt.append("length:");
		int length = msg[index++];
		final int LENGTH = from > 0 ? length : length - 1;
		txt.append(LENGTH);
		txt.append(" ");
		txt.append("pset:");
		final int PSET = msg[index++];
		txt.append(PSET);
		txt.append(" ");
			
		// indicates where we start to parse the bytes in msg.
		final int MY_OFFSET = index + (from > 0 ? 0 : 1); // correction for irrelevant first byte when starting from 0
			
		MidiMessages.writeMsg(msg, txt, MY_OFFSET, LENGTH);
			
		for(int i=0;i<LENGTH;i++){
			final int idx = i + FROM -1;
			txt.append(PARSERS[idx].parse(msg, MY_OFFSET + i, idx));
		}
		return txt.toString();
	}
}

class CmdParserStore {
	
	public CmdParser getParser(final int command){
		return (CmdParser) fCmdParsers.get(command);
	}
	
	private Map<Integer, CmdParser> fCmdParsers = new HashMap<Integer, CmdParser>();

	private CmdParserStore() {
		
	}

	public static CmdParserStore instance() {
		if(fgInstance == null){
			fgInstance = new CmdParserStore();
		}
		return fgInstance;
		
	}
	
	private static CmdParserStore fgInstance;

	/**
	 * @param cmd
	 * @param parser
	 */
	public void putParser(final int cmd, final AbstractCmdParser parser) {
		fCmdParsers.put(cmd, parser);
		
	}

	static {
		LogManager.getLogger(MethodHandles.lookup().lookupClass()).info("static section of CmdParserStore");
		// force creation of these parsers so they will be in the COMMANDS map of parsers.
		new InfoDataCmdParser(EDPParameterStrings.INFO_DATA_CMD);
		new GlobalParamDataCmdParser(EDPParameterStrings.GLOBAL_PARAM_DATA_CMD);
		new LocalParamDataCmdParser(EDPParameterStrings.LOCAL_PARAM_DATA_CMD);
	}

}
