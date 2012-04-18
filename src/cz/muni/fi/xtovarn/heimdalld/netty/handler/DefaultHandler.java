package cz.muni.fi.xtovarn.heimdalld.netty.handler;

import cz.muni.fi.xtovarn.heimdalld.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdalld.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdalld.netty.message.SimpleMessage;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class DefaultHandler extends SimpleChannelHandler {

	private final SecureChannelGroup secureChannelGroup;

	public DefaultHandler(SecureChannelGroup secureChannelGroup) {
		this.secureChannelGroup = secureChannelGroup;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();

		switch (message.getDirective()) {
			case HELLO:

				int size = secureChannelGroup.size() + 1;
				secureChannelGroup.add("xdanos@" + size, e.getChannel());

			case GREET:
				break;
			case AUTH_REQUEST:
				break;
			case AUTH_RESPONSE:
				break;
			case CHALLENGE:
				break;
			case COMMAND:
				break;
			case SEND_JSON:
				break;
			case SEND_SMILE:
				break;
		}
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		e.getChannel().write(new SimpleMessage(Directive.GREET, "greet(version:0.0.1)".getBytes()));
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println("Channel disconnected...");
	}
}
