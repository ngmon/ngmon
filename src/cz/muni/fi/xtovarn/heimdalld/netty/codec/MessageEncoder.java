package cz.muni.fi.xtovarn.heimdalld.netty.codec;

import cz.muni.fi.xtovarn.heimdalld.netty.message.SimpleMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class MessageEncoder extends OneToOneEncoder {
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		SimpleMessage message = (SimpleMessage) msg;

		ChannelBuffer buffer = ChannelBuffers.buffer(message.size());

		System.out.println(message.size() + "." + message.length());

		buffer.writeShort(message.length());
		buffer.writeByte(message.getDirective().getCode());
		buffer.writeBytes(message.getBody());

		return buffer;
	}
}
