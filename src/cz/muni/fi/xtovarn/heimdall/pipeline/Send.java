package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import org.jboss.netty.channel.Channel;

public class Send implements Runnable {

	private final String recipient;
	private final Event event;

	public Send(String recipient, Event event) {
		this.recipient = recipient;
		this.event = event;
	}

	@Override
	public void run() {
		Message message = new SimpleMessage(Directive.SEND_JSON, event.toString().getBytes());

		SecureChannelGroup secureChannelGroup = SecureChannelGroup.getInstance();

		if (secureChannelGroup.contains(recipient)) {
			Channel ch = secureChannelGroup.find(recipient);
			ch.write(message);
		} else {
			System.out.println("Written to tempDB");
		}
	}
}
