package cz.muni.fi.xtovarn.heimdall.netty.protocol;

import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachine;
import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.channel.ChannelFuture;

public class ServerFSM extends AbstractFiniteStateMachine<ServerState, ServerEvent, ServerContext> {

	public ServerFSM(final SecureChannelGroup secureChannelGroup) {
		super(ServerState.CREATED, new ServerState[]{ServerState.DISCONNECTED}, ServerState.class);

		buildTransitions(secureChannelGroup);
	}

	public void buildTransitions(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ServerState.CREATED, ServerEvent.NETTY_TCP_CONNECTED, ServerState.PRE_CONNECTED, null);

		this.addTransition(ServerState.PRE_CONNECTED, ServerEvent.RECIEVED_CONNECT, ServerState.CONNECTED, new Action<ServerContext>() {

			@Override
			public boolean perform(ServerContext context) {
				SimpleMessage message = (SimpleMessage) context.getMessageEvent().getMessage();

				String username = new String(message.getBody());
				secureChannelGroup.add(username, context.getMessageEvent().getChannel());

				ChannelFuture future = context.getMessageEvent().getChannel().write(new SimpleMessage(Directive.CONNECTED, "".getBytes()));

				return true;
			}
		});
	}
}
