package cz.muni.fi.xtovarn.heimdall.netty.protocol;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.publishsubscribe.countingtree.Subscription;
import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachine;
import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManagerSingleton;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionParser;

public class ServerFSM extends AbstractFiniteStateMachine<ServerState, ServerEvent, ServerContext> {

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
	private UserStore userStore = new UserStore();

	public ServerFSM(final SecureChannelGroup secureChannelGroup) {
		super(ServerState.CREATED, new ServerState[] { ServerState.DISCONNECTED }, ServerState.class, true);

		buildTransitions(secureChannelGroup);
	}

	private void addConnectTransition(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ServerState.PRE_CONNECTED, ServerEvent.RECEIVED_CONNECT, ServerState.CONNECTED,
				new Action<ServerContext>() {

					@Override
					public boolean perform(ServerContext context) {
						SimpleMessage message = (SimpleMessage) context.getMessageEvent().getMessage();
						boolean verified = false;
						Channel channel = context.getMessageEvent().getChannel();
						Map<String, Integer> connectionIdMap = new HashMap<>();
						try {
							User user = mapper.readValue(message.getBody(), User.class);
							verified = userStore.verifyLogin(user.getLogin(), user.getPasscode());
							if (verified) {
								secureChannelGroup.add(user.getLogin(), channel);
								// TODO - extract the string constant
								connectionIdMap.put("connectionId", channel.getId());
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

						try {
							SimpleMessage replyMessage = new SimpleMessage(verified ? Directive.CONNECTED
									: Directive.ERROR, verified ? mapper.writeValueAsBytes(connectionIdMap) : ""
									.getBytes());
							ChannelFuture future = channel.write(replyMessage);
						} catch (JsonProcessingException e) {
							throw new RuntimeException(e);
						}

						return verified;
					}
				});
	}

	private void addSubscribeToSubscriptionReceivedTransition(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ServerState.CONNECTED, ServerEvent.RECEIVED_SUBSCRIBE, ServerState.SUBSCRIPTION_RECEIVED,
				new Action<ServerContext>() {

					@Override
					public boolean perform(ServerContext context) {
						return true;
					}

				});
	}

	private void addSubscriptionReceivedTransition(SecureChannelGroup secureChannelGroup) {
		this.addTransition(ServerState.SUBSCRIPTION_RECEIVED, ServerEvent.PROCESS_SUBSCRIPTION, ServerState.CONNECTED,
				new Action<ServerContext>() {

					@Override
					public boolean perform(ServerContext context) {
						Channel channel = context.getMessageEvent().getChannel();
						boolean success = false;
						try {
							Predicate predicate = SubscriptionParser.parseSubscription(mapper.readValue(
									((SimpleMessage) context.getMessageEvent().getMessage()).getBody(), Map.class));
							Subscription subscription = SubscriptionManagerSingleton.getSubscriptionManager()
									.addSubscription(channel.getId(), predicate);
							Map<String, Long> subscriptionIdMap = new HashMap<>();
							subscriptionIdMap.put("subscriptionId", subscription.getId());
							channel.write(new SimpleMessage(Directive.ACK, mapper.writeValueAsBytes(subscriptionIdMap)));
							success = true;
						} catch (IOException | ParseException ex) {
							// TODO - send error here?
						}
						
						// TODO - send error here?

						// always changes state back to CONNECTED
						return true;
					}

				});
	}

	public void buildTransitions(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ServerState.CREATED, ServerEvent.NETTY_TCP_CONNECTED, ServerState.PRE_CONNECTED, null);
		this.addConnectTransition(secureChannelGroup);
		this.addSubscribeToSubscriptionReceivedTransition(secureChannelGroup);
		this.addSubscriptionReceivedTransition(secureChannelGroup);
	}

}
