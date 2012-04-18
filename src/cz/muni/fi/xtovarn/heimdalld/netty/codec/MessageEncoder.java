package cz.muni.fi.xtovarn.heimdalld.netty.codec;

import cz.muni.fi.xtovarn.heimdalld.netty.message.Message;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class MessageEncoder extends OneToOneEncoder {
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		Message message = (Message) msg;

		ChannelBuffer buffer = ChannelBuffers.buffer(message.size());

		buffer.writeShort(message.length());
		buffer.writeByte(message.getDirective().getCode());
		buffer.writeBytes(message.getBody());

		return buffer;
	}
}