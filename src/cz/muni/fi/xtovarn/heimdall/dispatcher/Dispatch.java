package cz.muni.fi.xtovarn.heimdall.dispatcher;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.channel.Channel;

public class Dispatch implements Runnable {

	private final String recipient;
	private final Event event;
	private final Channel channel;

	public Dispatch(Channel channel, String recipient, Event event) {
		this.channel = channel;
		this.recipient = recipient;
		this.event = event;
	}

	@Override
	public void run() {
		Message message = new SimpleMessage(Directive.SEND_JSON, event.toString().getBytes());
		channel.write(message);
	}
}
