package cz.muni.fi.xtovarn.heimdall.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import cz.muni.fi.xtovarn.heimdall.commons.Startable;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;

/**
 * The network component of the Ngmon server, implemented using Netty framework
 */
public class NettyServer implements Startable {

	private final SecureChannelGroup secureChannelGroup;
	private final SubscriptionManager subscriptionManager;
	public final static int SERVER_PORT = 6000;

	private ServerBootstrap bootstrap;
	
	private static Logger logger = LogManager.getLogger(NettyServer.class);

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

		logger.info(getClass().getCanonicalName() + "started");
	}

	@Override
	public void stop() {
		logger.info("Closing " + this.getClass() + "...");

		try {
			secureChannelGroup.close().await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bootstrap.releaseExternalResources();

		logger.info(this.getClass() + " closed!");
	}
}
