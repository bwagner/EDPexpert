import java.lang.invoke.MethodHandles;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Created on Sep 2, 2004
 *
 */

/**
 * @author bernhard.wagner
 *
 */
public class Interactive{
	
	public Interactive(){
		fCommands = new HashMap<String, Command>();
		fCommands.put("?", new AbstractCommand("for help"){
			public void doIt() {
				displayHelp();
			}

		});
		fCommands.put("x", new AbstractCommand("to terminate"){
			public void doIt() {
				fStay = false;
			}
		});
	}
	
	public void resetCommands(){
		fCommands.clear();
	}
	
	public Interactive addCommand(final String key, final Command command){
		fCommands.put(key, command);
		return this;
	}

	public void enterLoop(){
		fStay = true;
		while(fStay) {
			System.out.println("enter ? for help");
			String read = "";
			try {
				read = new BufferedReader(new InputStreamReader(System.in)).readLine();
				//System.in.skip(100);
			} catch (IOException e) {
				LOGGER.error(Utils.getStackTrace(e));
			}
			final Command cmd = (Command)fCommands.get(read);
			if(cmd == null){
				System.out.println("unknown command:"+read);
			} else {
				cmd.doIt();
			}
		}
		System.out.println("end.");
	}
	
	private void displayHelp(){
		final Iterator<String> it = fCommands.keySet().iterator();
		while(it.hasNext()){
			final String key = (String)it.next();
			if(key != "?") { // don't display help about help
				System.out.println("enter "+ key + " "+((Command)fCommands.get(key)).getDescription());
			}
		}
	}

	public static void main(final String[] args) {
		new Interactive().enterLoop();
	}
	
	public void terminate(){
		fStay = false;
	}
	
	private Map<String, Command> fCommands;
	private boolean fStay = true;
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

}
