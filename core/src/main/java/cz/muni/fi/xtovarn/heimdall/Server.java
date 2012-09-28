package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.storage.store.BerkeleyDBEventStoreImpl;
import cz.muni.fi.xtovarn.heimdall.storage.store.BerkeleyDBIOLayer;
import cz.muni.fi.xtovarn.heimdall.storage.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.localserver.LocalSocketServer;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.DefaultPipelineFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;

import java.io.IOException;

public class Server {

	private static BerkeleyDBIOLayer berkeleyDBIOLayer;
	private static NettyServer nettyServer;
	private static LocalSocketServer localSocketServer;


	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));

		berkeleyDBIOLayer = new BerkeleyDBIOLayer();
		SecureChannelGroup scg = new SecureChannelGroup();
		nettyServer = new NettyServer(scg);
		Dispatcher dispatcher = new Dispatcher(scg);
		EventStore eventStore = new BerkeleyDBEventStoreImpl(berkeleyDBIOLayer);
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
		berkeleyDBIOLayer.start();
		nettyServer.start();
		localSocketServer.start();
	}

	static void stop() {
		berkeleyDBIOLayer.stop();
		nettyServer.stop();
		localSocketServer.stop();
	}
}
