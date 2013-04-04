package cz.muni.fi.xtovarn.heimdall.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerContext;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerEvent;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerFSM;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerProtocolContext;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;

public class DefaultServerHandler extends SimpleChannelHandler {

	private final SecureChannelGroup secureChannelGroup;

	private final ServerFSM serverStateMachine;
	private ServerProtocolContext serverProtocolContext;

	public DefaultServerHandler(SecureChannelGroup secureChannelGroup, SubscriptionManager subscriptionManager) {
		this.secureChannelGroup = secureChannelGroup;
		this.serverProtocolContext = new ServerProtocolContext(secureChannelGroup, subscriptionManager);
		this.serverStateMachine = new ServerFSM(this.secureChannelGroup);
	}

	private void sendError(Channel channel) {
		channel.write(new SimpleMessage(Directive.ERROR, "".getBytes()));
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		Channel channel = e.getChannel();

		// TODO - add description to the error message (based on current state
		// and received message)
		ServerEvent serverEvent = HandlerUtils.directiveToServerEvent(message.getDirective());
		if (serverEvent == null || this.serverStateMachine.isEnded()
				|| this.serverStateMachine.getNextState(serverEvent) == null) {
			sendError(channel);
			return;
		}

		ServerContext actionContext = new ServerContext(ctx, e, null);
		switch (message.getDirective()) {
		case CONNECT:
			if (this.serverProtocolContext.connect(actionContext))
				this.serverStateMachine.readSymbol(serverEvent);
			break;
		case DISCONNECT:
			this.serverProtocolContext.disconnect(actionContext);
			this.serverStateMachine.readSymbol(serverEvent);
			break;
		case SUBSCRIBE:
			// change state to SUBSCRIPTION_RECEIVED immediately, then process
			// the subscription
			this.serverStateMachine.readSymbol(serverEvent);
			this.serverProtocolContext.processSubscription(actionContext);
			this.serverStateMachine.readSymbol(ServerEvent.SUBSCRIPTION_PROCESSED);
			break;
		case UNSUBSCRIBE:
			this.serverStateMachine.readSymbol(serverEvent);
			this.serverProtocolContext.processUnsubscribe(actionContext);
			this.serverStateMachine.readSymbol(ServerEvent.UNSUBSCRIBE_PROCESSED);
			break;
		case READY:
			this.serverStateMachine.readSymbol(serverEvent);
			this.serverProtocolContext.processReady(actionContext);
			break;
		case STOP:
			this.serverStateMachine.readSymbol(serverEvent);
			this.serverProtocolContext.processStop(actionContext);
			break;
		case GET:
			// the state doesn't change
			this.serverProtocolContext.processGet(actionContext);
			break;
		default:
			sendError(channel);
			break;
		}

	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		this.serverStateMachine.readSymbol(ServerEvent.NETTY_TCP_CONNECTED);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// this is probably unnecessary, since the disconnected channels are
		// removed from the ChannelGroup automatically
		this.serverProtocolContext.disconnect(e.getChannel());
		// TODO - set the machine state to DISCONNECTED?
		System.out.println("Channel disconnected...");
	}
}
