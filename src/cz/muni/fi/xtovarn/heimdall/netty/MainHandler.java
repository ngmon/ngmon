package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.stage.PerClientSendStage;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;

public class MainHandler extends SimpleChannelHandler {

	private PerClientSendStage stage;

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		e.getChannel().write("Connected to HeimdallD server!");
		stage = new PerClientSendStage(UserQueues.queue("xdanos"), e.getChannel());
		stage.start();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println("Channel disconnected...");
		stage.stop();
	}
}
