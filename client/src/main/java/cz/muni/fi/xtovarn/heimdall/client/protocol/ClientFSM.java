package cz.muni.fi.xtovarn.heimdall.client.protocol;

import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachineNoActions;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;

public class ClientFSM extends AbstractFiniteStateMachineNoActions<ClientState, ClientEvent> {

	public ClientFSM(final SecureChannelGroup secureChannelGroup) {
		super(ClientState.CREATED, new ClientState[] { ClientState.DISCONNECTED }, ClientState.class, false);

		buildTransitions(secureChannelGroup);
	}

	public void buildTransitions(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ClientState.CREATED, ClientEvent.NETTY_TCP_CONNECTED, ClientState.PRE_CONNECTED);
		this.addTransition(ClientState.PRE_CONNECTED, ClientEvent.REQUEST_CONNECT, ClientState.WAITING_FOR_ACK);
		this.addTransition(ClientState.WAITING_FOR_ACK, ClientEvent.RECEIVED_CONNECTED, ClientState.CONNECTED);
		this.addTransition(ClientState.CONNECTED, ClientEvent.REQUEST_SUBSCRIBE, ClientState.WAITING_FOR_ACK);
		this.addTransition(ClientState.WAITING_FOR_ACK, ClientEvent.RECEIVED_SUBSCRIBE_ACK, ClientState.CONNECTED);

	}

}
