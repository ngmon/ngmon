package cz.muni.fi.xtovarn.heimdall.test;

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

public class TestClient2 {

	public interface TestMessage {
		public Message getMessage(Map<String, Object> responseMap);
	}

	public interface ResponseHandler {
		public Object processResponse(MessageEvent messageEvent);
	}

	public interface MessageHandler {
		public void processMessage(MessageEvent messageEvent);
	}

	public static class DefaultMessageHandler implements MessageHandler {

		@Override
		public void processMessage(MessageEvent messageEvent) {
		}

	}

	public static final String MESSAGE_HANDLER_TITLE = "messageHandler";

	public static final long TIMEOUT = 5;
	// for debugging, set to MINUTES, otherwise use SECONDS
	public static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;
	public static final long EVENT_TIMEOUT_IN_MILLIS = 1000;

	private List<TestMessage> messages = new ArrayList<>();
	private Map<String, Message> responseMap = new HashMap<>();
	private Map<String, Object> responseObjects = new HashMap<>();
	private List<String> responseKeys = new ArrayList<>();
	private List<ResponseHandler> responseHandlers = new ArrayList<>();
	private MessageHandler unsolicitedMessageHandler = null;
	private List<Boolean> unsubscribeInStopList = new ArrayList<>();
	private Collection<Long> subscriptionIds = new ArrayList<>();
	private Channel channel = null;
	private ChannelFactory factory;
	private TestClient2Handler channelHandler;
	private int messagesProcessed = 0;
	private ObjectMapper mapper = new ObjectMapper();

	private boolean isRunning = false;

	public void run(boolean close) throws InterruptedException {
		factory = new NioClientSocketChannelFactory(Executors.newSingleThreadExecutor(),
				Executors.newSingleThreadExecutor());
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		TestClient2PipelineFactory pipelineFactory = new TestClient2PipelineFactory(responseHandlers,
				unsolicitedMessageHandler);
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(NettyServer.SERVER_PORT));
		future.awaitUninterruptibly();
		channel = future.getChannel();
		channelHandler = (TestClient2Handler) channel.getPipeline().getContext(MESSAGE_HANDLER_TITLE).getHandler();

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
			latch.await(TIMEOUT, TIMEOUT_UNIT);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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

	public void stop() {
		if (isRunning) {
			this.unsubscribeAll();

			channel.close().awaitUninterruptibly();
			factory.releaseExternalResources();

			isRunning = false;
		}
	}

	public void addMessage(TestMessage testMessage, String responseKey, ResponseHandler responseHandler,
			boolean unsubscribeInStop) {
		if (responseHandler == null && unsubscribeInStop)
			throw new IllegalArgumentException("If the message is SUBSCRIBE, ResponseHandler must not be null");
		messages.add(testMessage);
		responseKeys.add(responseKey);
		responseHandlers.add(responseHandler);
		unsubscribeInStopList.add(unsubscribeInStop);
	}

	public void addMessage(TestMessage testMessage, String responseKey, ResponseHandler responseHandler) {
		this.addMessage(testMessage, responseKey, responseHandler, false);
	}

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

	public void addUnsolicitedMessageHandler(MessageHandler messageHandler) {
		this.unsolicitedMessageHandler = messageHandler;
	}

	public CountDownLatch getNewMessageReceivedLatch(int count) {
		return channelHandler.getNewMessageReceivedLatch(count);
	}

}
