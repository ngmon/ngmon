package cz.muni.fi.xtovarn.heimdall.netty.codec;

import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

/**
 * Constructs a Message from the raw data
 */
public class MessageDecoder extends OneToOneDecoder {
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		ChannelBuffer buffer = (ChannelBuffer) msg;
		short unsignedByte = ((ChannelBuffer) msg).readUnsignedByte();

		Directive directive = Directive.get(unsignedByte);

		if (directive == null) {
			return null;
		}

		byte[] body = new byte[buffer.readableBytes()];
		buffer.readBytes(body);

		return new SimpleMessage(directive, body);
	}
}
