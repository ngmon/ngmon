package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RemoteClient {

	private static ClientBootstrap bootstrap;

	public static void main(String[] args) throws InterruptedException {
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor());

		bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new ClientPipelineFactory());

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(6000));
	}
}
