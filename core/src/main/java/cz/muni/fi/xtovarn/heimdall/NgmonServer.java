package cz.muni.fi.xtovarn.heimdall;

import cz.muni.fi.xtovarn.heimdall.collector.SocketCollector;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.DefaultPipelineFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;
import cz.muni.fi.xtovarn.heimdall.storage.EventStore;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.DefaultDatabaseEnvironment;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.EventDataAccessor;

import java.io.File;

/**
 * The Ngmon server itself
 */
public class NgmonServer {

	private final DefaultDatabaseEnvironment defaultDatabaseEnvironment;
	private final NettyServer nettyServer;
	private final SocketCollector socketCollector;

	public NgmonServer(File baseDirectory) {

		SecureChannelGroup scg = new SecureChannelGroup();
		SubscriptionManager subscriptionManager = new SubscriptionManager();
		this.nettyServer = new NettyServer(scg, subscriptionManager);
		Dispatcher dispatcher = new Dispatcher(scg);

		// initialize event store
		this.defaultDatabaseEnvironment = new DefaultDatabaseEnvironment();
		EventStore eventStore = new EventDataAccessor(defaultDatabaseEnvironment.setup(baseDirectory));

		// create pipeline factory for sensor events
		PipelineFactory pipelineFactory = new DefaultPipelineFactory(eventStore, dispatcher, scg, subscriptionManager);
		this.socketCollector = new SocketCollector(pipelineFactory);
	}

	public void start() {

		// start the server itself
		this.nettyServer.start();
		// start collecting sensor events
		this.socketCollector.start();
	}

	public void stop() {

		this.defaultDatabaseEnvironment.close();
		this.nettyServer.stop();
		this.socketCollector.stop();
	}
}
