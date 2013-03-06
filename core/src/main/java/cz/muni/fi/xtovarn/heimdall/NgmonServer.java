package cz.muni.fi.xtovarn.heimdall;

import cz.muni.fi.xtovarn.heimdall.collector.SocketCollector;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.DefaultPipelineFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
import cz.muni.fi.xtovarn.heimdall.storage.EventStore;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.DefaultDatabaseEnvironment;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.EventDataAccessor;

import java.io.File;

public class NgmonServer {

	private final DefaultDatabaseEnvironment defaultDatabaseEnvironment;
	private final NettyServer nettyServer;
	private final SocketCollector socketCollector;

	public NgmonServer(File baseDirectory) {

		SecureChannelGroup scg = new SecureChannelGroup();
		this.nettyServer = new NettyServer(scg);
		Dispatcher dispatcher = new Dispatcher(scg);

		this.defaultDatabaseEnvironment = new DefaultDatabaseEnvironment();
		EventStore eventStore = new EventDataAccessor(defaultDatabaseEnvironment.setup(baseDirectory));

		PipelineFactory pipelineFactory = new DefaultPipelineFactory(eventStore, dispatcher);
		this.socketCollector = new SocketCollector(pipelineFactory);
	}

	public void start() {

		this.nettyServer.start();
		this.socketCollector.start();
	}

	public void stop() {

		this.defaultDatabaseEnvironment.close();
		this.nettyServer.stop();
		this.socketCollector.stop();
	}
}