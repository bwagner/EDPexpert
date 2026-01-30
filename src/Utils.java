import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Utils {
	
	public static String getStackTrace(Exception theException){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final PrintStream ps = new PrintStream(baos, true, "utf-8");
			theException.printStackTrace(ps);
			String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			return content;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "printing stack trace failed, see stdout";
	}

}