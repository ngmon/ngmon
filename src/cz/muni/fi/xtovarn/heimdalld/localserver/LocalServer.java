package cz.muni.fi.xtovarn.heimdalld.localserver;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.picocontainer.Startable;

public class LocalServer implements Startable {

	private ServerBootstrap bootstrap;


	@Override
	public void start() {
		ChannelFactory factory = new DefaultLocalServerChannelFactory();

		bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new LocalServerPipelineFactory(resender));

		bootstrap.bind(new LocalAddress("/dev/eventlog")); // fake address
	}

	@Override
	public void stop() {
		bootstrap.releaseExternalResources();
	}
}
