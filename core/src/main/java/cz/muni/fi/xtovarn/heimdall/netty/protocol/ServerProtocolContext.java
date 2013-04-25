package cz.muni.fi.xtovarn.heimdall.netty.protocol;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionParser;

/**
 * The "executive" part of the server - processes client requests (after they
 * are preprocessed by DefaultServerHandler) and sends results to the client
 */
public class ServerProtocolContext {

	/**
	 * Simple static user store
	 */
	private static class UserStore {

		private Map<String, String> userMap = new HashMap<>();

		public UserStore() {
			for (int i = 0; i < 10; i++) {
				userMap.put("user" + i, "password" + i);
			}
		}

		public boolean verifyLogin(String login, String passcode) {
			String userPasscode = userMap.get(login);
			return userPasscode != null && passcode.equals(userPasscode);
		}

	}

	private ObjectMapper mapper = new ObjectMapper();
	private SecureChannelGroup secureChannelGroup;
	private UserStore userStore = new UserStore();
	private final SubscriptionManager subscriptionManager;

	public ServerProtocolContext(SecureChannelGroup secureChannelGroup, SubscriptionManager subscriptionManager) {
		this.secureChannelGroup = secureChannelGroup;
		this.subscriptionManager = subscriptionManager;
	}

	/**
	 * Processes CONNECT request
	 */
	public boolean connect(ServerContext actionContext) {
		MessageEvent messageEvent = actionContext.getMessageEvent();
		SimpleMessage message = (SimpleMessage) messageEvent.getMessage();
		Channel channel = messageEvent.getChannel();
		boolean verified = false;
		Map<String, Integer> connectionIdMap = new HashMap<>();
		try {
			User user = mapper.readValue(message.getBody(), User.class);
			verified = userStore.verifyLogin(user.getLogin(), user.getPasscode());
			if (verified) {
				secureChannelGroup.add(user.getLogin(), channel);
				connectionIdMap.put(Constants.CONNECTION_ID_TITLE, channel.getId());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			SimpleMessage replyMessage = new SimpleMessage(verified ? Directive.CONNECTED : Directive.ERROR,
					verified ? mapper.writeValueAsBytes(connectionIdMap) : "".getBytes());
			ChannelFuture future = channel.write(replyMessage);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return verified;
	}

	/**
	 * Processes DISCONNECT request
	 */
	public void disconnect(ServerContext actionContext) {
		Channel channel = actionContext.getMessageEvent().getChannel();

		SimpleMessage replyMessage = new SimpleMessage(Directive.ACK, "".getBytes());
		channel.write(replyMessage);

		disconnect(channel);
	}

	/**
	 * Removes the client from the collection of connected clients
	 */
	public void disconnect(Channel channel) {
		secureChannelGroup.remove(channel);
	}

	private void sendError(Channel channel) {
		channel.write(new SimpleMessage(Directive.ERROR, "".getBytes()));
	}

	/**
	 * Subscribes the client for sensor events specified by the Predicate
	 */
	public void processSubscription(ServerContext actionContext) {
		MessageEvent messageEvent = actionContext.getMessageEvent();
		Channel channel = messageEvent.getChannel();
		boolean success = false;
		try {
			// parse the predicate and subscribe the client
			@SuppressWarnings("unchecked")
			Predicate predicate = SubscriptionParser.parseSubscription(mapper.readValue(
					((SimpleMessage) messageEvent.getMessage()).getBody(), Map.class));
			Long subscriptionId = this.subscriptionManager.addSubscription(secureChannelGroup.getUsername(channel),
					predicate);
			
			// send the subscription ID to the client
			Map<String, Long> subscriptionIdMap = new HashMap<>();
			subscriptionIdMap.put(Constants.SUBSCRIPTION_ID_TITLE, subscriptionId);
			channel.write(new SimpleMessage(Directive.ACK, mapper.writeValueAsBytes(subscriptionIdMap)));
			success = true;
		} catch (IOException | ParseException | IndexOutOfBoundsException ex) {
		}

		if (!success)
			sendError(channel);
	}

	/**
	 * Cancels the specified subscription
	 */
	public void processUnsubscribe(ServerContext actionContext) {
		Channel channel = actionContext.getMessageEvent().getChannel();
		boolean success = false;
		try {
			// retrieve the subscription ID, then unsubscribe
			@SuppressWarnings("unchecked")
			Map<String, Number> unsubscribeMap = mapper.readValue(((SimpleMessage) actionContext.getMessageEvent()
					.getMessage()).getBody(), Map.class);
			Long subscriptionId = unsubscribeMap.get(Constants.SUBSCRIPTION_ID_TITLE).longValue();
			if (subscriptionId != null) {
				success = this.subscriptionManager.removeSubscription(secureChannelGroup.getUsername(channel),
						subscriptionId);
			}
		} catch (IOException e) {
		}

		if (success) {
			channel.write(new SimpleMessage(Directive.ACK, "".getBytes()));
		} else {
			sendError(channel);
		}
	}

	private void sendAck(Channel channel) {
		channel.write(new SimpleMessage(Directive.ACK, "".getBytes()));
	}

	/**
	 * Starts forwarding sensor events to the client
	 */
	public void processReady(ServerContext actionContext) {
		Channel channel = actionContext.getMessageEvent().getChannel();
		secureChannelGroup.setReceiving(channel, true);
		sendAck(channel);
	}

	/**
	 * Stops forwarding sensor events to the client
	 */
	public void processStop(ServerContext actionContext) {
		Channel channel = actionContext.getMessageEvent().getChannel();
		secureChannelGroup.setReceiving(channel, false);
		sendAck(channel);
	}

	/**
	 * Retrieves the "missed" sensor events - NOT YET IMPLEMENTED
	 */
	public void processGet(ServerContext actionContext) {
		// TODO - implement
		sendAck(actionContext.getMessageEvent().getChannel());
	}

}
