package cz.muni.fi.xtovarn.heimdall.client.test.util;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sleepycat.je.DatabaseException;

import cz.muni.fi.xtovarn.heimdall.NgmonServer;

public class NgmonLauncher {

	public File DATABASE_PATH;
	public static NgmonServer ngmonServer;

	private static long WAIT_IN_MILLIS = 1000;
	
	private static Logger logger = LogManager.getLogger(NgmonLauncher.class);

	public NgmonLauncher(File baseDirectory) {
		this.DATABASE_PATH = baseDirectory;
	}

	private static class NgmonStarter implements Runnable {

		private NgmonServer ngmonServer;

		public NgmonStarter(NgmonServer ngmonServer) {
			this.ngmonServer = ngmonServer;
		}

		@Override
		public void run() {
			ngmonServer.start();
		}
	}

	public void start() throws IOException, DatabaseException, InterruptedException {
		ngmonServer = new NgmonServer(DATABASE_PATH);
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler(ngmonServer)));

		logger.info("Heimdall is starting...");
		(new Thread(new NgmonStarter(ngmonServer))).start();

		Thread.sleep(WAIT_IN_MILLIS);
	}

	public void stop() {
		logger.info("Shutting down...");
		ngmonServer.stop();
	}

}
