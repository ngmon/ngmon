package cz.muni.fi.xtovarn.heimdall.test;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;
import cz.muni.fi.xtovarn.heimdall.test.SimpleMessageWrapper.PrepareMessageAction;

public class ProtocolTest {

	@Before
	public void before() {
		MyAssert.clearAssertionError();
	}

	@After
	public void after() {
		MyAssert.throwAssertionErrorIfAny();
	}

	private static class ConnectMessageContainer extends MessageContainerImpl {

		public enum USER_TYPE {
			VALID, INVALID_USER, INVALID_PASSWORD
		}

		private static final User validUser = new User("user0", "password0");
		private static final User invalidUser = new User("incorrectUser", "password0");
		private static final User invalidPassword = new User("user0", "invalidPassword");

		public ConnectMessageContainer() {
			this(USER_TYPE.VALID);
		}

		public ConnectMessageContainer(USER_TYPE userType) {
			super();

			User user = null;
			switch (userType) {
			case VALID:
				user = validUser;
				break;
			case INVALID_USER:
				user = invalidUser;
				break;
			case INVALID_PASSWORD:
				user = invalidPassword;
				break;
			}
			try {
				this.addMessage(new SimpleMessageWrapper(Directive.CONNECT, getMapper().writeValueAsBytes(user)));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class DisconnectMessageContainer extends ConnectMessageContainer {

		public DisconnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.DISCONNECT, "".getBytes()));
		}

	}

	private static class DisconnectWithoutConnectMessageContainer extends MessageContainerImpl {

