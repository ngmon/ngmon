package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

public class Connection {
	private final String id;
	private final Channel channel;

	public Connection(String id, Channel channel) {
		this.id = id;
		this.channel = channel;
	}

	protected void send(Event event) {
		ChannelFuture future = channel.write(event.toString());
		future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
	}

	public String getId() {
		return id;
	}
}
