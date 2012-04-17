package cz.muni.fi.xtovarn.heimdall.dispatcher;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.channel.Channel;

public class Dispatch implements Runnable {

	private final String recipient;
	private final Event event;
	private final SecureChannelGroup secureChannelGroup;

	public Dispatch(SecureChannelGroup secureChannelGroup, String recipient, Event event) {
		this.secureChannelGroup = secureChannelGroup;
		this.recipient = recipient;
		this.event = event;
	}

	@Override
	public void run() {
		Message message = new SimpleMessage(Directive.SEND_JSON, event.toString().getBytes());

		if (secureChannelGroup.contains(recipient)) {
			Channel ch = secureChannelGroup.find(recipient);
			ch.write(message);
		} else {
			System.out.println("Written to tempDB");
		}
	}
}
