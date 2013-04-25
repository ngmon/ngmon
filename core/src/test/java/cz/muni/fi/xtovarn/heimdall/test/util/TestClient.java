package cz.muni.fi.xtovarn.heimdall.test.util;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;

/**
 * A simple client for testing purposes. Sends the specified messages to the
 * server and optionally saves the server response.
 */
public class TestClient {

	/**
	 * Represents a message to be sent to the server by the client
	 */
	public interface TestMessage {
		/**
		 * Constructs the message
		 * 
		 * @param responseMap
		 *            Previously saved data obtained from the server responses
		 *            (might be used to construct the message)
		 * @return The message
		 */
		public Message getMessage(Map<String, Object> responseMap);
	}

	/**
	 * Handles server responses
	 */
	public interface ResponseHandler {
		/**
		 * Processes server response
		 * 
		 * @param messageEvent
		 *            The server message with some additional data
		 * @return The processed data from the server response (or null if not
		 *         needed)
		 */
		public Object processResponse(MessageEvent messageEvent);
	}

	/**
	 * Handles the sensor events
	 */
	public interface MessageHandler {
		/**
		 * Processes the sensor event
		 */
		public void processMessage(MessageEvent messageEvent);
	}

	public static class DefaultMessageHandler implements MessageHandler {

		@Override
		public void processMessage(MessageEvent messageEvent) {
		}

	}

	public static final String MESSAGE_HANDLER_TITLE = "messageHandler";

	// how long to wait for the server responses
	public static final long TIMEOUT = 5;
	// for debugging, set to MINUTES, otherwise use SECONDS
	public static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;
	// how long to wait for all sensor events
	public static final long EVENT_TIMEOUT_IN_MILLIS = 1000;

	/**
	 * List of messages to send from the client to the server
	 */
	private List<TestMessage> messages = new ArrayList<>();
	/**
	 * For saving server responses (for example subscription IDs)
	 */
	private Map<String, Object> responseObjects = new HashMap<>();
	/**
	 * The keys to use in the responseObjects map
	 */
	private List<String> responseKeys = new ArrayList<>();
	/**
	 * The handlers of the server responses
	 */
	private List<ResponseHandler> responseHandlers = new ArrayList<>();
	/**
	 * Handles other messages (not server responses to client requests, but
	 * typically sensor events)
	 */
	private MessageHandler unsolicitedMessageHandler = null;
	/**
	 * Whether to cancel the corresponding subscription when stopping the client
	 */
	private List<Boolean> unsubscribeInStopList = new ArrayList<>();
	/**
	 * Saves the IDs of all subscriptions to be cancelled when stopping the
	 * client
	 */
	private Collection<Long> subscriptionIds = new ArrayList<>();
	private Channel channel = null;
	private ChannelFactory factory;
	private TestClientHandler channelHandler;
	private int messagesProcessed = 0;
	private ObjectMapper mapper = new ObjectMapper();

	private boolean isRunning = false;

