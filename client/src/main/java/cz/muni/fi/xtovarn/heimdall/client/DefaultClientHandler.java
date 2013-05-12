package cz.muni.fi.xtovarn.heimdall.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientFSM;
import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientProtocolContext;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class DefaultClientHandler extends SimpleChannelHandler {

	private ClientFSM clientStateMachine = new ClientFSM(null);
	private ClientProtocolContext clientProtocolContext;
	private EventReceivedHandler eventReceivedHandler = null;
	private ServerResponseExceptionHandler exceptionHandler = null;
	private boolean disconnected = false;
	
	private static Logger logger = LogManager.getLogger(DefaultClientHandler.class);

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

	private ClientEvent directiveToClientEvent(Directive directive, ClientEvent lastRequest) {
		switch (directive) {
		case CONNECTED:
			return ClientEvent.RECEIVED_CONNECTED;
		case ERROR:
			return ClientEvent.ERROR;
		case ACK: {
			switch (lastRequest) {
			case REQUEST_READY:
				return ClientEvent.RECEIVED_ACK_FOR_READY;
			case REQUEST_DISCONNECT:
				return ClientEvent.RECEIVED_DISCONNECTED;
			default:
				return ClientEvent.RECEIVED_ACK;
			}
		}
		default:
			return null;
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (disconnected)
			return;
		
		SimpleMessage message = (SimpleMessage) e.getMessage();
		Directive directive = message.getDirective();

		// sensor event?
		if (directive.equals(Directive.SEND_SMILE) || directive.equals(Directive.SEND_JSON)) {
			if (eventReceivedHandler != null)
				eventReceivedHandler.handleEvent(JSONEventMapper.bytesToEvent(message.getBody()));
			return;
		}

		ClientEvent clientEvent = directiveToClientEvent(directive, clientProtocolContext.getLastRequest());
		if (clientEvent != null && clientEvent.equals(ClientEvent.RECEIVED_DISCONNECTED)) {
			disconnected = true;
			clientProtocolContext.disconnectResponse();
			return;
		}
		
		if (clientEvent == null || this.clientStateMachine.isEnded()
				|| this.clientStateMachine.getNextState(clientEvent) == null) {
			// TODO - use checked exception?
			if (exceptionHandler != null)
				exceptionHandler.handleException(new IllegalStateException("Unexpected response from server"));
			return;
		}

		// it's crucial to change the state before calling ProtocolContext
		// method, otherwise the user of the client may get the result
		// (Future<>) before the state is changed and call another
		// client method, which requires the new state, causing it to fail
		clientStateMachine.readSymbol(clientEvent);

		switch (directive) {
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
		if (disconnected)
			return;
		
		if (e.getCause().getClass().equals(java.net.ConnectException.class)) {
			logger.error("Connection refused...");
			System.exit(-1);
		} else {
			if (exceptionHandler == null)
				super.exceptionCaught(ctx, e);
			else
				exceptionHandler.handleException(e.getCause());
		}
	}

	public ClientFSM getClientStateMachine() {
		return clientStateMachine;
	}

	public ResultFuture<Boolean> getChannelConnectedResult() {
		return channelConnectedResult;
	}

	public void setEventReceivedHandler(EventReceivedHandler handler) {
		this.eventReceivedHandler = handler;
	}

	public void setServerResponseExceptionHandler(ServerResponseExceptionHandler handler) {
		this.exceptionHandler = handler;
	}

}
