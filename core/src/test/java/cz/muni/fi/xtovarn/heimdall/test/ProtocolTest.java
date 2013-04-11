package cz.muni.fi.xtovarn.heimdall.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sleepycat.je.DatabaseException;

import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;
import cz.muni.fi.xtovarn.heimdall.test.util.NgmonLauncher;
import cz.muni.fi.xtovarn.heimdall.test.util.ObjectMapperWrapper;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient;
import cz.muni.fi.xtovarn.heimdall.test.util.TestResponseHandlers;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.ResponseHandler;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.TestMessage;

public class ProtocolTest {

	private static final String VALID_USER_LOGIN = "user0";
	private static final String VALID_USER_PASSCODE = "password0";
	private static final String INVALID_USER_LOGIN = "incorrectUser";
	private static final String INVALID_USER_PASSCODE = "invalidPassword";

	private static final String CONNECTION_ID_KEY = "connectionId";
	private static final String SUBSCRIPTION_ID_KEY = "subscriptionId";

	private static ObjectMapperWrapper mapper = new ObjectMapperWrapper();
	
	private NgmonLauncher ngmon = null;
	private TestClient testClient = null;

	@Before
	public void setUp() throws DatabaseException, IOException, InterruptedException {
		this.ngmon = new NgmonLauncher();
		this.ngmon.start();
		
		testClient = new TestClient();
	}

	@After
	public void tearDown() {
		testClient.stop();
		
		this.ngmon.stop();
	}

