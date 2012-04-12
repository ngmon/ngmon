package cz.muni.fi.xtovarn.heimdall.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class MainHandler extends SimpleChannelHandler {

	private static ConnectionPool pool = ConnectionPools.getPool();

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		e.getChannel().write("Connected to HeimdallD server!");
		pool.add(new Connection("xdanos", e.getChannel()));
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println("Channel disconnected...");
		pool.remove("xdanos");
	}
}
