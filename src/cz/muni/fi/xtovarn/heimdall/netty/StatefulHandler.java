package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.StringMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class StatefulHandler extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		StringMessage message = new StringMessage(Directive.GREET, "greet(miver:100)");
		ChannelBuffer buffer = ChannelBuffers.buffer(message.size());

		buffer.writeShort(message.length());
		buffer.writeByte(message.getDirective().getCode());
		buffer.writeBytes(message.getBody().getBytes());

		e.getChannel().write(buffer);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println("Channel disconnected...");
	}
}