	private void connect() {
		testClient.addMessage(
				new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytesNoExceptions(new User(VALID_USER_LOGIN,
						VALID_USER_PASSCODE))), CONNECTION_ID_KEY, TestResponseHandlers.CONNECT_RESPONSE_HANDLER);
	}

	private void disconnect() {
		testClient.addMessage(new SimpleMessage(Directive.DISCONNECT, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}
	
	private void ready() {
		testClient.addMessage(new SimpleMessage(Directive.READY, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}
	
	private void readyWithError() {
		testClient.addMessage(new SimpleMessage(Directive.READY, "".getBytes()), null,
				TestResponseHandlers.ERROR_RESPONSE_HANDLER);
	}
	
	private void stop() {
		testClient.addMessage(new SimpleMessage(Directive.STOP, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}
	
	private void stopWithError() {
		testClient.addMessage(new SimpleMessage(Directive.STOP, "".getBytes()), null,
				TestResponseHandlers.ERROR_RESPONSE_HANDLER);
	}
	
	private void get() {
		testClient.addMessage(new SimpleMessage(Directive.GET, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}
	
	private void getWithError() {
		testClient.addMessage(new SimpleMessage(Directive.GET, "".getBytes()), null,
				TestResponseHandlers.ERROR_RESPONSE_HANDLER);
	}

	@Test
	public void testConnect() throws InterruptedException {
		testClient.addMessage(
				new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytesNoExceptions(new User(VALID_USER_LOGIN,
						VALID_USER_PASSCODE))), CONNECTION_ID_KEY, new ResponseHandler() {

					@Override
					public Object processResponse(MessageEvent messageEvent) {
						SimpleMessage message = (SimpleMessage) messageEvent.getMessage();
						Assert.assertEquals(Directive.CONNECTED, message.getDirective());
						@SuppressWarnings("unchecked")
						Map<String, Number> connectionIdMap = (Map<String, Number>) mapper.readValueNoExceptions(
								message.getBody(), Map.class);
						Long connectionId = connectionIdMap.get(Constants.CONNECTION_ID_TITLE).longValue();
						Assert.assertNotNull(connectionId);
						System.out.println("Connection ID: " + connectionId);
						return connectionId;
					}
				});
		testClient.run();
	}

	@Test
	public void testConnectIncorrectUser() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytesNoExceptions(new User(INVALID_USER_LOGIN,
						VALID_USER_PASSCODE))), null, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testConnectIncorrectPassword() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytesNoExceptions(new User(VALID_USER_LOGIN,
						INVALID_USER_PASSCODE))), null, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	private Map<String, String> getSubscriptionMap(String attributeName, String predicate) {
		Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put(attributeName, predicate);
		return subscriptionMap;
	}

	@Test
	public void testSubscribe() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "#lt 2"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.SUBSCRIBE_RESPONSE_HANDLER,
				true);
		testClient.run();
	}

	@Test
	public void testSubscribeInvalidLessThanString() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "#lt abcd"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testSubscribeInvalidNoOperator() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "foo"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testSubscribeInvalidOperator() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap("foo",
						"#foo 3"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testSubscribeInvalidNoValue() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap("foo",
						"#eq"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testSubscribeInvalidNoAttribute() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap("",
						"#lt 2"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testSubscribeInvalidNoOperatorValue() throws InterruptedException {
		connect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap("foo",
						""))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testUnsubscribe() throws InterruptedException {
		connect();
		// subscribe first
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "#lt 2"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.SUBSCRIBE_RESPONSE_HANDLER);
		testClient.addMessage(new TestMessage() {
			@Override
			public Message getMessage(Map<String, Object> responseMap) {
				Map<String, Long> unsubscribeMap = new HashMap<>();
				unsubscribeMap.put(Constants.SUBSCRIPTION_ID_TITLE, (Long) responseMap.get(SUBSCRIPTION_ID_KEY));
				return new SimpleMessage(Directive.UNSUBSCRIBE, mapper.writeValueAsBytesNoExceptions(unsubscribeMap));
			}
		}, null, TestResponseHandlers.ACK_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testUnsubscribeInvalidSubscriptionId() throws InterruptedException {
		connect();
		// subscribe first
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "#lt 2"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.SUBSCRIBE_RESPONSE_HANDLER,
				true);
		testClient.addMessage(new TestMessage() {
			@Override
			public Message getMessage(Map<String, Object> responseMap) {
				Map<String, Long> unsubscribeMap = new HashMap<>();
				unsubscribeMap.put(Constants.SUBSCRIPTION_ID_TITLE, (Long) responseMap.get(SUBSCRIPTION_ID_KEY) + 1);
				return new SimpleMessage(Directive.UNSUBSCRIBE, mapper.writeValueAsBytesNoExceptions(unsubscribeMap));
			}
		}, null, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testSubscribeWithoutConnectingFirst() throws InterruptedException {
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "#lt 2"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testDisconnectAfterConnect() throws InterruptedException {
		connect();
		testClient.addMessage(new SimpleMessage(Directive.DISCONNECT, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testSubscribeAfterDisconnect() throws InterruptedException {
		connect();
		disconnect();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "#lt 2"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testDisconnectWithoutConnect() throws InterruptedException {
		testClient.addMessage(new SimpleMessage(Directive.DISCONNECT, "".getBytes()), null,
				TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testDoubleDisconnect() throws InterruptedException {
		connect();
		disconnect();
		testClient.addMessage(new SimpleMessage(Directive.DISCONNECT, "".getBytes()), null,
				TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}

	@Test
	public void testReady() throws InterruptedException {
		connect();
		ready();
		testClient.run();
	}
	
	@Test
	public void testReadyWithoutConnect() throws InterruptedException {
		testClient.addMessage(new SimpleMessage(Directive.READY, "".getBytes()), null,
				TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}
	
	@Test
	public void testReadyAfterDisconnect() throws InterruptedException {
		connect();
		disconnect();
		testClient.addMessage(new SimpleMessage(Directive.READY, "".getBytes()), null,
				TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}
	
	@Test
	public void testStop() throws InterruptedException {
		connect();
		ready();
		stop();
		testClient.run();
	}
	
	@Test
	public void testStopWithoutReady() throws InterruptedException {
		connect();
		stopWithError();
		testClient.run();
	}
	
	@Test
	public void testStopWithoutConnect() throws InterruptedException {
		stopWithError();
		testClient.run();
	}
	
	@Test
	public void testStopAfterDisconnect() throws InterruptedException {
		connect();
		disconnect();
		stopWithError();
		testClient.run();
	}
	
	@Test
	public void testGet() throws InterruptedException {
		connect();
		get();
		testClient.run();
	}
	
	@Test
	public void testGetWhenSending() throws InterruptedException {
		connect();
		ready();
		getWithError();
		testClient.run();
	}
	
	@Test
	public void testGetWithoutConnect() throws InterruptedException {
		getWithError();
		testClient.run();
	}
	
	@Test
	public void testGetAfterDisconnect() throws InterruptedException {
		connect();
		disconnect();
		getWithError();
		testClient.run();
	}
	
	@Test
	public void testReadyAfterReady() throws InterruptedException {
		connect();
		ready();
		readyWithError();
		testClient.run();
	}
	
	@Test
	public void testStopAfterStop() throws InterruptedException {
		connect();
		ready();
		stop();
		stopWithError();
		testClient.run();
	}
	
	@Test
	public void testSubscribeAfterReady() throws InterruptedException {
		connect();
		ready();
		testClient.addMessage(
				new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(getSubscriptionMap(
						"priority", "#lt 2"))), SUBSCRIPTION_ID_KEY, TestResponseHandlers.ERROR_RESPONSE_HANDLER);
		testClient.run();
	}
}
