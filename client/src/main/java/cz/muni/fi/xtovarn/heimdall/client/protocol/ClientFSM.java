package cz.muni.fi.xtovarn.heimdall.client.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachine;
import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;

public class ClientFSM extends AbstractFiniteStateMachine<ClientState, ClientEvent, ClientContext> {

	private ObjectMapper mapper = new ObjectMapper();

	private Long connectionId = null;
	private List<Long> subscriptionIds = new ArrayList<>();
	private boolean lastSubscriptionSuccessful = false;

	public ClientFSM(final SecureChannelGroup secureChannelGroup) {
		super(ClientState.CREATED, new ClientState[] { ClientState.DISCONNECTED }, ClientState.class);

		buildTransitions(secureChannelGroup);
	}

	public void buildTransitions(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ClientState.CREATED, ClientEvent.NETTY_TCP_CONNECTED, ClientState.PRE_CONNECTED, null);
		this.addTransition(ClientState.PRE_CONNECTED, ClientEvent.REQUEST_CONNECT, ClientState.WAITING_FOR_ACK,
				new Action<ClientContext>() {

					@Override
					public boolean perform(ClientContext context) {
						Channel channel = context.getMessageEvent().getChannel();
						User user = (User) context.getObject();
						try {
							channel.write(new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytes(user)));
							return true;
						} catch (JsonProcessingException e) {
							return false;
						}
					}
				});

		this.addTransition(ClientState.WAITING_FOR_ACK, ClientEvent.ERROR, ClientState.PRE_CONNECTED, null);

		this.addTransition(ClientState.WAITING_FOR_ACK, ClientEvent.RECEIVED_CONNECTED, ClientState.CONNECTED,
				new Action<ClientContext>() {

					@Override
					public boolean perform(ClientContext context) {
						connectionId = (Long) context.getObject();
						return true;
					}

				});

		this.addTransition(ClientState.CONNECTED, ClientEvent.REQUEST_SUBSCRIBE, ClientState.WAITING_FOR_ACK,
				new Action<ClientContext>() {

					@Override
					public boolean perform(ClientContext context) {
						Channel channel = context.getMessageEvent().getChannel();
						Predicate predicate = (Predicate) context.getObject();
						try {
							channel.write(new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytes(predicate
									.toStringMap())));
							return true;
						} catch (JsonProcessingException e) {
							return false;
						}
					}

				});

		this.addTransition(ClientState.WAITING_FOR_ACK, ClientEvent.RECEIVED_SUBSCRIBE_ACK, ClientState.CONNECTED,
				new Action<ClientContext>() {

					@Override
					public boolean perform(ClientContext context) {
						SimpleMessage message = (SimpleMessage) context.getMessageEvent().getMessage();
						boolean success = false;
						try {
							Map<String, Number> subscriptionIdMap = (Map<String, Number>) mapper.readValue(
									message.getBody(), Map.class);
							Long subscriptionId = subscriptionIdMap.get(Constants.SUBSCRIPTION_ID_TITLE).longValue();
							if (subscriptionId != null) {
								subscriptionIds.add(subscriptionId);
								success = true;
							}
						} catch (IOException e) {
						}

						lastSubscriptionSuccessful = success;

						return true;
					}

				});

	}

	public Long getConnectionId() {
		return connectionId;
	}

	public List<Long> getSubscriptionIds() {
		return subscriptionIds;
	}

	public Long getLastSubscriptionId() {
		if (subscriptionIds.isEmpty())
			return null;
		else
			return subscriptionIds.get(subscriptionIds.size() - 1);
	}

	public boolean wasLastSubscriptionSuccessful() {
		return lastSubscriptionSuccessful;
	}

}
