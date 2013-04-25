package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.je.DatabaseException;

import java.io.File;
import java.io.IOException;

/**
 * Used to start the server directly (from the command line)
 */
public class Application {

	public static final String DATABASE_PATH = "./database/events";
	public static NgmonServer ngmonServer;

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));
		ngmonServer = new NgmonServer(new File(DATABASE_PATH));

		System.out.println("Heimdall is starting...");
		ngmonServer.start();
	}

	static class ShutdownHandler implements Runnable {
		@Override
		public void run() {
			System.out.println("Shutting down...");
			ngmonServer.stop();
		}
	}

}
