/*
 * Created on Sep 2, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractCommand implements Command {

	public AbstractCommand(String helpString) {
		fHelpString = helpString;
	}

	public String getDescription() {
		return fHelpString;
	}

	private String fHelpString;

}
