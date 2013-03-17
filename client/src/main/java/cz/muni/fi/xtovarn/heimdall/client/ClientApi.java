package cz.muni.fi.xtovarn.heimdall.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientContext;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;
import cz.muni.fi.xtovarn.heimdall.entities.User;

public class ClientApi {

	private ChannelFactory factory;
	private ClientBootstrap bootstrap;
	private Channel channel = null;
	private DefaultClientHandler clientHandler = null;
	private ClientFSM clientFSM = null;
	private boolean preconnected = false;
	private boolean connected = false;

	public ClientApi() {
		factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(),
				Executors.newSingleThreadExecutor());

		bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new ClientPipelineFactory());

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(6000));

		channel = future.getChannel();

		clientHandler = (DefaultClientHandler) channel.getPipeline().getContext(Constants.DEFAULT_CLIENT_HANDLER_TITLE)
				.getHandler();
		clientFSM = clientHandler.getClientStateMachine();
	}

	private ClientContext getContextFromChannel() {
		// TODO - static?
		return new ClientContext(null, new ClientMessageEvent(channel, null, null, null), null);
	}

	public boolean isConnected() throws InterruptedException, ExecutionException {
		if (!preconnected)
			return false;
		boolean result = false;
		if (!connected) {
			connected = clientFSM.getConnectResult().get();
		}

		return connected;
	}

	public Future<Boolean> connect(String login, String passcode) throws InterruptedException {
		if (login == null || passcode == null || login.isEmpty() || passcode.isEmpty())
			throw new IllegalArgumentException("connect()");

		if (!preconnected) {
			if (!clientHandler.getChannelConnectedResult().get().equals(true)) {
				throw new IllegalStateException("not preconnected");
			}
			preconnected = true;
		}

		User user = new User(login, passcode);

		// TODO - workaround
		ClientContext actionContext = getContextFromChannel();
		actionContext.setObject(user);
		clientFSM.readSymbol(ClientEvent.REQUEST_CONNECT, actionContext);

		return clientFSM.getConnectResult();
	}

	public Long getConnectionId() {
		return clientFSM.getConnectionId();
	}

	/*-public boolean isConnected() {
		return getConnectionId() != null;
	}*/

	private void checkConnected() throws InterruptedException, ExecutionException {
		if (!isConnected())
			throw new IllegalStateException("not connected");
	}

	public Future<Long> subscribe(Predicate predicate) throws InterruptedException, ExecutionException {
		checkConnected();

		if (predicate == null || predicate.isEmpty())
			throw new IllegalArgumentException("subscribe()");

		ClientContext actionContext = getContextFromChannel();
		actionContext.setObject(predicate);
		clientFSM.readSymbol(ClientEvent.REQUEST_SUBSCRIBE, actionContext);
		return clientFSM.getSubscribeResult();
	}

	public List<Long> getSubscriptionIds() {
		return clientFSM.getSubscriptionIds();
	}

	public Long getLastSubscriptionId() {
		return clientFSM.getLastSubscriptionId();
	}

	public boolean wasLastSubscriptionSuccessful() {
		return clientFSM.wasLastSubscriptionSuccessful();
	}

	public void stop() {
		ChannelFuture future = channel.close();
		future.awaitUninterruptibly();
		// channel.getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
	}

}
