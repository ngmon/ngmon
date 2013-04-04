package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;
import cz.muni.fi.xtovarn.heimdall.commons.Startable;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NettyServer implements Startable {

	private final SecureChannelGroup secureChannelGroup;
	private final SubscriptionManager subscriptionManager;
	public final static int SERVER_PORT = 6000;

	private ServerBootstrap bootstrap;

//	@Inject
	public NettyServer(SecureChannelGroup secureChannelGroup, SubscriptionManager subscriptionManager) {
		this.secureChannelGroup = secureChannelGroup;
		this.subscriptionManager = subscriptionManager;
	}

	@Override
	public void start() {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newSingleThreadExecutor(), Executors.newCachedThreadPool(), 2);

		bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ServerPipelineFactory(secureChannelGroup, subscriptionManager));

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		bootstrap.bind(new InetSocketAddress(SERVER_PORT));

		System.out.println(getClass().getCanonicalName() + "started");
	}

	@Override
	public void stop() {
		System.out.println("Closing " + this.getClass() + "...");

		try {
			secureChannelGroup.close().await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bootstrap.releaseExternalResources();

		System.out.println(this.getClass() + " closed!");
	}
}
