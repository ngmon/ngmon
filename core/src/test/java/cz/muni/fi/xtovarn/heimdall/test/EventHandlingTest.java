package cz.muni.fi.xtovarn.heimdall.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.muni.fi.xtovarn.heimdall.commons.util.test.TestUtil;
import org.jboss.netty.channel.MessageEvent;
import org.junit.*;

import com.sleepycat.je.DatabaseException;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.commons.util.test.TestSensor;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.test.util.NgmonLauncher;
import cz.muni.fi.xtovarn.heimdall.test.util.ObjectMapperWrapper;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient;
import cz.muni.fi.xtovarn.heimdall.test.util.TestMessageHandler;
import cz.muni.fi.xtovarn.heimdall.test.util.TestResponseHandlers;
import org.junit.rules.TemporaryFolder;

/**
 * Tests sensor events forwarding (sensor -> server -> clients)
 */
public class EventHandlingTest {

	@Rule
	public static final TemporaryFolder JUNIT_TEMPORARY_DIRECTORY = new TemporaryFolder();
	private static final File BASE_DIRECTORY = JUNIT_TEMPORARY_DIRECTORY.newFolder("junit_testdatabase1");

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

	/**
	 * Launches Ngmon server, initializes the test client
	 */
	@Before
	public void setUp() throws DatabaseException, IOException, InterruptedException {
		this.ngmon = new NgmonLauncher(BASE_DIRECTORY);
		this.ngmon.start();

		this.testClient = new TestClient();
	}

	@After
	public void tearDown() {
		this.testClient.stop();
		this.ngmon.stop();
	}

	@AfterClass
	public static void tearDownClass() {
		TestUtil.recursiveDelete(BASE_DIRECTORY);
	}

	/**
	 * Autenticates against the Ngmon server
	 */
	private void connect() {
		testClient.addMessage(
				new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytesNoExceptions(new User(VALID_USER_LOGIN,
						VALID_USER_PASSCODE))), CONNECTION_ID_KEY, TestResponseHandlers.CONNECT_RESPONSE_HANDLER);
	}

	/**
	 * Sends READY
	 */
	private void ready() {
		testClient.addMessage(new SimpleMessage(Directive.READY, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}

	/**
	 * Sends STOP
	 */
	private void stop() {
		testClient.addMessage(new SimpleMessage(Directive.STOP, "".getBytes()), null,
				TestResponseHandlers.ACK_RESPONSE_HANDLER);
	}

	/**
	 * Subscribes to sensor events using the specified Predicates (each
	 * subscription map is one predicate), then sends events using the test
	 * sensors and checks whether the number of sensor events the client
	 * received is what we expected; Also returns all the events the client
	 * received
	 * 
	 * @param subscriptionMaps
	 *            Collection of predicates, each item is a map from attribute
	 *            names to strings specifying the operator and value (so each
	 *            item is a constraint)
	 * @param expectedEventCount
	 *            How many sensor events the client expects to receive
	 * @param jsonFileName
	 *            The name of the file with the sensor events (in JSON)
	 * @param includeReady
	 *            Whether to send READY after all the SUBSCRIBE messages (if
	 *            not, then the client will receive no sensor events)
	 * @param includeStop
	 *            Whether to send STOP after all the SUBSCRIBE messages (and the
	 *            optional READY) (if yes, then the client will receive no
	 *            sensor events)
	 * @return The sensor events the client received
	 */
	private List<MessageEvent> testSubscribe(Collection<Map<String, String>> subscriptionMaps, int expectedEventCount,
			String jsonFileName, boolean includeReady, boolean includeStop) throws InterruptedException, IOException {
		connect();

		int i = 1;
		// each subscription map is one predicate (collection of attribute
		// name and operator with value pairs)
		for (Map<String, String> subscriptionMap : subscriptionMaps) {
			testClient.addMessage(
					new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytesNoExceptions(subscriptionMap)),
					SUBSCRIPTION_ID_KEY_PREFIX + i++, TestResponseHandlers.SUBSCRIBE_RESPONSE_HANDLER, true);
		}

		if (includeReady)
			ready();

		if (includeStop)
			stop();

		// set message handler to keep track of the received sensor events
		TestMessageHandler messageHandler = new TestMessageHandler();
		testClient.addUnsolicitedMessageHandler(messageHandler);

		testClient.run(false);

		// send the test events using a simple sensor
		TestSensor sensor = new TestSensor();
		BufferedReader br = new BufferedReader(new InputStreamReader(EventHandlingTest.class.getResourceAsStream(jsonFileName)));
		String line;
		while ((line = br.readLine()) != null) {
			sensor.sendString(line);
		}
		br.close();
		sensor.close();

		// wait a little while for all the events to be processed (forwarded);
		// This makes the method not deterministic, but it would be quite
		// difficult to tell whether all the sensor events reached the server
		// and then whether all the events were processed and either received
		// (and processed) by the client or not intended for the client at all
		Thread.sleep(TestClient.EVENT_TIMEOUT_IN_MILLIS);

		// check the number of received events
		assertEquals((!includeReady || includeStop) ? 0 : expectedEventCount, messageHandler.getMessageCount());

		return messageHandler.getMessageEventList();
	}

	/**
	 * A helper method with default values for includeReady and includeStop
	 * (because there is usually no need for them to have other value)
	 */
	private List<MessageEvent> testSubscribe(Collection<Map<String, String>> subscriptionMaps, int expectedEventCount,
			String jsonFileName) throws InterruptedException, IOException {
		return testSubscribe(subscriptionMaps, expectedEventCount, jsonFileName, INCLUDE_READY_DEFAULT,
				INCLUDE_STOP_DEFAULT);
	}

	/**
	 * A helper method with only one "subscription map" (predicate)
	 */
	private List<MessageEvent> testSubscribeOneSubscription(Map<String, String> subscriptionMap,
			int expectedEventCount, String jsonFileName, boolean includeReady, boolean includeStop)
			throws InterruptedException, IOException {
		List<Map<String, String>> subscriptionMaps = new ArrayList<>();
		subscriptionMaps.add(subscriptionMap);
		return testSubscribe(subscriptionMaps, expectedEventCount, jsonFileName, includeReady, includeStop);
	}

	/**
	 * A helper method with only one subscription map and default values for
	 * includeReady and includeStop
	 */
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
		// with the constraint processId <= 5000 we should get all 10 events in
		// the JSON_FILE_1 file
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

	/**
	 * Checks the sensor event content too
	 */
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
		// 0 events matching both constraints
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

	/**
	 * Returns two predicates, each with exactly one constraint
	 */
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
		// 1 event satisfied by at least one of the two predicates
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
		// 0 received events, since ready was not called
		testSubscribeOneSubscription(subscriptionMap, 0, JSON_FILE_1, false, false);
	}

	@Test
	public void testSubscribeWithStop() throws IOException, InterruptedException {
		Map<String, String> subscriptionMap = new HashMap<>();
		subscriptionMap.put("processId", "#eq 4219");
		// 0 received events, since stop was called after ready
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
