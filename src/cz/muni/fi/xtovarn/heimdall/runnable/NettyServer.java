package cz.muni.fi.xtovarn.heimdall.runnable;

import cz.muni.fi.xtovarn.heimdall.netty.ServerPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.picocontainer.Startable;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class NettyServer implements Startable {

	private final ServerPipelineFactory serverPipelineFactory;
	private final static int SERVER_PORT = 6000;

	public NettyServer(ServerPipelineFactory serverPipelineFactory) {
		this.serverPipelineFactory = serverPipelineFactory;
	}

	@Override
	public void start() {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(serverPipelineFactory);

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		bootstrap.bind(new InetSocketAddress(SERVER_PORT));
	}

	@Override
	public void stop() {
		System.out.println("how to stop?");
	}
}
