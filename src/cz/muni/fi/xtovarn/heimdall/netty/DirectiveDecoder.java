package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.netty.messages.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.messages.StringMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class DirectiveDecoder extends OneToOneDecoder {
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		ChannelBuffer buffer = (ChannelBuffer) msg;
		short unsignedByte = ((ChannelBuffer) msg).readShort();

		Directive directive = Directive.get(unsignedByte);

		if (directive == null) {
			return null;
		}

		byte[] body = new byte[buffer.readableBytes()];
		buffer.readBytes(body);

		String stringBody = new String(body, "UTF-8");

		return new StringMessage(directive, stringBody);
	}
}
