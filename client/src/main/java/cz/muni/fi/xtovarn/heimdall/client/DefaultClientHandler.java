package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.client.protocol.ClientEvent;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.channel.*;

public class DefaultClientHandler extends SimpleChannelHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println(ClientEvent.NETTY_TCP_CONNECTED);
		e.getChannel().write(new SimpleMessage(Directive.CONNECT, "danos".getBytes()));
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();

		switch (message.getDirective()) {
			case CONNECTED:
				System.out.println(ClientEvent.RECEIVED_CONNECTED);
				break;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if	(e.getCause().getClass().equals(java.net.ConnectException.class)) {
			System.err.println("Connection refused...");
			System.exit(-1);
		} else {
			super.exceptionCaught(ctx, e);
		}
	}
}
