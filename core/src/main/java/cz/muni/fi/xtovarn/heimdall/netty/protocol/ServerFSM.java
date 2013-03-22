package cz.muni.fi.xtovarn.heimdall.netty.protocol;

import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachineNoActions;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;

public class ServerFSM extends AbstractFiniteStateMachineNoActions<ServerState, ServerEvent> {

	public ServerFSM(final SecureChannelGroup secureChannelGroup) {
		super(ServerState.CREATED, new ServerState[] { ServerState.DISCONNECTED }, ServerState.class, false);

		buildTransitions(secureChannelGroup);
	}

	public void buildTransitions(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ServerState.CREATED, ServerEvent.NETTY_TCP_CONNECTED, ServerState.PRE_CONNECTED);
		this.addTransition(ServerState.PRE_CONNECTED, ServerEvent.RECEIVED_CONNECT, ServerState.CONNECTED);
		this.addTransition(ServerState.CONNECTED, ServerEvent.RECEIVED_SUBSCRIBE, ServerState.SUBSCRIPTION_RECEIVED);
		this.addTransition(ServerState.SUBSCRIPTION_RECEIVED, ServerEvent.SUBSCRIPTION_PROCESSED, ServerState.CONNECTED);
		this.addTransition(ServerState.CONNECTED, ServerEvent.RECEIVED_UNSUBSCRIBE, ServerState.UNSUBSCRIBE_RECEIVED);
		this.addTransition(ServerState.UNSUBSCRIBE_RECEIVED, ServerEvent.UNSUBSCRIBE_PROCESSED, ServerState.CONNECTED);
		this.addTransition(ServerState.CONNECTED, ServerEvent.RECEIVED_DISCONNECT, ServerState.DISCONNECTED);
	}

}
