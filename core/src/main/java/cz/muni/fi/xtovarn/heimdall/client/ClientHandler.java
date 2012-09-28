package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class ClientHandler extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		String body = new String(message.getBody());

		switch (message.getDirective()) {
			case GREET:
				System.out.println(message.getDirective() + ": " + body);
				e.getChannel().write(new SimpleMessage(Directive.HELLO, ("hello(" + body + ")").getBytes()));
			case HELLO:
				break;
			case AUTH_REQUEST:
				break;
			case AUTH_RESPONSE:
				break;
			case CHALLENGE:
				break;
			case COMMAND:
				break;
			case SEND_SMILE:
				Event event = JSONEventMapper.bytesToEvent(message.getBody());
//				System.out.println(JSONStringParser.eventToString(event));
				long arrival = System.currentTimeMillis();
				long occurence = event.getOccurrenceTime().getTime();
				long detection = event.getDetectionTime().getTime();

//				System.out.println((detection - occurence) + ";" + (arrival - detection));
			case SEND_JSON:
				break;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if	(e.getCause().getClass().equals(java.net.ConnectException.class)) {
			System.err.println("Connection refused...");
		} else {
			super.exceptionCaught(ctx, e);
		}
	}
}
