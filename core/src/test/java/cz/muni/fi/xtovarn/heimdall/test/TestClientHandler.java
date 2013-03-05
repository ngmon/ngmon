package cz.muni.fi.xtovarn.heimdall.test;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class TestClientHandler extends SimpleChannelHandler {

	private int messageCount = 0;
	private static final int MESSAGES_PROCESSED_BY_HANDLER = 0;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		System.out.println("Message number: " + (messageCount + 1));
		System.out.println("Directive: " + message.getDirective().toString());
		// System.out.println("Body1: " + Arrays.toString(message.getBody()));
		System.out.println("Body: " + new String(message.getBody()));
		
		processReceivedMessage(ctx, e);
		
		messageCount++;

		closeChannelIfLastMessage(e.getChannel());
	}
	
	public void processReceivedMessage(ChannelHandlerContext ctx,
			MessageEvent e) throws Exception {	
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

	public int getMessageCount() {
		return messageCount;
	}

	public int getMessagesProcessedByHandler() {
		return MESSAGES_PROCESSED_BY_HANDLER;
	}

	public boolean wasLastMessage() {
		return getMessageCount() >= getMessagesProcessedByHandler();
	}

	public void closeChannelIfLastMessage(Channel channel) {
		if (wasLastMessage())
			channel.close();
	}

	public ObjectMapper getMapper() {
		return objectMapper;
	}

}
