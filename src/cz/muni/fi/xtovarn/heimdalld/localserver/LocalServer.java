package cz.muni.fi.xtovarn.heimdalld.localserver;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.picocontainer.Startable;

public class LocalServer implements Startable {

	private ServerBootstrap bootstrap;
	private final Resender resender;

	public LocalServer(Resender resender) {
		this.resender = resender;
	}

	@Override
	public void start() {
		ChannelFactory factory = new DefaultLocalServerChannelFactory();

		bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new LocalServerPipelineFactory(resender));

		Channel channel = bootstrap.bind(new LocalAddress(1)); // fake address
	}

	@Override
	public void stop() {
		bootstrap.releaseExternalResources();
	}
}
