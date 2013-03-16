package cz.muni.fi.xtovarn.heimdall.client.protocol;

import org.jboss.netty.channel.Channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachine;
import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class ClientFSM extends AbstractFiniteStateMachine<ClientState, ClientEvent, ClientContext> {

	private ObjectMapper mapper = new ObjectMapper();

	private Long connectionId = null;

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

	}

	public Long getConnectionId() {
		return connectionId;
	}

}
