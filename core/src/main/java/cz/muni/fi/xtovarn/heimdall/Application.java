package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.je.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.util.NgmonLauncher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
/**
 * Used to start the server directly (from the command line)
 */
public class Application {

	public static final File DATABASE_PATH = new File("db");
	public static NgmonLauncher ngmonLauncher;
	
	private static Logger logger = LogManager.getLogger(Application.class);

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {
        ngmonLauncher = new NgmonLauncher(DATABASE_PATH);

        ngmonLauncher.start();
	}
}