	/**
	 * Runs the test client - sends all the prepared messages, processing the
	 * responses
	 * 
	 * @param close
	 *            Whether to stop the client when the method finishes
	 */
	public void run(boolean close) throws InterruptedException {
		factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(),
				Executors.newSingleThreadExecutor());
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		TestClientPipelineFactory pipelineFactory = new TestClientPipelineFactory(responseHandlers,
				unsolicitedMessageHandler);
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(NettyServer.SERVER_PORT));
		future.awaitUninterruptibly();
		channel = future.getChannel();
		channelHandler = (TestClientHandler) channel.getPipeline().getContext(MESSAGE_HANDLER_TITLE).getHandler();

		// send the messages, process (and save) the responses
		int messagesCount = messages.size();
		for (messagesProcessed = 0; messagesProcessed < messagesCount; messagesProcessed++) {
			TestMessage message = messages.get(messagesProcessed);
			String key = responseKeys.get(messagesProcessed);
			ResponseHandler responseHandler = responseHandlers.get(messagesProcessed);
			boolean unsubscribeInStop = unsubscribeInStopList.get(messagesProcessed);

			// send the message to server
			CountDownLatch latch = channelHandler.getNewMessageReceivedLatch();
			channel.write(message.getMessage(Collections.unmodifiableMap(responseObjects))).awaitUninterruptibly();
			// wait for the server response (will not work in async mode)
			latch.await(TIMEOUT, TIMEOUT_UNIT);
			// save the response data (for example subscription ID)
			if (responseHandler != null) {
				Object lastResponseObject = channelHandler.getLastResponseObject();
				if (key != null)
					responseObjects.put(key, lastResponseObject);
				if (unsubscribeInStop)
					subscriptionIds.add((Long) lastResponseObject);
			}
		}

		isRunning = true;

		if (close) {
			stop();
		}
	}

	public void run() throws InterruptedException {
		run(true);
	}

	/**
	 * Cancels the request subscriptions (all which are saved in
	 * subscriptionIds)
	 */
	private void unsubscribeAll() {
		MessageHandler unsubscribeHandler = new MessageHandler() {

			@Override
			public void processMessage(MessageEvent messageEvent) {
				SimpleMessage message = (SimpleMessage) messageEvent.getMessage();
				Directive directive = message.getDirective();
				if (!directive.equals(Directive.ACK)) {
					throw new RuntimeException("Error when unsubscribing. It's recommended to restart the server.");
				}
			}
		};

		// send STOP first (it doesn't matter if the server is not in the
		// SENDING state)
		channelHandler.setUnsolicitedMessageHandler(null);
		CountDownLatch latch = channelHandler.getNewMessageReceivedLatch();
		channel.write(new SimpleMessage(Directive.STOP, "".getBytes()));
		try {
			// wait for the response to the STOP message
			latch.await(TIMEOUT, TIMEOUT_UNIT);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// all the received messages will be processed by this handler
		channelHandler.setUnsolicitedMessageHandler(unsubscribeHandler);
		for (Long subscriptionId : this.subscriptionIds) {
			System.out.println("Unsubscribing " + subscriptionId);
			latch = channelHandler.getNewMessageReceivedLatch();
			Map<String, Long> unsubscribeMap = new HashMap<>();
			unsubscribeMap.put(Constants.SUBSCRIPTION_ID_TITLE, subscriptionId);
			try {
				channel.write(new SimpleMessage(Directive.UNSUBSCRIBE, mapper.writeValueAsBytes(unsubscribeMap)));
				latch.await(TIMEOUT, TIMEOUT_UNIT);
				System.out.println("Unsubscribed");
			} catch (JsonProcessingException | InterruptedException e) {
				throw new RuntimeException("Error when unsubscribing. It's recommended to restart the server.", e);
			}
		}
	}

	/**
	 * Stops the client, canceling all the subscriptions from the
	 * subscriptionIds collection
	 */
	public void stop() {
		if (isRunning) {
			this.unsubscribeAll();

			channel.close().awaitUninterruptibly();
			factory.releaseExternalResources();

			isRunning = false;
		}
	}

	/**
	 * Adds a message with a handler to be sent and the response processed in
	 * the run() method
	 * 
	 * @param testMessage
	 *            The message to be sent to the server later
	 * @param responseKey
	 *            The key to use when saving the processed server response to
	 *            the response map (responseObjects)
	 * @param responseHandler
	 *            The server response handler, might optionally return an
	 *            arbitrary object (which will then be saved to the response
	 *            map)
	 * @param unsubscribeInStop
	 *            True if this is a subscribe message and the subscription
	 *            should be automatically cancelled when stopping the client
	 */
	public void addMessage(TestMessage testMessage, String responseKey, ResponseHandler responseHandler,
			boolean unsubscribeInStop) {
		if (responseHandler == null && unsubscribeInStop)
			throw new IllegalArgumentException("If the message is SUBSCRIBE, ResponseHandler must not be null");
		messages.add(testMessage);
		responseKeys.add(responseKey);
		responseHandlers.add(responseHandler);
		unsubscribeInStopList.add(unsubscribeInStop);
	}

	/**
	 * Helper method for messages other than SUBSCRIBE
	 */
	public void addMessage(TestMessage testMessage, String responseKey, ResponseHandler responseHandler) {
		this.addMessage(testMessage, responseKey, responseHandler, false);
	}

	/**
	 * Helper method to be used when a static (constructed before anything is
	 * received from the server) message is sufficient (the message doesn't
	 * depend on a previous server response; an example might be a READY
	 * message, which is unlike an UNSUBSCRIBE message, since the latter needs
	 * to send the subscription ID received from the server before)
	 */
	public void addMessage(final Message message, String responseKey, ResponseHandler responseHandler,
			boolean unsubscribeInStop) {
		TestMessage testMessage = new TestMessage() {
			@Override
			public Message getMessage(Map<String, Object> responseObjects) {
				return message;
			}
		};
		this.addMessage(testMessage, responseKey, responseHandler, unsubscribeInStop);
	}

	public void addMessage(Message message, String responseKey, ResponseHandler responseHandler) {
		this.addMessage(message, responseKey, responseHandler, false);
	}

	/**
	 * Sets a handler for processing other messages than the server responses
	 * (to be more precise: messages for which there is no corresponding
	 * response handler)
	 */
	public void addUnsolicitedMessageHandler(MessageHandler messageHandler) {
		this.unsolicitedMessageHandler = messageHandler;
	}

	/**
	 * Returns a CountDownLatch that can be used to wait for the next server
	 * response(s) to be processed by the client
	 */
	public CountDownLatch getNewMessageReceivedLatch(int count) {
		return channelHandler.getNewMessageReceivedLatch(count);
	}

}
