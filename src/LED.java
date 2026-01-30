import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author bwagner
 * 
 * Resources are a ridiculous fucking bitch.
 * To get them working from within the IDE *and* inside a jar, I currently have
 * this strategy:
 * 1. put your resources into a project-top-level-folder "resources"
 * 2. Retrieve them in the Java source code via the paths: resources/__MY_RESOURCE_
 * URL onImageUrl = currentClass.getClassLoader().getResource("resources/__MY_RESOURCE_");
 * fOnIcon = new ImageIcon(onImageUrl);
 * 3. add the top-level *folder* to the classpath in the IDE run-configuration
 * (Via Run>Run Configurations>Class Path>User Entries>Advanced>Add Folders>__MY_PROJECT_TOP_FOLDER_.
 * 4. In your build.xml within the jar-task add:
 * <fileset dir="${dir.workspace}/EDPexpert" includes="resources/**"/>
 
 *
 */
class LED {
	private JLabel fLabel;
	private ImageIcon fOnIcon = null;
	private ImageIcon fOffIcon = null;
	private boolean fState;
	private Timer fTimer;
	private TimerTask fTimerTask;

	public static final boolean ON = true;
	public static final boolean OFF = false;
	
	private static final String ON_IMG = "resources/LEDgrn1cm.jpg";
	private static final String OFF_IMG = "resources/LEDoff1cm.jpg";

	static final Class<?> currentClass = new Object() { }.getClass().getEnclosingClass();
	
	public LED(boolean state) {
		final URL onImageUrl = currentClass.getClassLoader().getResource(ON_IMG);
		if (onImageUrl == null) {
			LOGGER.fatal("resource not found: " + ON_IMG);
		}
		final URL offImageUrl = currentClass.getClassLoader().getResource(OFF_IMG);
		if (onImageUrl == null) {
			LOGGER.fatal("resource not found: " + OFF_IMG);
		}
		fLabel = new JLabel();
		fOnIcon = new ImageIcon(onImageUrl);
		fOffIcon = new ImageIcon(offImageUrl);
		this.fState = !state;
		setState(state);
		fTimer = new Timer();
		fTimerTask = new MyTimerTask(this);
	}
	
	public void toggleState(){
		setState(!fState);
	}

	public void setState(boolean state) {
		if (this.fState == state) {
			return;
		}

		this.fState = state;
		fLabel.setIcon(state ? fOnIcon : fOffIcon);
		fLabel.repaint();
	}
	
	public JLabel getLabel(){
		return fLabel;
	}

	public boolean getState() {
		return fState;
	}
	
	public void flash() {
//		LOGGER.info("flash");
		setState(true);
		fTimerTask.cancel();
		fTimerTask = null;
		fTimerTask = new MyTimerTask(this);
		final long MILLISECS = 80;
		fTimer.schedule(fTimerTask, MILLISECS);
	}
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
}

class MyTimerTask extends TimerTask {
	
	MyTimerTask(LED led){
		fLed = led;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
//		LOGGER.info("turning led off");
		fLed.setState(false);
	}
	
	private LED fLed;
}
