package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreIOLayer;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreImpl;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.localserver.LocalSocketServer;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.DefaultPipelineFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;

import java.io.IOException;

public class Server {

	private static EventStoreIOLayer eventStoreIOLayer;
	private static NettyServer nettyServer;
	private static LocalSocketServer localSocketServer;


	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));

		eventStoreIOLayer = new EventStoreIOLayer();
		SecureChannelGroup scg = new SecureChannelGroup();
		nettyServer = new NettyServer(scg);
		Dispatcher dispatcher = new Dispatcher(scg);
		EventStore eventStore = new EventStoreImpl(eventStoreIOLayer);
		PipelineFactory pipelineFactory = new DefaultPipelineFactory(eventStore, dispatcher);
		localSocketServer = new LocalSocketServer(pipelineFactory);

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
		eventStoreIOLayer.start();
		nettyServer.start();
		localSocketServer.start();
	}

	static void stop() {
		eventStoreIOLayer.stop();
		nettyServer.stop();
		localSocketServer.stop();
	}
}
