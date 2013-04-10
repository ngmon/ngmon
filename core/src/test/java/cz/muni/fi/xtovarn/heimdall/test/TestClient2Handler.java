package cz.muni.fi.xtovarn.heimdall.test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.test.TestClient2.MessageHandler;
import cz.muni.fi.xtovarn.heimdall.test.TestClient2.ResponseHandler;

public class TestClient2Handler extends SimpleChannelHandler {

	private int messageCount = 0;

	private ObjectMapper objectMapper = new ObjectMapper();

	private List<ResponseHandler> responseHandlers;

	private CountDownLatch messageReceivedLatch = null;
	private Object lastResponseObject = null;
	private MessageHandler unsolicitedMessageHandler;

	public TestClient2Handler(List<ResponseHandler> responseHandlers, MessageHandler unsolicitedMessageHandler) {
		this.responseHandlers = responseHandlers;
		this.unsolicitedMessageHandler = unsolicitedMessageHandler;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		System.out.println("Message number: " + messageCount);
		System.out.println("Directive: " + message.getDirective().toString());
		// System.out.println("Body1: " + Arrays.toString(message.getBody()));
		System.out.println("Body: " + new String(message.getBody()));

		// send the message to the appropriate handler
		if (messageCount < responseHandlers.size()) {
			ResponseHandler responseHandler = responseHandlers.get(messageCount);
			if (responseHandler != null) {
				lastResponseObject = responseHandler.processResponse(e);
			}
		}

		messageCount++;

		if ((messageCount > responseHandlers.size()) && (unsolicitedMessageHandler != null))
			unsolicitedMessageHandler.processMessage(e);

		if (messageReceivedLatch != null)
			messageReceivedLatch.countDown();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

	public int getMessageCount() {
		return messageCount;
	}

	public ObjectMapper getMapper() {
		return objectMapper;
	}

	public CountDownLatch getNewMessageReceivedLatch(int count) {
		return messageReceivedLatch = new CountDownLatch(count);
	}

	public CountDownLatch getNewMessageReceivedLatch() {
		return getNewMessageReceivedLatch(1);
	}

	public Object getLastResponseObject() {
		return lastResponseObject;
	}

	public void setUnsolicitedMessageHandler(MessageHandler unsolicitedMessageHandler) {
		this.unsolicitedMessageHandler = unsolicitedMessageHandler;
	}

}