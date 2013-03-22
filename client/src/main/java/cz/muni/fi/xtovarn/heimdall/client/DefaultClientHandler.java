package cz.muni.fi.xtovarn.heimdall.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientProtocolContext;
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

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();

		// it's crucial to change the state before calling ProtocolContext
		// method, otherwise the user of the client may get the result
		// (Future<>) before the state is changed and call another
		// client method, which requires the new state, causing it to fail
		switch (message.getDirective()) {
		case CONNECTED:
			clientStateMachine.readSymbol(ClientEvent.RECEIVED_CONNECTED);
			clientProtocolContext.connectResponse(e);
			break;
		case ERROR:
			clientStateMachine.readSymbol(ClientEvent.ERROR);
			clientProtocolContext.errorResponse(e);
			break;
		case ACK:
			clientStateMachine.readSymbol(ClientEvent.RECEIVED_ACK);
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
