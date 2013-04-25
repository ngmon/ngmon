package cz.muni.fi.xtovarn.heimdall.test.util;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.MessageHandler;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.ResponseHandler;

/**
 * Processes the server responses
 */
public class TestClientHandler extends SimpleChannelHandler {

	/**
	 * Number of messages received so far
	 */
	private int messageCount = 0;

	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * The handlers of the server responses
	 */
	private List<ResponseHandler> responseHandlers;

	private CountDownLatch messageReceivedLatch = null;

	/**
	 * Temporary storage for the object returned by the response handler which
	 * was invoked last
	 */
	private Object lastResponseObject = null;

	/**
	 * A handler for processing other messages than the server responses (to be
	 * more precise: messages for which there is no corresponding response
	 * handler)
	 */
	private MessageHandler unsolicitedMessageHandler;

	public TestClientHandler(List<ResponseHandler> responseHandlers, MessageHandler unsolicitedMessageHandler) {
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
				// save the handler processing result
				lastResponseObject = responseHandler.processResponse(e);
			}
		}

		messageCount++;

		// if there's no corresponding handler, invoke the unsolicited message
		// handler if available (useful for example for handling the sensor
		// events)
		if ((messageCount > responseHandlers.size()) && (unsolicitedMessageHandler != null))
			unsolicitedMessageHandler.processMessage(e);

		// for letting the other thread know the a message has just been
		// processed
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

	/**
	 * Returns a CountDownLatch that can be used to wait for the next server
	 * response(s) to be processed by the client
	 * 
	 * @param count
	 *            How many messages to wait to be processed for
	 */
	public CountDownLatch getNewMessageReceivedLatch(int count) {
		return messageReceivedLatch = new CountDownLatch(count);
	}

	public CountDownLatch getNewMessageReceivedLatch() {
		return getNewMessageReceivedLatch(1);
	}

	public Object getLastResponseObject() {
		return lastResponseObject;
	}

	/**
	 * Sets a handler for processing other messages than the server responses
	 * (to be more precise: messages for which there is no corresponding
	 * response handler)
	 */
	public void setUnsolicitedMessageHandler(MessageHandler unsolicitedMessageHandler) {
		this.unsolicitedMessageHandler = unsolicitedMessageHandler;
	}

}
