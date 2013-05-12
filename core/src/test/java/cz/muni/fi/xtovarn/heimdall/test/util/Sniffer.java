package cz.muni.fi.xtovarn.heimdall.test.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class Sniffer extends OneToOneDecoder {
	
	private static Logger logger = LogManager.getLogger(Sniffer.class);
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer) msg;

		logger.debug("Sniffer: " + buffer.readableBytes());

		return msg;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
