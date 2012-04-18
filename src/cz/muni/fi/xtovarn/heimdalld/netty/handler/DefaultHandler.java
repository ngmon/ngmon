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

	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		e.getChannel().write(new SimpleMessage(Directive.GREET, "greet(param1:100)".getBytes()));
		secureChannelGroup.add("xdanos", e.getChannel());
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println("Channel disconnected...");
	}
}
