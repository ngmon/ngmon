package cz.muni.fi.xtovarn.heimdalld.localserver;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SimpleLocalHandler extends SimpleChannelHandler {

	private final Resender resender;

	public SimpleLocalHandler(Resender resender) {
		this.resender = resender;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		resender.pipeline((String) e.getMessage());
	}
}
