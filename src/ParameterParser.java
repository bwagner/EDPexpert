import java.lang.invoke.MethodHandles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

interface ParameterParser {
	public String parse(final byte[] msg, final int OFFSET, final int parameterIndex);
}

abstract class AbstractParameterParser implements ParameterParser {

	public AbstractParameterParser(final String[][] allSymbols) {
		fParameterStrings = allSymbols;
	}
	public final String parse(
		final byte[] msg,
		final int OFFSET,
		final int parameterIndex) {
		final StringBuffer txt = new StringBuffer();
		txt.append(fParameterStrings[parameterIndex][0]);
		txt.append(":");
		final int value = msg[OFFSET] & 0xFF;
		txt.append(doDetermineValue(parameterIndex, value));
		txt.append("\n");
		return txt.toString();
	}

	public abstract String doDetermineValue(final int parameterIndex, final int value);

	protected final String[][] fParameterStrings;
}

class DefaultParameterParser extends AbstractParameterParser {
	public DefaultParameterParser(final String[][] allSymbols) {
		super(allSymbols);
	}
	public String doDetermineValue(final int parameterIndex, final int value) {
		return fParameterStrings[parameterIndex][value + 1];
	}
}

class TempoParameterParser extends AbstractParameterParser {
	public TempoParameterParser(final String[][] allSymbols) {
		super(allSymbols);
	}
	public String doDetermineValue(final int parameterIndex, final int value) {
		return value == 0 ? "Off" : Integer.toString(24 + 2 * value);
	}
}

class EighthPerCycleParameterParser extends AbstractParameterParser {
	public EighthPerCycleParameterParser(final String[][] allSymbols) {
		super(allSymbols);
	}
	public String doDetermineValue(final int parameterIndex, final int value) {
		if (value + 1 > fParameterStrings[parameterIndex].length - 1) {
			return ""
				+ (value + 1 - fParameterStrings[parameterIndex].length + 1);
		} else {
			return fParameterStrings[parameterIndex][value + 1];
		}
	}
}

class InfoDataCmdParser extends AbstractCmdParser {
	
	private static final int INFO_DATA_LEN = 13;
	
	InfoDataCmdParser(final int cmd) {
		super(cmd, INFO_DATA_LEN);
	}

	/* (non-Javadoc)
	 * @see EDPSysex.CmdParser#parse(byte[])
	 */
	public String parse(final byte[] msg, final int OFFSET) {
		LOGGER.trace("5. InfoDataCmdParser.parse");
		final StringBuffer txt = new StringBuffer();
		int index = OFFSET;
		txt.append("version:");
		txt.append(Integer.toHexString(msg[index++]));
		txt.append(" ");
		txt.append("soundmemory size:");
		txt.append(Integer.toHexString(msg[index++]));
		txt.append(" ");
		txt.append(Integer.toHexString(msg[index++]));
		txt.append(" ");
		txt.append(Integer.toHexString(msg[index++]));
		txt.append(" ");
		return txt.toString();
	}
	
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
}

class DefaultGlobalParamDataParser extends AbstractParameterParser {
	public DefaultGlobalParamDataParser(final String[][] names) {
		super(names);
	}

	/* (non-Javadoc)
	 * @see EDPSysex.AbstractParameterParser#doDetermineValue(int, int)
	 */
	public String doDetermineValue(final int parameterIndex, final int value) {
		return "" + value;
	}
}

class ControlSourceGlobalParamDataParser extends AbstractParameterParser {
	public ControlSourceGlobalParamDataParser(final String[][] names) {
		super(names);
	}

	/* (non-Javadoc)
	 * @see EDPSysex.AbstractParameterParser#doDetermineValue(int, int)
	 */
	public String doDetermineValue(final int parameterIndex, final int value) {
		return (fParameterStrings[parameterIndex][value + 1]);
	}
}

class MidiChannelGlobalParamDataParser extends AbstractParameterParser {
	public MidiChannelGlobalParamDataParser(final String[][] names) {
		super(names);
	}

	/* (non-Javadoc)
	 * @see EDPSysex.AbstractParameterParser#doDetermineValue(int, int)
	 */
	public String doDetermineValue(final int parameterIndex, final int value) {
		return "" + (value + 1);
	}
}

class GlobalParamDataCmdParser extends AbstractCmdParser {
	
	private static final int GLOBAL_PARAM_LEN = 23;

	GlobalParamDataCmdParser(final int cmd) {
		super(cmd, GLOBAL_PARAM_LEN);
	}

	/* (non-Javadoc)
	 * @see EDPSysex.CmdParser#parse(byte[])
	 */
	public String parse(final byte[] msg, final int OFFSET) {
		final StringBuffer txt = new StringBuffer();
		int index = OFFSET;

		final DefaultGlobalParamDataParser dgpdp =
			new DefaultGlobalParamDataParser(
				EDPParameterStrings.fgGlobalParameters);
		final ParameterParser[] PARSERS =
			{
				dgpdp,
				dgpdp,
				new MidiChannelGlobalParamDataParser(
					EDPParameterStrings.fgGlobalParameters),
				new ControlSourceGlobalParamDataParser(
					EDPParameterStrings.fgGlobalParameters),
				dgpdp,
				dgpdp,
				dgpdp,
				dgpdp,
				dgpdp,
				dgpdp,
				dgpdp,
				};

		txt.append("from:");
		final int FROM = msg[index++];
		txt.append(FROM);
		txt.append(" ");
		txt.append("length:");
		final int LENGTH = msg[index++];
		txt.append(LENGTH);
		txt.append(" ");
		txt.append("pset:");
		txt.append(msg[index++]);
		txt.append(" ");
		txt.append("\n");
		final int MY_OFFSET = index;
		for (int i = 0; i < LENGTH; i++) {
			final int idx = i + FROM;
			txt.append(PARSERS[idx].parse(msg, MY_OFFSET + i, idx));
		}
		return txt.toString();
	}
}
