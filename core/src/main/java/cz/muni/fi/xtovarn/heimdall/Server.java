package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.je.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.localserver.LocalSocketServer;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.DefaultPipelineFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
import cz.muni.fi.xtovarn.heimdall.storage.EventStore;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.DefaultEnvironment;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.EventDataAccessor;

import java.io.File;
import java.io.IOException;

public class Server {

	private static DefaultEnvironment defaultEnvironment;
	private static NettyServer nettyServer;
	private static LocalSocketServer localSocketServer;
	public static final String DATABASE_PATH = "./database/events";


	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));

		defaultEnvironment = new DefaultEnvironment();
		SecureChannelGroup scg = new SecureChannelGroup();
		nettyServer = new NettyServer(scg);
		Dispatcher dispatcher = new Dispatcher(scg);
		EventStore eventStore = new EventDataAccessor(defaultEnvironment.setup(new File(DATABASE_PATH)));
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
		nettyServer.start();
		localSocketServer.start();
	}

	static void stop() {
		defaultEnvironment.close();
		nettyServer.stop();
		localSocketServer.stop();
	}
}
