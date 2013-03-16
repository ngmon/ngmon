package cz.muni.fi.xtovarn.heimdall.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientContext;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.entities.User;

public class ClientApi {

	private ChannelFactory factory;
	private ClientBootstrap bootstrap;
	private Channel channel = null;
	private DefaultClientHandler clientHandler = null;
	private ClientFSM clientFSM = null;

	public ClientApi() {
		factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(),
				Executors.newSingleThreadExecutor());

		bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new ClientPipelineFactory());

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(6000));
		future.awaitUninterruptibly();

		channel = future.getChannel();

		clientHandler = (DefaultClientHandler) channel.getPipeline().getContext(Constants.DEFAULT_CLIENT_HANDLER_TITLE)
				.getHandler();
		clientFSM = clientHandler.getClientStateMachine();
	}

	public void connect(String login, String passcode) {
		if (login == null || passcode == null || login.isEmpty() || passcode.isEmpty())
			throw new IllegalArgumentException("connect()");
		User user = new User(login, passcode);
		// channel.write(new SimpleMessage(Directive.CONNECT,
		// mapper.writeValueAsBytes(user)));

		// TODO - workaround
		ClientMessageEvent messageEvent = new ClientMessageEvent(channel, null, null, null);
		ClientContext actionContext = new ClientContext(null, messageEvent, null);
		actionContext.setObject(user);
		clientFSM.readSymbol(ClientEvent.REQUEST_CONNECT, actionContext);
	}

	public Long getConnectionId() {
		return clientFSM.getConnectionId();
	}

	public boolean isConnected() {
		return getConnectionId() != null;
	}

	public void stop() {
		channel.getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
	}

}
