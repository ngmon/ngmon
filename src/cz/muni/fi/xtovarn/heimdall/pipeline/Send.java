package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.netty.ChannelGroups;
import cz.muni.fi.xtovarn.heimdall.netty.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.StringMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
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
		StringMessage message = new StringMessage(Directive.SEND_JSON, event.toString());
		ChannelBuffer buffer = ChannelBuffers.buffer(message.size());

		buffer.writeShort(message.length());
		buffer.writeByte(message.getDirective().getCode());
		buffer.writeBytes(message.getBody().getBytes());

		SecureChannelGroup secureChannelGroup = ChannelGroups.getSingleInstance();

		Channel ch = secureChannelGroup.find(recipient);
		if (ch != null) {
			ch.write(buffer);
		} else {
			System.out.println("Written to temp DB: " + buffer.toString());
		}
	}
}
