import java.lang.invoke.MethodHandles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Terminable implements Runnable {

	private boolean fStay = true;

	// check every PERIOD_MILLISECONDS whether someone wants us to stop
	private long fPeriodMilliseconds;

	public Terminable(Closeable closeable, long periodMilliseconds) {
		fMyCloseable = closeable;
		fPeriodMilliseconds = periodMilliseconds;
		start();
	}

	public void terminate() {
		fStay = false;
		try {
			fThread.join();
		} catch (InterruptedException e) {
			LOGGER.error(Utils.getStackTrace(e));
		}		
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		// TODO: I'm not sure this is the right way to implement it
		// does the sleeping cause MIDI data to be lost?
		// or does a too short period cause MIDI data to be lost?
		// what I don't like about this solution is that it's actually polling.
		while (stay()) {
			try {
				Thread.sleep(fPeriodMilliseconds);
			} catch (InterruptedException e) {
				LOGGER.error("interrupted");
			}
		}
		fMyCloseable.close();
		LOGGER.info("Terminable finished");
	}

	/**
	 * @return
	 */
	private boolean stay() {
		return fStay;
	}

	/* (non-Javadoc)
	 * @see Terminable#setThread(java.lang.Thread)
	 */
	private void setThread(Thread thread) {
		fThread = thread;
		fThread.start();
	}

	/* (non-Javadoc)
	 * @see Terminable#start()
	 */
	private void start() {
		setThread(new Thread(this));
	}

	private Thread fThread;

	private Closeable fMyCloseable;

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
}
