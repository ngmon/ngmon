package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientProtocolContext;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientState;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Ngmon client implementation
 */
public class ClientImpl implements Client {

	private ChannelFactory factory;
	private ClientBootstrap bootstrap;
	private Channel channel = null;
	private DefaultClientHandler clientHandler = null;
	private ClientProtocolContext clientProtocolContext = null;
	/**
	 * Manages client state
	 */
	private ClientFSM clientFSM = null;
	/**
	 * True if disconnect() has been called
	 */
	private boolean disconnected = false;

	/**
	 * Connects to the Ngmon server
	 */
	public ClientImpl(long timeout, TimeUnit unit) throws InterruptedException {
		factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(),
				Executors.newSingleThreadExecutor());

		bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new ClientPipelineFactory());

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		ChannelFuture future = bootstrap.connect(new InetSocketAddress("192.168.1.101", 11000));
		// maybe not needed
		future.await(timeout, unit);

		channel = future.getChannel();

		clientHandler = (DefaultClientHandler) channel.getPipeline().getContext(Constants.DEFAULT_CLIENT_HANDLER_TITLE)
				.getHandler();
		clientProtocolContext = clientHandler.getClientProtocolContext();
		clientFSM = clientHandler.getClientStateMachine();
	}

	/**
	 * Used in ConnectionFactory when connecting to the server, this is true as
	 * soon as the connection has been established and client state has been
	 * changed accordingly (this is very important)
	 */
	public Future<Boolean> getChannelConnectedResult() {
		return clientHandler.getChannelConnectedResult();
	}

	/**
	 * Helper method for checking if the client is in a desired state; if not,
	 * exception is thrown
	 * 
	 * @param state
	 *            The required state
	 */
	private void checkFsmState(ClientState state) {
		ClientState currentState = clientFSM.getCurrentState();
		if (!currentState.equals(state))
			throw new IllegalStateException("This operation requires the state " + state.toString()
					+ ", but the current state is " + currentState.toString());
		if (disconnected)
			throw new IllegalStateException("The client is disconnected");
	}

	/**
	 * Authenticates against the Ngmon server, finishing the connection phase
	 */
	public Future<Boolean> connect(String login, String passcode) throws InterruptedException {
		if (login == null || passcode == null || login.isEmpty() || passcode.isEmpty())
			throw new IllegalArgumentException("connect()");

		checkFsmState(ClientState.PRE_CONNECTED);

		User user = new User(login, passcode);

		// set waiting state, then send the request to server
		clientFSM.readSymbol(ClientEvent.REQUEST_CONNECT);
		return clientProtocolContext.connectRequest(channel, user);
	}

	public Long getConnectionId() {
		return clientProtocolContext.getConnectionId();
	}

	public boolean isConnected() {
		return clientProtocolContext.isConnected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cz.muni.fi.xtovarn.heimdall.client.Client#subscribe(cz.muni.fi.xtovarn
	 * .heimdall.client.subscribe.Predicate)
	 */
	@Override
	public Future<Long> subscribe(Predicate predicate) throws InterruptedException, ExecutionException {
		checkFsmState(ClientState.CONNECTED);

		if (predicate == null || predicate.isEmpty())
			throw new IllegalArgumentException("subscribe()");

		// set waiting state, then send the request to server
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cz.muni.fi.xtovarn.heimdall.client.Client#unsubscribe(java.lang.Long)
	 */
	@Override
	public Future<Boolean> unsubscribe(Long subscriptionId) throws InterruptedException, ExecutionException {
		checkFsmState(ClientState.CONNECTED);

		if (subscriptionId == null)
			throw new IllegalArgumentException("unsubscribe()");

		clientFSM.readSymbol(ClientEvent.REQUEST_UNSUBSCRIBE);
		return clientProtocolContext.unsubscribeRequest(channel, subscriptionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.muni.fi.xtovarn.heimdall.client.Client#stop()
	 */
	@Override
	public void stop() {
		ChannelFuture future = channel.close();
		future.awaitUninterruptibly();
		// channel.getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
	}

	@Override
	public Future<Boolean> ready() {
		checkFsmState(ClientState.CONNECTED);

		clientFSM.readSymbol(ClientEvent.REQUEST_READY);
		return clientProtocolContext.readyRequest(channel);
	}

	@Override
	public Future<Boolean> stopSending() {
		checkFsmState(ClientState.RECEIVING);

		clientFSM.readSymbol(ClientEvent.REQUEST_STOP);
		return clientProtocolContext.stopRequest(channel);
	}

	@Override
	public void reset() {
		checkFsmState(ClientState.WAITING_FOR_ACK);

		clientFSM.rollback();
	}

	@Override
	public void setEventReceivedHandler(EventReceivedHandler handler) {
		clientHandler.setEventReceivedHandler(handler);
	}

	@Override
	public void setServerResponseExceptionHandler(ServerResponseExceptionHandler handler) {
		clientHandler.setServerResponseExceptionHandler(handler);
	}

	@Override
	public Future<Boolean> disconnect() {
		checkFsmState(ClientState.CONNECTED);

		disconnected = true;

		clientFSM.readSymbol(ClientEvent.REQUEST_DISCONNECT);
		return clientProtocolContext.disconnectRequest(channel);
	}

	@Override
	public Future<Boolean> get() {
		checkFsmState(ClientState.CONNECTED);

		clientFSM.readSymbol(ClientEvent.REQUEST_GET);
		return clientProtocolContext.getRequest(channel);
	}

}