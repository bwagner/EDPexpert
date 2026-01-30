import java.awt.Component;

public class LedListener implements MidiListener {
	public LedListener(int source_n_offset, int offset) {
		fcOFFSET = source_n_offset + offset;
	}
	public String notifyMidiMessage(final byte[] rawMsg) {
		if (((rawMsg[2] & 0xFF) > 0) // only note ons!
			&& ((rawMsg[1] & 0xFF) == fcOFFSET)) {
				fLed.flash();
		}
		return "";
	}

	public Component getComponent() {
		return fLed.getLabel();
	}

	private final LED fLed = new LED(false);
	private final int fcOFFSET;
}