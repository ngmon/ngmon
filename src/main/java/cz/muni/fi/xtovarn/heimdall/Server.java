package cz.muni.fi.xtovarn.heimdall;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreIOLayer;
import cz.muni.fi.xtovarn.heimdall.guice.HeimdallModule;
import cz.muni.fi.xtovarn.heimdall.localserver.LocalSocketServer;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;

import java.io.IOException;

public class Server {

	private static Injector injector;

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));

		injector = Guice.createInjector(new HeimdallModule());
		start();
	}

	static class ShutdownHandler implements Runnable {
		@Override
		public void run() {
			System.out.println("Shutting down...");
			stop();
		}
	}

	static void start() {
		System.out.println("Heimdall is starting...");
		injector.getInstance(EventStoreIOLayer.class).start();
		injector.getInstance(NettyServer.class).start();
		injector.getInstance(LocalSocketServer.class).start();
	}

	static void stop() {
		injector.getInstance(LocalSocketServer.class).stop();
		injector.getInstance(NettyServer.class).stop();
		injector.getInstance(EventStoreIOLayer.class).stop();
	}
}
