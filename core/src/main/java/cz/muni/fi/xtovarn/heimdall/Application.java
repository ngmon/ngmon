package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.je.DatabaseException;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to start the server directly (from the command line)
 */
public class Application {

	public static final String DATABASE_PATH = "./database/events";
	public static NgmonServer ngmonServer;
	
	private static Logger logger = LogManager.getLogger(Application.class);

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));
		ngmonServer = new NgmonServer(new File(DATABASE_PATH));

		logger.info("Heimdall is starting...");
		ngmonServer.start();
	}

	static class ShutdownHandler implements Runnable {
		@Override
		public void run() {
			logger.info("Shutting down...");
			ngmonServer.stop();
		}
	}

}