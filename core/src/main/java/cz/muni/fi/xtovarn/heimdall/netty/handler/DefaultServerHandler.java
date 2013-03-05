package cz.muni.fi.xtovarn.heimdall.netty.handler;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.publishsubscribe.countingtree.Subscription;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerContext;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerEvent;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerFSM;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerState;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionParser;

public class DefaultServerHandler extends SimpleChannelHandler {

	private final SecureChannelGroup secureChannelGroup;

	private final ServerFSM serverStateMachine;

	private SubscriptionManager subscriptionManager = new SubscriptionManager();
	private ObjectMapper mapper = new ObjectMapper();

	public DefaultServerHandler(SecureChannelGroup secureChannelGroup) {
		this.secureChannelGroup = secureChannelGroup;
		this.serverStateMachine = new ServerFSM(this.secureChannelGroup);
	}

	private void sendError(Channel channel) {
		channel.write(new SimpleMessage(Directive.ERROR, "".getBytes()));
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		Channel channel = e.getChannel();

		switch (message.getDirective()) {
		case CONNECT:
			this.serverStateMachine.readSymbol(ServerEvent.RECEIVED_CONNECT, new ServerContext(ctx, e, null));
			break;
		case SUBSCRIBE:
			if (!this.serverStateMachine.getCurrentState().equals(ServerState.CONNECTED)) {
				sendError(channel);
			} else {
				boolean success = false;
				try {
					Predicate predicate = SubscriptionParser.parseSubscription(mapper.readValue(message.getBody(),
							Map.class));
					Subscription subscription = subscriptionManager.addSubscription(e.getChannel().getId(),
							predicate);
					Map<String, Long> subscriptionIdMap = new HashMap<>();
					subscriptionIdMap.put("subscriptionId", subscription.getId());
					channel.write(new SimpleMessage(Directive.ACK, mapper.writeValueAsBytes(subscriptionIdMap)));
					success = true;
				} catch (IOException | ParseException ex) {
				}

				if (!success)
					sendError(channel);
			}
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
