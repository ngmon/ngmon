package cz.muni.fi.xtovarn.heimdall.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

public class LengthDecoder extends ReplayingDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, Enum state) throws Exception {
		return buffer.readBytes(buffer.readUnsignedShort()); // Length is encoded as 2 byte unsigned short
	}
}