package cz.muni.fi.xtovarn.heimdall.client.protocol;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;

public class ClientContext {
	private final ChannelHandlerContext ctx;
	private final MessageEvent messageEvent;
	private final ChannelStateEvent channelStateEvent;

	public ClientContext(ChannelHandlerContext ctx, MessageEvent messageEvent, ChannelStateEvent channelStateEvent) {
		this.ctx = ctx;
		this.messageEvent = messageEvent;
		this.channelStateEvent = channelStateEvent;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public MessageEvent getMessageEvent() {
		return messageEvent;
	}

	public ChannelStateEvent getChannelStateEvent() {
		return channelStateEvent;
	}
}
