package cz.muni.fi.xtovarn.heimdall.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientProtocolContext;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientState;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;
import cz.muni.fi.xtovarn.heimdall.entities.User;

public class ClientApi {

	private ChannelFactory factory;
	private ClientBootstrap bootstrap;
	private Channel channel = null;
	private DefaultClientHandler clientHandler = null;
	private ClientProtocolContext clientProtocolContext = null;
	private ClientFSM clientFSM = null;

	public ClientApi(long timeout, TimeUnit unit) throws InterruptedException {
		factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(),
				Executors.newSingleThreadExecutor());

		bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new ClientPipelineFactory());

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(6000));
		// maybe not needed
		future.await(timeout, unit);

		channel = future.getChannel();

		clientHandler = (DefaultClientHandler) channel.getPipeline().getContext(Constants.DEFAULT_CLIENT_HANDLER_TITLE)
				.getHandler();
		clientProtocolContext = clientHandler.getClientProtocolContext();
		clientFSM = clientHandler.getClientStateMachine();
	}

	public Future<Boolean> getChannelConnectedResult() {
		return clientHandler.getChannelConnectedResult();
	}

	public void checkFsmState(ClientState state) {
		ClientState currentState = clientFSM.getCurrentState();
		if (!currentState.equals(state))
			throw new IllegalStateException("This operation requires the state " + state.toString()
					+ ", but the current state is " + currentState.toString());
	}

	public Future<Boolean> connect(String login, String passcode) throws InterruptedException {
		if (login == null || passcode == null || login.isEmpty() || passcode.isEmpty())
			throw new IllegalArgumentException("connect()");

		checkFsmState(ClientState.PRE_CONNECTED);

		User user = new User(login, passcode);

		clientFSM.readSymbol(ClientEvent.REQUEST_CONNECT);
		return clientProtocolContext.connectRequest(channel, user);
	}

	public Long getConnectionId() {
		return clientProtocolContext.getConnectionId();
	}

	public boolean isConnected() {
		return clientProtocolContext.isConnected();
	}

	public Future<Long> subscribe(Predicate predicate) throws InterruptedException, ExecutionException {
		checkFsmState(ClientState.CONNECTED);

		if (predicate == null || predicate.isEmpty())
			throw new IllegalArgumentException("subscribe()");

		clientFSM.readSymbol(ClientEvent.REQUEST_SUBSCRIBE);
		return clientProtocolContext.subscribeRequest(channel, predicate);
	}

	public List<Long> getSubscriptionIds() {
		return clientProtocolContext.getSubscriptionIds();
	}

	public Long getLastSubscriptionId() {
		return clientProtocolContext.getLastSubscriptionId();
	}

	public boolean wasLastSubscriptionSuccessful() {
		return clientProtocolContext.wasLastSubscriptionSuccessful();
	}

	public Future<Boolean> unsubscribe(Long subscriptionId) throws InterruptedException, ExecutionException {
		checkFsmState(ClientState.CONNECTED);

		if (subscriptionId == null)
			throw new IllegalArgumentException("unsubscribe()");

		clientFSM.readSymbol(ClientEvent.REQUEST_UNSUBSCRIBE);
		return clientProtocolContext.unsubscribeRequest(channel, subscriptionId);
	}

	public void stop() {
		ChannelFuture future = channel.close();
		future.awaitUninterruptibly();
		// channel.getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
	}

}
