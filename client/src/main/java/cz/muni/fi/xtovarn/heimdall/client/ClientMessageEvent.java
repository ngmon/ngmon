package cz.muni.fi.xtovarn.heimdall.client;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;

public class ClientMessageEvent implements MessageEvent {

	private Channel channel;
	private ChannelFuture channelFuture;
	private Object message;
	private SocketAddress socketAddress;

	public ClientMessageEvent(Channel channel, ChannelFuture channelFuture, Object message, SocketAddress socketAddress) {
		this.channel = channel;
		this.channelFuture = channelFuture;
		this.message = message;
		this.socketAddress = socketAddress;
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

	@Override
	public ChannelFuture getFuture() {
		return channelFuture;
	}

	@Override
	public Object getMessage() {
		return message;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return socketAddress;
	}

}
