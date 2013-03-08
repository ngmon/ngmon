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

	private static class SubscribeMessageContainer extends ConnectMessageContainer {

		public enum MESSAGE_TYPE {
			VALID, INVALID_LESS_THAN_STRING, INVALID_NO_OPERATOR, INVALID_OPERATOR, INVALID_NO_VALUE, INVALID_NO_ATTRIBUTE, INVALID_NO_OPERATOR_VALUE
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
}
