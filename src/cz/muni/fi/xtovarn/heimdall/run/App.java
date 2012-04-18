package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;

import java.io.IOException;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {
		/*Thread.sleep(10000);

		final SimpleFileReader socketServer = new SimpleFileReader(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3));

		class ShutdownHandler implements Runnable {
			@Override
			public void run() {
				socketServer.shutdown();
			}
		}

		System.out.println("Heimdall is starting...");
		socketServer.start();
		Executors.newSingleThreadExecutor().execute(new NettyServer());

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));*/
	}


}

