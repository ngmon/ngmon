package cz.muni.fi.xtovarn.heimdall.guice;

import com.google.inject.AbstractModule;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreIOLayer;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreImpl;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.localserver.LocalSocketServer;
import cz.muni.fi.xtovarn.heimdall.localserver.Resender;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.DefaultPipelineFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;

public class HeimdallModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(EventStoreIOLayer.class).asEagerSingleton();
		bind(Dispatcher.class).asEagerSingleton();
		bind(SecureChannelGroup.class).asEagerSingleton();
		bind(NettyServer.class).asEagerSingleton();
		bind(Resender.class).asEagerSingleton();
		bind(LocalSocketServer.class).asEagerSingleton();
		bind(EventStore.class).to(EventStoreImpl.class);
		bind(PipelineFactory.class).to(DefaultPipelineFactory.class);
	}
}
