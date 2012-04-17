package cz.muni.fi.xtovarn.heimdall.runnable;

import cz.muni.fi.xtovarn.heimdall.netty.ServerPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.picocontainer.Startable;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class NettyServer implements Startable {

	@Override
	public void start() {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ServerPipelineFactory());

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		bootstrap.bind(new InetSocketAddress(6869));
	}

	@Override
	public void stop() {
		System.out.println("how to stop?");
	}
}
