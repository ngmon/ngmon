package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.runnable.NettyServer;
import cz.muni.fi.xtovarn.heimdall.runnable.SocketServer;

import java.io.IOException;
import java.util.concurrent.Executors;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {
		final SocketServer socketServer = new SocketServer(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3));

		class ShutdownHandler implements Runnable {
			@Override
			public void run() {
				socketServer.shutdown();
			}
		}

		System.out.println("Heimdall is starting...");
		socketServer.start();
		Executors.newSingleThreadExecutor().execute(new NettyServer());

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));
	}


}

