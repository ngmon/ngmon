package cz.muni.fi.xtovarn.heimdall.client;

import java.io.IOException;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientContext;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;

public class DefaultClientHandler extends SimpleChannelHandler {

	private ClientFSM clientStateMachine = new ClientFSM(null);
	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		clientStateMachine.readSymbol(ClientEvent.NETTY_TCP_CONNECTED, null);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();

		switch (message.getDirective()) {
		case CONNECTED:
			handleConnected(ctx, e);
			break;
		case ERROR:
			clientStateMachine.readSymbol(ClientEvent.ERROR, null);
			break;
		case ACK:
			// TODO - check current machine state and decide which symbol to
			// read accordingly
			clientStateMachine.readSymbol(ClientEvent.RECEIVED_SUBSCRIBE_ACK, new ClientContext(ctx, e, null));
		}
	}

	private void readError(ChannelHandlerContext ctx, MessageEvent e) {
		clientStateMachine.readSymbol(ClientEvent.ERROR, new ClientContext(ctx, e, null));
	}

	private void handleConnected(ChannelHandlerContext ctx, MessageEvent e) {
		System.out.println(ClientEvent.RECEIVED_CONNECTED);
		SimpleMessage message = (SimpleMessage) e.getMessage();
		try {
			Map<String, Number> connectionIdMap = (Map<String, Number>) mapper.readValue(message.getBody(), Map.class);
			Long connectionId = connectionIdMap.get(Constants.CONNECTION_ID_TITLE).longValue();
			if (connectionId != null) {
				ClientContext actionContext = new ClientContext(ctx, e, null);
				actionContext.setObject(connectionId);
				clientStateMachine.readSymbol(ClientEvent.RECEIVED_CONNECTED, actionContext);
				return;
			}
		} catch (IOException ex) {
		}

		readError(ctx, e);

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

}
