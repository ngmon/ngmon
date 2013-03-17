package cz.muni.fi.xtovarn.heimdall.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientContext;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class DefaultClientHandler extends SimpleChannelHandler {

	private ClientFSM clientStateMachine = new ClientFSM(null);

	private ResultFuture<Boolean> channelConnectedResult = new ResultFuture<>();

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		clientStateMachine.readSymbol(ClientEvent.NETTY_TCP_CONNECTED, null);
		channelConnectedResult.put(true);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();

		switch (message.getDirective()) {
		case CONNECTED:
			clientStateMachine.readSymbol(ClientEvent.RECEIVED_CONNECTED, new ClientContext(ctx, e, null));
			break;
		case ERROR:
			// TODO - exception if I get error and don't have action
			// (ClientContext != null)
			clientStateMachine.readSymbol(ClientEvent.ERROR, new ClientContext(ctx, e, null));
			break;
		case ACK:
			// TODO - check current machine state and decide which symbol to
			// read accordingly
			clientStateMachine.readSymbol(ClientEvent.RECEIVED_SUBSCRIBE_ACK, new ClientContext(ctx, e, null));
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
