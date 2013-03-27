package cz.muni.fi.xtovarn.heimdall.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientProtocolContext;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class DefaultClientHandler extends SimpleChannelHandler {

	private ClientFSM clientStateMachine = new ClientFSM(null);
	private ClientProtocolContext clientProtocolContext;

	private ResultFuture<Boolean> channelConnectedResult = new ResultFuture<>();

	public DefaultClientHandler(ClientProtocolContext clientProtocolContext) {
		this.clientProtocolContext = clientProtocolContext;
	}

	public ClientProtocolContext getClientProtocolContext() {
		return clientProtocolContext;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		clientStateMachine.readSymbol(ClientEvent.NETTY_TCP_CONNECTED);
		channelConnectedResult.put(true);
	}

	private ClientEvent directiveToClientEvent(Directive directive) {
		switch (directive) {
		case CONNECTED:
			return ClientEvent.RECEIVED_CONNECTED;
		case ERROR:
			return ClientEvent.ERROR;
		case ACK:
			return ClientEvent.RECEIVED_ACK;
		default:
			return null;
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();

		ClientEvent clientEvent = directiveToClientEvent(message.getDirective());
		if (clientEvent == null || this.clientStateMachine.isEnded()
				|| this.clientStateMachine.getNextState(clientEvent) == null) {
			// TODO - use checked exception?
			throw new IllegalStateException("Unexpected response from server");
		}

		// it's crucial to change the state before calling ProtocolContext
		// method, otherwise the user of the client may get the result
		// (Future<>) before the state is changed and call another
		// client method, which requires the new state, causing it to fail
		clientStateMachine.readSymbol(clientEvent);
		
		switch (message.getDirective()) {
		case CONNECTED:
			clientProtocolContext.connectResponse(e);
			break;
		case ERROR:
			clientProtocolContext.errorResponse(e);
			break;
		case ACK:
			clientProtocolContext.ackResponse(e);
			break;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause().getClass().equals(java.net.ConnectException.class)) {
			System.err.println("Connection refused...");
			System.exit(-1);
		} else {
			super.exceptionCaught(ctx, e);
		}
	}

	public ClientFSM getClientStateMachine() {
		return clientStateMachine;
	}

	public ResultFuture<Boolean> getChannelConnectedResult() {
		return channelConnectedResult;
	}

}
