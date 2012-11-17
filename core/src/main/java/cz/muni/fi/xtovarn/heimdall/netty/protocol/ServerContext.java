package cz.muni.fi.xtovarn.heimdall.netty.protocol;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;

public class ServerContext {
	private final ChannelHandlerContext ctx;
	private final MessageEvent messageEvent;
	private final ChannelStateEvent channelStateEvent;

	public ServerContext(ChannelHandlerContext ctx, MessageEvent messageEvent, ChannelStateEvent channelStateEvent) {
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
