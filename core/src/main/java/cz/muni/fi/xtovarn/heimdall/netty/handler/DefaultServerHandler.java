package cz.muni.fi.xtovarn.heimdall.netty.handler;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerContext;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerEvent;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerFSM;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class DefaultServerHandler extends SimpleChannelHandler {

	private final SecureChannelGroup secureChannelGroup;

	private final ServerFSM serverStateMachine;

	public DefaultServerHandler(SecureChannelGroup secureChannelGroup) {
		this.secureChannelGroup = secureChannelGroup;
		this.serverStateMachine = new ServerFSM(this.secureChannelGroup);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();

		switch (message.getDirective()) {
			case CONNECT:
				this.serverStateMachine.readSymbol(ServerEvent.RECIEVED_CONNECT, new ServerContext(ctx, e, null));
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
