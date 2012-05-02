package cz.muni.fi.xtovarn.heimdall.dispatcher;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.json.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.json.JSONStringParser;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.io.IOException;

public class Dispatch implements Runnable {

	private final Event event;
	private final Channel channel;

	public Dispatch(Channel channel, Event event) {
		this.channel = channel;
		this.event = event;
	}

	@Override
	public void run() {
		Message message = null;
		try {
			message = new SimpleMessage(Directive.SEND_JSON, JSONStringParser.eventToString(event).getBytes());

			System.out.println("json length:" + JSONStringParser.eventToString(event).getBytes().length);

		} catch (IOException e) {
			e.printStackTrace();
		}
		ChannelFuture future = channel.write(message);
	}
}