		public DisconnectWithoutConnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.DISCONNECT, "".getBytes()));
		}

	}

	private static class DoubleDisconnectMessageContainer extends DisconnectMessageContainer {

		public DoubleDisconnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.DISCONNECT, "".getBytes()));
		}

	}

	private static class SubscribeAfterDisconnectMessageContainer extends DisconnectMessageContainer {

		public SubscribeAfterDisconnectMessageContainer() {
			try {
				Map<String, String> subscriptionMap = new HashMap<>();
				subscriptionMap.put("priority", "#lt 2");
				this.addMessage(new SimpleMessageWrapper(Directive.SUBSCRIBE, getMapper().writeValueAsBytes(
						subscriptionMap)));

			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private static class SubscribeMessageContainer extends ConnectMessageContainer {

		public enum MESSAGE_TYPE {
			VALID,
			INVALID_LESS_THAN_STRING,
			INVALID_NO_OPERATOR,
			INVALID_OPERATOR,
			INVALID_NO_VALUE,
			INVALID_NO_ATTRIBUTE,
			INVALID_NO_OPERATOR_VALUE
		}

		public SubscribeMessageContainer() {
			this(MESSAGE_TYPE.VALID);
		}

		public SubscribeMessageContainer(MESSAGE_TYPE messageType) {
			try {
				this.addMessage(new SimpleMessageWrapper(Directive.SUBSCRIBE, getMapper().writeValueAsBytes(
						getSubscriptionMap(messageType))));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

		private Map<String, String> getSubscriptionMap(MESSAGE_TYPE messageType) {
			Map<String, String> subscriptionMap = new HashMap<>();
			switch (messageType) {
			case VALID:
				subscriptionMap.put("priority", "#lt 2");
				break;
			case INVALID_LESS_THAN_STRING:
				subscriptionMap.put("priority", "#lt abcd");
				break;
			case INVALID_NO_OPERATOR:
				subscriptionMap.put("priority", "foo");
				break;
			case INVALID_OPERATOR:
				subscriptionMap.put("foo", "#foo 3");
				break;
			case INVALID_NO_VALUE:
				subscriptionMap.put("foo", "#eq");
				break;
			case INVALID_NO_ATTRIBUTE:
				subscriptionMap.put("", "#lt 2");
				break;
			case INVALID_NO_OPERATOR_VALUE:
				subscriptionMap.put("foo", "");
				break;
			}

			return subscriptionMap;
		}

	}

	private static class UnsubscribeMessageContainer extends SubscribeMessageContainer {

		public UnsubscribeMessageContainer(SubscriptionHandler handler, final boolean validId) {
			this.addMessage(new SimpleMessageWrapper(new PrepareMessageAction() {

				@Override
				public Message perform(Object object) {
					SubscriptionHandler handler = (SubscriptionHandler) object;

					Map<String, Long> unsubscribeMap = new HashMap<>();
					Long usedId = handler.getSubscriptionId();
					if (!validId)
						usedId++;
					unsubscribeMap.put(Constants.SUBSCRIPTION_ID_TITLE, usedId);
					try {
						return new SimpleMessage(Directive.UNSUBSCRIBE, getMapper().writeValueAsBytes(unsubscribeMap));
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
				}
			}, handler));
		}

	}

	private static class SubscribeWithoutConnectMessageContainer extends MessageContainerImpl {

		public SubscribeWithoutConnectMessageContainer() {
			try {
				Map<String, String> subscriptionMap = new HashMap<>();
				subscriptionMap.put("priority", "#lt 2");
				this.addMessage(new SimpleMessageWrapper(Directive.SUBSCRIBE, getMapper().writeValueAsBytes(
						subscriptionMap)));

			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private static class ReadyMessageContainer extends ConnectMessageContainer {

		public ReadyMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.READY, "".getBytes()));
		}

	}
	
	private static class SubscribeAfterReadyMessageContainer extends ReadyMessageContainer {

		public SubscribeAfterReadyMessageContainer() {
			try {
				Map<String, String> subscriptionMap = new HashMap<>();
				subscriptionMap.put("priority", "#lt 2");
				this.addMessage(new SimpleMessageWrapper(Directive.SUBSCRIBE, getMapper().writeValueAsBytes(
						subscriptionMap)));

			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

	}
	
	private static class ReadyAfterReadyMessageContainer extends ReadyMessageContainer {

		public ReadyAfterReadyMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.READY, "".getBytes()));
		}

	}

	private static class ReadyWithoutConnectMessageContainer extends MessageContainerImpl {

		public ReadyWithoutConnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.READY, "".getBytes()));
		}

	}

	private static class ReadyAfterDisconnectMessageContainer extends DisconnectMessageContainer {

		public ReadyAfterDisconnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.READY, "".getBytes()));
		}

	}

	private static class StopMessageContainer extends ReadyMessageContainer {

		public StopMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.STOP, "".getBytes()));
		}

	}
	
	private static class StopAfterStopMessageContainer extends StopMessageContainer {

		public StopAfterStopMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.STOP, "".getBytes()));
		}

	}
	
	private static class StopWithoutReadyMessageContainer extends ConnectMessageContainer {

		public StopWithoutReadyMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.STOP, "".getBytes()));
		}

	}
	
	private static class StopWithoutConnectMessageContainer extends MessageContainerImpl {

		public StopWithoutConnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.STOP, "".getBytes()));
		}

	}
	
	private static class StopAfterDisconnectMessageContainer extends DisconnectMessageContainer {

		public StopAfterDisconnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.STOP, "".getBytes()));
		}

	}
	
	private static class GetMessageContainer extends ConnectMessageContainer {

		public GetMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.GET, "".getBytes()));
		}

	}
	
	private static class GetWhenSendingMessageContainer extends ReadyMessageContainer {
		
		public GetWhenSendingMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.GET, "".getBytes()));
		}
		
	}
	
	private static class GetWithoutConnectMessageContainer extends MessageContainerImpl {

		public GetWithoutConnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.GET, "".getBytes()));
		}

	}
	
	private static class GetAfterDisconnectMessageContainer extends DisconnectMessageContainer {

		public GetAfterDisconnectMessageContainer() {
			this.addMessage(new SimpleMessageWrapper(Directive.GET, "".getBytes()));
		}

	}

	private static class ConnectHandler extends TestClientHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		private Directive expectedDirective;
		private Long connectionId;

		public ConnectHandler() {
			this(Directive.CONNECTED);
		}

		public ConnectHandler(Directive expectedDirective) {
			this.expectedDirective = expectedDirective;
		}

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(expectedDirective, message.getDirective());
				if (expectedDirective.equals(Directive.CONNECTED)) {
					@SuppressWarnings("unchecked")
					Map<String, Number> connectionIdMap = (Map<String, Number>) getMapper().readValue(
							message.getBody(), Map.class);
					connectionId = connectionIdMap.get("connectionId").longValue();
					MyAssert.assertNotNull(connectionId);
					System.out.println("Connection ID: " + connectionId);
				}
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

		public Long getConnectionId() {
			return connectionId;
		}

	}

	private static class DisconnectHandler extends ConnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ACK, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}

	private static class DisconnectWithoutConnectHandler extends TestClientHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}

	private static class DoubleDisconnectHandler extends DisconnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}

	private static class SubscribeAfterDisconnectHandler extends DisconnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}

	private static class SubscriptionHandler extends ConnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		private Directive expectedDirective;
		private Long subscriptionId;

		public SubscriptionHandler() {
			this(Directive.ACK);
		}

		public SubscriptionHandler(Directive expectedDirective) {
			this.expectedDirective = expectedDirective;
		}

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			// parent method must be called in order to save the connection ID
			super.processReceivedMessage(ctx, e);

			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(expectedDirective, message.getDirective());
				if (expectedDirective.equals(Directive.ACK)) {
					Map<String, Number> subscriptionIdMap = (Map<String, Number>) getMapper().readValue(
							message.getBody(), Map.class);
					subscriptionId = subscriptionIdMap.get(Constants.SUBSCRIPTION_ID_TITLE).longValue();
					MyAssert.assertNotNull(subscriptionId);
					System.out.println("Subscription ID: " + subscriptionId);
				}
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

		public Long getSubscriptionId() {
			return subscriptionId;
		}

	}

	private static class UnsubscribeHandler extends SubscriptionHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		private Directive expectedDirective;

		public UnsubscribeHandler(Directive expectedDirective) {
			this.expectedDirective = expectedDirective;
		}

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			super.processReceivedMessage(ctx, e);

			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(expectedDirective, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}

	private static class SubscribeWithoutConnectHandler extends TestClientHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}
	}

	private static class ReadyHandler extends ConnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ACK, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class SubscribeAfterReadyHandler extends ReadyHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class ReadyAfterReadyHandler extends ReadyHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class ReadyWithoutConnectHandler extends TestClientHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class ReadyAfterDisconnectHandler extends DisconnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class StopHandler extends ReadyHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ACK, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class StopAfterStopHandler extends StopHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class StopWithoutReadyHandler extends ConnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class StopWithoutConnectHandler extends TestClientHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class StopAfterDisconnectHandler extends DisconnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class GetHandler extends ConnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ACK, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class GetWhenSendingHandler extends ReadyHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class GetWithoutConnectHandler extends TestClientHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}
	
	private static class GetAfterDisconnectHandler extends DisconnectHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandlerPrivate()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(Directive.ERROR, message.getDirective());
			}
		}

		private int getMessagesProcessedByHandlerPrivate() {
			return super.getMessagesProcessedByHandler() + MESSAGES_PROCESSED_BY_HANDLER;
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return getMessagesProcessedByHandlerPrivate();
		}

	}

	@Test
	public void testConnect() throws JsonProcessingException {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("connect", new ConnectHandler());
		TestClient client = new TestClient(pipelineFactory, new ConnectMessageContainer());
		client.run();
	}

	@Test
	public void testConnectIncorrectUser() throws JsonProcessingException {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("connect", new ConnectHandler(Directive.ERROR));
		TestClient client = new TestClient(pipelineFactory, new ConnectMessageContainer(
				ConnectMessageContainer.USER_TYPE.INVALID_USER));
		client.run();
	}

	@Test
	public void testConnectIncorrectPassword() throws JsonProcessingException {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("connect", new ConnectHandler(Directive.ERROR));
		TestClient client = new TestClient(pipelineFactory, new ConnectMessageContainer(
				ConnectMessageContainer.USER_TYPE.INVALID_PASSWORD));
		client.run();
	}

	private void testSubscribeHelper(SubscriptionHandler subscriptionHandler,
			SubscribeMessageContainer subscribeMessageContainer) {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("subscribe", subscriptionHandler);
		TestClient client = new TestClient(pipelineFactory, subscribeMessageContainer);
		client.run();
	}

	@Test
	public void testSubscribe() {
		testSubscribeHelper(new SubscriptionHandler(), new SubscribeMessageContainer());
	}

	@Test
	public void testSubscribeInvalidLessThanString() {
		testSubscribeHelper(new SubscriptionHandler(Directive.ERROR), new SubscribeMessageContainer(
				SubscribeMessageContainer.MESSAGE_TYPE.INVALID_LESS_THAN_STRING));
	}

	@Test
	public void testSubscribeInvalidNoOperator() {
		testSubscribeHelper(new SubscriptionHandler(Directive.ERROR), new SubscribeMessageContainer(
				SubscribeMessageContainer.MESSAGE_TYPE.INVALID_NO_OPERATOR));
	}

	@Test
	public void testSubscribeInvalidOperator() {
		testSubscribeHelper(new SubscriptionHandler(Directive.ERROR), new SubscribeMessageContainer(
				SubscribeMessageContainer.MESSAGE_TYPE.INVALID_OPERATOR));
	}

	@Test
	public void testSubscribeInvalidNoValue() {
		testSubscribeHelper(new SubscriptionHandler(Directive.ERROR), new SubscribeMessageContainer(
				SubscribeMessageContainer.MESSAGE_TYPE.INVALID_NO_VALUE));
	}

	@Test
	public void testSubscribeInvalidNoAttribute() {
		testSubscribeHelper(new SubscriptionHandler(Directive.ERROR), new SubscribeMessageContainer(
				SubscribeMessageContainer.MESSAGE_TYPE.INVALID_NO_ATTRIBUTE));
	}

	@Test
	public void testSubscribeInvalidNoOperatorValue() {
		testSubscribeHelper(new SubscriptionHandler(Directive.ERROR), new SubscribeMessageContainer(
				SubscribeMessageContainer.MESSAGE_TYPE.INVALID_NO_OPERATOR_VALUE));
	}

	@Test
	public void testUnsubscribe() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		UnsubscribeHandler handler = new UnsubscribeHandler(Directive.ACK);
		pipelineFactory.addHandler("unsubscribe", handler);
		TestClient client = new TestClient(pipelineFactory, new UnsubscribeMessageContainer(handler, true));
		client.run();
	}

	@Test
	public void testUnsubscribeInvalidSubscriptionId() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		UnsubscribeHandler handler = new UnsubscribeHandler(Directive.ERROR);
		pipelineFactory.addHandler("unsubscribe", handler);
		TestClient client = new TestClient(pipelineFactory, new UnsubscribeMessageContainer(handler, false));
		client.run();
	}

	@Test
	public void testSubscribeWithoutConnectingFirst() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("subscribe", new SubscribeWithoutConnectHandler());
		TestClient client = new TestClient(pipelineFactory, new SubscribeWithoutConnectMessageContainer());
		client.run();
	}

	@Test
	public void testDisconnectAfterConnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("disconnect", new DisconnectHandler());
		TestClient client = new TestClient(pipelineFactory, new DisconnectMessageContainer());
		client.run();
	}

	@Test
	public void testSubscribeAfterDisconnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("subscribe", new SubscribeAfterDisconnectHandler());
		TestClient client = new TestClient(pipelineFactory, new SubscribeAfterDisconnectMessageContainer());
		client.run();
	}

	@Test
	public void testDisconnectWithoutConnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("disconnect", new DisconnectWithoutConnectHandler());
		TestClient client = new TestClient(pipelineFactory, new DisconnectWithoutConnectMessageContainer());
		client.run();
	}

	@Test
	public void testDoubleDisconnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("disconnect", new DoubleDisconnectHandler());
		TestClient client = new TestClient(pipelineFactory, new DoubleDisconnectMessageContainer());
		client.run();
	}

	@Test
	public void testReady() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("ready", new ReadyHandler());
		TestClient client = new TestClient(pipelineFactory, new ReadyMessageContainer());
		client.run();
	}
	
	@Test
	public void testReadyWithoutConnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("ready", new ReadyWithoutConnectHandler());
		TestClient client = new TestClient(pipelineFactory, new ReadyWithoutConnectMessageContainer());
		client.run();
	}
	
	@Test
	public void testReadyAfterDisconnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("ready", new ReadyAfterDisconnectHandler());
		TestClient client = new TestClient(pipelineFactory, new ReadyAfterDisconnectMessageContainer());
		client.run();
	}
	
	@Test
	public void testStop() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("stop", new StopHandler());
		TestClient client = new TestClient(pipelineFactory, new StopMessageContainer());
		client.run();
	}
	
	@Test
	public void testStopWithoutReady() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("stop", new StopWithoutReadyHandler());
		TestClient client = new TestClient(pipelineFactory, new StopWithoutReadyMessageContainer());
		client.run();
	}
	
	@Test
	public void testStopWithoutConnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("stop", new StopWithoutConnectHandler());
		TestClient client = new TestClient(pipelineFactory, new StopWithoutConnectMessageContainer());
		client.run();
	}
	
	@Test
	public void testStopAfterDisconnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("stop", new StopAfterDisconnectHandler());
		TestClient client = new TestClient(pipelineFactory, new StopAfterDisconnectMessageContainer());
		client.run();
	}
	
	@Test
	public void testGet() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("get", new GetHandler());
		TestClient client = new TestClient(pipelineFactory, new GetMessageContainer());
		client.run();
	}
	
	@Test
	public void testGetWhenSending() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("get", new GetWhenSendingHandler());
		TestClient client = new TestClient(pipelineFactory, new GetWhenSendingMessageContainer());
		client.run();
	}
	
	@Test
	public void testGetWithoutConnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("get", new GetWithoutConnectHandler());
		TestClient client = new TestClient(pipelineFactory, new GetWithoutConnectMessageContainer());
		client.run();
	}
	
	@Test
	public void testGetAfterDisconnect() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("get", new GetAfterDisconnectHandler());
		TestClient client = new TestClient(pipelineFactory, new GetAfterDisconnectMessageContainer());
		client.run();
	}
	
	@Test
	public void testReadyAfterReady() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("ready", new ReadyAfterReadyHandler());
		TestClient client = new TestClient(pipelineFactory, new ReadyAfterReadyMessageContainer());
		client.run();
	}
	
	@Test
	public void testStopAfterStop() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("stop", new StopAfterStopHandler());
		TestClient client = new TestClient(pipelineFactory, new StopAfterStopMessageContainer());
		client.run();
	}
	
	@Test
	public void testSubscribeAfterReady() {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("subscribe", new SubscribeAfterReadyHandler());
		TestClient client = new TestClient(pipelineFactory, new SubscribeAfterReadyMessageContainer());
		client.run();
	}
}
