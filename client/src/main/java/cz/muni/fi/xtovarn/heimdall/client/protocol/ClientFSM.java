package cz.muni.fi.xtovarn.heimdall.client.protocol;

import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachine;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;

public class ClientFSM extends AbstractFiniteStateMachine<ClientState, ClientEvent, ClientContext> {

	public ClientFSM(final SecureChannelGroup secureChannelGroup) {
		super(ClientState.CREATED, new ClientState[]{ClientState.DISCONNECTED}, ClientState.class);

		buildTransitions(secureChannelGroup);
	}

	public void buildTransitions(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ClientState.CREATED, ClientEvent.NETTY_TCP_CONNECTED, ClientState.PRE_CONNECTED, null);


	}
}
