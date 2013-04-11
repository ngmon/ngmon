package cz.muni.fi.xtovarn.heimdall.test.util;

import java.io.File;
import java.io.IOException;

import com.sleepycat.je.DatabaseException;

import cz.muni.fi.xtovarn.heimdall.NgmonServer;

public class NgmonLauncher {

	public static final String DATABASE_PATH = "./database/events";
	public static NgmonServer ngmonServer;
	
	private static long WAIT_IN_MILLIS = 1000;

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
		ngmonServer = new NgmonServer(new File(DATABASE_PATH));
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler(ngmonServer)));
		
		System.out.println("Heimdall is starting...");
		(new Thread(new NgmonStarter(ngmonServer))).start();
		
		Thread.sleep(WAIT_IN_MILLIS);
	}

	public void stop() {
		System.out.println("Shutting down...");
		ngmonServer.stop();
	}

}
