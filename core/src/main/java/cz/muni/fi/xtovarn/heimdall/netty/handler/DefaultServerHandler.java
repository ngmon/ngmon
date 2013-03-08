package cz.muni.fi.xtovarn.heimdall.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerContext;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerEvent;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerFSM;

public class DefaultServerHandler extends SimpleChannelHandler {

	private final SecureChannelGroup secureChannelGroup;

	private final ServerFSM serverStateMachine;

	private ObjectMapper mapper = new ObjectMapper();

	public DefaultServerHandler(SecureChannelGroup secureChannelGroup) {
		this.secureChannelGroup = secureChannelGroup;
		this.serverStateMachine = new ServerFSM(this.secureChannelGroup);
	}

	private void sendError(Channel channel) {
		channel.write(new SimpleMessage(Directive.ERROR, "".getBytes()));
	}

	private ServerEvent directiveToServerEvent(Directive directive) {
		switch (directive) {
		case CONNECT:
			return ServerEvent.RECEIVED_CONNECT;
		case SUBSCRIBE:
			return ServerEvent.RECEIVED_SUBSCRIBE;
		case UNSUBSCRIBE:
			return ServerEvent.RECEIVED_UNSUBSCRIBE;
		case DISCONNECT:
			return ServerEvent.RECEIVED_DISCONNECT;
		default:
			return null;
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		Channel channel = e.getChannel();

		// TODO - add description to the error message (based on current state
		// and received message)
		ServerEvent serverEvent = directiveToServerEvent(message.getDirective());
		if (serverEvent == null || this.serverStateMachine.getNextState(serverEvent) == null) {
			sendError(channel);
			return;
		}

		ServerContext actionContext = new ServerContext(ctx, e, null);
		switch (message.getDirective()) {
		case CONNECT:
		case DISCONNECT:
			this.serverStateMachine.readSymbol(serverEvent, actionContext);
			break;
		case SUBSCRIBE:
			// change state to SUBSCRIPTION_RECEIVED immediately, then process
			// the subscription
			this.serverStateMachine.readSymbol(serverEvent, actionContext);
			this.serverStateMachine.readSymbol(ServerEvent.PROCESS_SUBSCRIPTION, actionContext);
			break;
		case UNSUBSCRIBE:
			this.serverStateMachine.readSymbol(serverEvent, actionContext);
			this.serverStateMachine.readSymbol(ServerEvent.PROCESS_UNSUBSCRIBE, actionContext);
			break;
		}

	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		this.serverStateMachine.readSymbol(ServerEvent.NETTY_TCP_CONNECTED, null);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println("Channel disconnected...");
	}
}
