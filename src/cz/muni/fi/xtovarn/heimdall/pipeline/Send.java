package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.StringMessage;
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

		SecureChannelGroup secureChannelGroup = SecureChannelGroup.getInstance();

		if (secureChannelGroup.contains(recipient)) {
			Channel ch = secureChannelGroup.find(recipient);
			ch.write(buffer);
		} else {
			System.out.println("Written to tempDB");
		}
	}
}
