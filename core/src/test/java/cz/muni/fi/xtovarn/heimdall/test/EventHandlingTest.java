package cz.muni.fi.xtovarn.heimdall.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sleepycat.je.DatabaseException;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.test.util.NgmonLauncher;
import cz.muni.fi.xtovarn.heimdall.test.util.ObjectMapperWrapper;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient;
import cz.muni.fi.xtovarn.heimdall.test.util.TestMessageHandler;
import cz.muni.fi.xtovarn.heimdall.test.util.TestResponseHandlers;
import cz.muni.fi.xtovarn.heimdall.test.util.TestSensor;

public class EventHandlingTest {

	private static final String JSON_FILE_1 = "events.jsons";
	private static final String JSON_FILE_2 = "events2.jsons";
	private static final String CONNECTION_ID_KEY = "connectionId";
	private static final String SUBSCRIPTION_ID_KEY_PREFIX = "subscriptionId";
	private static final boolean INCLUDE_READY_DEFAULT = true;
	private static final boolean INCLUDE_STOP_DEFAULT = false;

	private static final String VALID_USER_LOGIN = "user0";
	private static final String VALID_USER_PASSCODE = "password0";
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

	private void ready() {
		testClient.addMessage(new SimpleMessage(Directive.READY, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}

	private void stop() {
		testClient.addMessage(new SimpleMessage(Directive.STOP, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}

	private List<MessageEvent> testSubscribe(Collection<Map<String, String>> subscriptionMaps, int expectedEventCount,
			String jsonFileName, boolean includeReady, boolean includeStop) throws InterruptedException, IOException {
		connect();

		int i = 1;
		for (Map<String, String> subscriptionMap : subscriptionMaps) {
			testClient.addMessage(
					new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(subscriptionMap)),
					SUBSCRIPTION_ID_KEY_PREFIX + i++, TestResponseHandlers.SUBSCRIBE_RESPONSE_HANDLER, true);
		}

		if (includeReady)
			ready();

		if (includeStop)
			stop();

		TestMessageHandler messageHandler = new TestMessageHandler();
		testClient.addUnsolicitedMessageHandler(messageHandler);

		testClient.run(false);

		TestSensor sensor = new TestSensor();
		BufferedReader br = new BufferedReader(new FileReader("src/main/resources/" + jsonFileName));
		String line;
		while ((line = br.readLine()) != null) {
			sensor.sendString(line);
		}
		br.close();
		sensor.close();

		Thread.sleep(TestClient.EVENT_TIMEOUT_IN_MILLIS);

		assertEquals((!includeReady || includeStop) ? 0 : expectedEventCount, messageHandler.getMessageCount());

		return messageHandler.getMessageEventList();
	}

	private List<MessageEvent> testSubscribe(Collection<Map<String, String>> subscriptionMaps, int expectedEventCount,
			String jsonFileName) throws InterruptedException, IOException {
		return testSubscribe(subscriptionMaps, expectedEventCount, jsonFileName, INCLUDE_READY_DEFAULT,
				INCLUDE_STOP_DEFAULT);
	}

	private List<MessageEvent> testSubscribeOneSubscription(Map<String, String> subscriptionMap,
			int expectedEventCount, String jsonFileName, boolean includeReady, boolean includeStop)
			throws InterruptedException, IOException {
		List<Map<String, String>> subscriptionMaps = new ArrayList<>();
		subscriptionMaps.add(subscriptionMap);
		return testSubscribe(subscriptionMaps, expectedEventCount, jsonFileName, includeReady, includeStop);
	}

	private List<MessageEvent> testSubscribeOneSubscription(Map<String, String> subscriptionMap,
			int expectedEventCount, String jsonFileName) throws InterruptedException, IOException {
		List<Map<String, String>> subscriptionMaps = new ArrayList<>();
		subscriptionMaps.add(subscriptionMap);
		return testSubscribe(subscriptionMaps, expectedEventCount, jsonFileName, INCLUDE_READY_DEFAULT,
				INCLUDE_STOP_DEFAULT);
	}

	private List<MessageEvent> testSubscribeOnePredicate(String attribute, String operator, String value,
			int expectedEventCount, String jsonFileName) throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put(attribute, "#" + operator + " " + value);

		return testSubscribeOneSubscription(subscriptionMap, expectedEventCount, jsonFileName, INCLUDE_READY_DEFAULT,
				INCLUDE_STOP_DEFAULT);
	}

	@Test
	public void testSubscribeOnProcessIdLessThan5000() throws InterruptedException, IOException {
		testSubscribeOnePredicate("processId", "le", "5000", 10, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnProcessIdGreaterThan5000() throws InterruptedException, IOException {
		testSubscribeOnePredicate("processId", "gt", "5000", 0, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnProcessIdEquals5000() throws InterruptedException, IOException {
		testSubscribeOnePredicate("processId", "eq", "5000", 0, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnProcessIdEquals4219() throws InterruptedException, IOException {
		testSubscribeOnePredicate("processId", "eq", "4219", 10, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnTypeEqualsStarted5() throws InterruptedException, IOException {
		List<MessageEvent> messageEventList = testSubscribeOnePredicate("type", "eq", "'org.linux.cron.Started5'", 1,
				JSON_FILE_1);
		assertEquals(1, messageEventList.size());
		MessageEvent messageEvent = messageEventList.get(0);
		Message message = (Message) messageEvent.getMessage();
		Directive directive = message.getDirective();
		assertTrue(directive.equals(Directive.SEND_JSON) || directive.equals(Directive.SEND_SMILE));
		Event event = JSONEventMapper.bytesToEvent(message.getBody());
		assertEquals("Cron", event.getApplication());
		assertEquals("domain.localhost.cz", event.getHostname());
		assertEquals(5, event.getLevel());
		assertEquals(4, event.getPriority());
		assertEquals("cron", event.getProcess());
		assertEquals(4219, event.getProcessId());
		assertEquals("org.linux.cron.Started5", event.getType());
	}

	@Test
	public void testSubscribeOnTypeEqualsStarted20() throws InterruptedException, IOException {
		testSubscribeOnePredicate("type", "eq", "'org.linux.cron.Started20'", 0, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnTypePrefixStarted20() throws InterruptedException, IOException {
		testSubscribeOnePredicate("type", "pref", "'org.linux.cron.Started20'", 0, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnTypePrefixStarted() throws InterruptedException, IOException {
		testSubscribeOnePredicate("type", "pref", "'org.linux.cron.Started'", 10, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnTypePrefixStarted1() throws InterruptedException, IOException {
		testSubscribeOnePredicate("type", "pref", "'org.linux.cron.Started1'", 2, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnTypePrefixFoo() throws InterruptedException, IOException {
		testSubscribeOnePredicate("type", "pref", "'foo'", 0, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnNonexistingAttributeEqualsFoo() throws InterruptedException, IOException {
		testSubscribeOnePredicate("foo", "eq", "'foo'", 0, JSON_FILE_1);
	}

	@Test
	public void testSubscribeOnNonexistingAttributePrefixFoo() throws InterruptedException, IOException {
		testSubscribeOnePredicate("foo", "pref", "'foo'", 0, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionNoneMatched2() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#eq 'org.linux.cron.Started8'");
		subscriptionMap.put("processId", "#eq 5000");
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionNoneMatched() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#eq 'org.linux.cron.Started20'");
		subscriptionMap.put("processId", "#eq 5000");
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionNoneMatched5() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#pref 'org.linux.cron.Started5'");
		subscriptionMap.put("processId", "#eq 5000");
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionNoneMatched4() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#pref 'org.linux.cron.Started1'");
		subscriptionMap.put("processId", "#eq 5000");
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionNoneMatched3() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#eq 'org.linux.cron.Started20'");
		subscriptionMap.put("processId", "#eq 4219");
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionAllMatched() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#pref 'org.linux.cron.Started'");
		subscriptionMap.put("processId", "#eq 4219");
		testSubscribeOneSubscription(subscriptionMap, 10, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionOneMatched() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#pref 'org.linux.cron.Started5'");
		subscriptionMap.put("processId", "#eq 4219");
		testSubscribeOneSubscription(subscriptionMap, 1, JSON_FILE_1);
	}

	@Test
	public void testTwoPredicatesInOneSubscriptionTwoMatched() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("type", "#pref 'org.linux.cron.Started1'");
		subscriptionMap.put("processId", "#eq 4219");
		testSubscribeOneSubscription(subscriptionMap, 2, JSON_FILE_1);
	}

	private List<Map<String, String>> getSubscriptionMapsForTwoSubscriptions(String attributeTitle1,
			String constraint1, String attributeTitle2, String constraint2) {
		final Map<String, String> subscriptionMap1 = new HashMap<>();
		subscriptionMap1.put(attributeTitle1, constraint1);
		final Map<String, String> subscriptionMap2 = new HashMap<>();
		subscriptionMap2.put(attributeTitle2, constraint2);
		List<Map<String, String>> subscriptionMaps = new ArrayList<>();
		subscriptionMaps.add(subscriptionMap1);
		subscriptionMaps.add(subscriptionMap2);
		return subscriptionMaps;
	}

	@Test
	public void testTwoSubscriptionsOneMatched() throws InterruptedException, IOException {
		testSubscribe(
				getSubscriptionMapsForTwoSubscriptions("type", "#eq 'org.linux.cron.Started8'", "processId", "#eq 5000"),
				1, JSON_FILE_1);
	}

	@Test
	public void testTwoSubscriptionsNoneMatched() throws InterruptedException, IOException {
		testSubscribe(
				getSubscriptionMapsForTwoSubscriptions("type", "#eq 'org.linux.cron.Started20'", "processId",
						"#eq 5000"), 0, JSON_FILE_1);
	}

	@Test
	public void testTwoSubscriptionsOneMatched2() throws InterruptedException, IOException {
		testSubscribe(
				getSubscriptionMapsForTwoSubscriptions("type", "#pref 'org.linux.cron.Started5'", "processId",
						"#eq 5000"), 1, JSON_FILE_1);
	}

	@Test
	public void testTwoSubscriptionsTwoMatched() throws InterruptedException, IOException {
		testSubscribe(
				getSubscriptionMapsForTwoSubscriptions("type", "#pref 'org.linux.cron.Started1'", "processId",
						"#eq 5000"), 2, JSON_FILE_1);
	}

	@Test
	public void testTwoSubscriptionsAllMatched() throws InterruptedException, IOException {
		testSubscribe(
				getSubscriptionMapsForTwoSubscriptions("type", "#eq 'org.linux.cron.Started20'", "processId",
						"#eq 4219"), 10, JSON_FILE_1);
	}

	@Test
	public void testTwoSubscriptionsAllMatched2() throws InterruptedException, IOException {
		testSubscribe(
				getSubscriptionMapsForTwoSubscriptions("type", "#pref 'org.linux.cron.Started'", "processId",
						"#eq 4219"), 10, JSON_FILE_1);
	}

	@Test
	public void testSubscribeWithoutReady() throws IOException, InterruptedException {
		Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("processId", "#eq 4219");
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1, false, false);
	}

	@Test
	public void testSubscribeWithStop() throws IOException, InterruptedException {
		Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("processId", "#eq 4219");
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1, true, true);
	}

	@Test
	public void testApplicationCron() throws InterruptedException, IOException {
		testSubscribeOnePredicate("application", "eq", "'Cron'", 4, JSON_FILE_2);
	}

	@Test
	public void testApplicationCronLowerCase() throws InterruptedException, IOException {
		testSubscribeOnePredicate("application", "eq", "'cron'", 1, JSON_FILE_2);
	}

	@Test
	public void testApplicationCronPrefix() throws InterruptedException, IOException {
		testSubscribeOnePredicate("application", "pref", "'Cron'", 6, JSON_FILE_2);
	}

	@Test
	public void testApplicationCronPrefixLowerCase() throws InterruptedException, IOException {
		testSubscribeOnePredicate("application", "pref", "'cron'", 1, JSON_FILE_2);
	}

	@Test
	public void testApplicationBaPrefix() throws InterruptedException, IOException {
		testSubscribeOnePredicate("application", "pref", "'ba'", 2, JSON_FILE_2);
	}

	@Test
	public void testLevelLessThan5() throws InterruptedException, IOException {
		testSubscribeOnePredicate("level", "lt", "5", 4, JSON_FILE_2);
	}

	@Test
	public void testLevelLessThanOrEqualTo5() throws InterruptedException, IOException {
		testSubscribeOnePredicate("level", "le", "5", 10, JSON_FILE_2);
	}

	@Test
	public void testLevelLessThan4() throws InterruptedException, IOException {
		testSubscribeOnePredicate("level", "lt", "4", 4, JSON_FILE_2);
	}

	@Test
	public void testLevelGreaterThan6() throws InterruptedException, IOException {
		testSubscribeOnePredicate("level", "gt", "6", 0, JSON_FILE_2);
	}

	@Test
	public void testApplicationCronPrefixLevelLessThan4() throws InterruptedException, IOException {
		final Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("application", "#pref 'Cron'");
		subscriptionMap.put("level", "#lt 4");
		testSubscribeOneSubscription(subscriptionMap, 3, JSON_FILE_2);
	}
}
