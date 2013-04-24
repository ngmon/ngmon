package cz.muni.fi.xtovarn.heimdall.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sleepycat.je.DatabaseException;

import cz.muni.fi.xtovarn.heimdall.client.Client;
import cz.muni.fi.xtovarn.heimdall.client.ClientApi;
import cz.muni.fi.xtovarn.heimdall.client.ClientConnectionFactory;
import cz.muni.fi.xtovarn.heimdall.client.ClientConnectionFactory.ConnectionException;
import cz.muni.fi.xtovarn.heimdall.client.EventReceivedHandler;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Constraint;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Operator;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;
import cz.muni.fi.xtovarn.heimdall.client.test.util.NgmonLauncher;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.util.test.TestSensor;

/**
 * Various client tests
 */
public class ClientTest {

	/**
	 * Simple EventReceivedHandler which counts and saves received sensor events
	 */
	private static class TestEventHandler implements EventReceivedHandler {
		private AtomicInteger count = new AtomicInteger(0);
		private List<Event> events = new ArrayList<>();

		@Override
		public void handleEvent(Event event) {
			count.incrementAndGet();
			addEventToList(event);
		}

		private synchronized void addEventToList(Event event) {
			events.add(event);
		}

		public AtomicInteger getCount() {
			return count;
		}

		public List<Event> getEvents() {
			return events;
		}

	}

	private static final String VALID_USER_PASSWORD = "password0";
	private static final String VALID_USER_NAME = "user0";
	private static final String INVALID_USER_NAME = "userFoo";
	private static final String INVALID_USER_PASSWORD = "passwordFoo";
	private static final String JSON_FILE_NAME = "events2.jsons";

	/**
	 * How long to wait for server response
	 */
	private static final int TIMEOUT_VALUE = 5;
	private static final TimeUnit TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;
	/**
	 * The amount of time to wait for all sensor events
	 */
	private static final long EVENT_TIMEOUT_IN_MILLIS = 1000;

	private TestEventHandler testEventHandler = new TestEventHandler();

	private NgmonLauncher ngmon = null;

	private Client client = null;

	/**
	 * Starts Ngmon server and connects to it
	 */
	@Before
	public void setUp() throws ConnectionException, DatabaseException, IOException, InterruptedException {
		this.ngmon = new NgmonLauncher();
		this.ngmon.start();

		client = (Client) ClientConnectionFactory.getClient(VALID_USER_NAME, VALID_USER_PASSWORD, TIMEOUT_VALUE,
				TIMEOUT_TIME_UNIT);
		assertNotNull(client);
		assertTrue(client.isConnected());
	}

	@After
	public void tearDown() throws InterruptedException {
		client.stop();

		this.ngmon.stop();
	}

	/**
	 * Empty since the actual test is done in setUp
	 */
	@Test
	public void connect() throws InterruptedException, ExecutionException {
	}

	@Test
	public void connectInvalidUser() throws ConnectionException {
		ClientApi client2 = ClientConnectionFactory.getClient(INVALID_USER_NAME, INVALID_USER_PASSWORD, TIMEOUT_VALUE,
				TIMEOUT_TIME_UNIT);
		assertNull(client2);
	}

	@Test
	public void connectInvalidPassword() throws ConnectionException {
		ClientApi client2 = ClientConnectionFactory.getClient(INVALID_USER_NAME, INVALID_USER_PASSWORD, TIMEOUT_VALUE,
				TIMEOUT_TIME_UNIT);
		assertNull(client2);
	}

	private Predicate getPredicate() {
		Predicate predicate = new Predicate();
		predicate.addConstraint(new Constraint("attribute1", Operator.LESS_THAN, "5"));
		predicate.addConstraint(new Constraint("attribute2", Operator.EQUALS, "abcd"));
		return predicate;
	}

	@Test
	public void subscribe() throws InterruptedException, ExecutionException {
		assertEquals(0, client.getSubscriptionIds().size());
		assertNotNull(client.subscribe(getPredicate()).get());
		assertTrue(client.wasLastSubscriptionSuccessful());
		assertEquals(1, client.getSubscriptionIds().size());
		assertNotNull(client.getLastSubscriptionId());
	}

	@Test
	public void unsubscribe() throws InterruptedException, ExecutionException {
		subscribe();
		assertTrue(client.unsubscribe(client.getLastSubscriptionId()).get());
	}

	@Test
	public void unsubscribeInvalidId() throws InterruptedException, ExecutionException {
		subscribe();
		assertFalse(client.unsubscribe(client.getLastSubscriptionId() + 1).get());
	}

	// can't test this, since I need to get the client through the connection
	// factory
	/*-@Test
	public void subscribeWithoutConnect() throws InterruptedException, ExecutionException {
		try {
			client.subscribe(getPredicate());
			fail();
		} catch (IllegalStateException e) {
			assertFalse(client.wasLastSubscriptionSuccessful());
			assertEquals(0, client.getSubscriptionIds().size());
			assertNull(client.getLastSubscriptionId());
		}
	}*/

	@Test
	public void readyWithNoSubscriptions() throws InterruptedException, ExecutionException {
		assertTrue(client.ready().get());
	}

	@Test
	public void readyWithOneSubscription() throws InterruptedException, ExecutionException {
		subscribe();
		assertTrue(client.ready().get());
	}

	@Test(expected = IllegalStateException.class)
	public void stopWithoutReady() throws InterruptedException, ExecutionException {
		assertFalse(client.stopSending().get());
	}

	@Test
	public void stop() throws InterruptedException, ExecutionException {
		readyWithOneSubscription();
		assertTrue(client.stopSending().get());
	}

	@Test(expected = IllegalStateException.class)
	public void readyAfterReady() throws InterruptedException, ExecutionException {
		readyWithNoSubscriptions();
		client.ready();
	}

	@Test(expected = IllegalStateException.class)
	public void stopAfterStop() throws InterruptedException, ExecutionException {
		stop();
		client.stopSending();
	}

	/**
	 * Sends the events from the specified file (JSON format)
	 */
	private void sendEvents(String jsonFileName) throws IOException, InterruptedException {
		TestSensor sensor = new TestSensor();
		BufferedReader br = new BufferedReader(new FileReader("src/main/resources/" + jsonFileName));
		String line;
		while ((line = br.readLine()) != null) {
			sensor.sendString(line);
		}
		br.close();
		sensor.close();

		Thread.sleep(EVENT_TIMEOUT_IN_MILLIS);
	}

	/**
	 * Subscribes to sensor events using the specified Predicate, then sends the
	 * events using a test sensor, and finally checks the number of received
	 * events is what we expected
	 */
	private List<Event> testMessageReceived(Predicate predicate, int expectedEventCount) throws InterruptedException,
			ExecutionException, IOException {
		Long subscriptionId = client.subscribe(predicate).get();
		client.setEventReceivedHandler(testEventHandler);
		assertTrue(client.ready().get());

		sendEvents(JSON_FILE_NAME);

		assertTrue(client.stopSending().get());
		client.unsubscribe(subscriptionId);

		assertEquals(expectedEventCount, testEventHandler.getCount().intValue());

		return testEventHandler.getEvents();
	}

	private List<Event> testMessageReceivedOneConstraint(Constraint constraint, int expectedEventCount)
			throws InterruptedException, ExecutionException, IOException {
		Predicate predicate = new Predicate();
		predicate.addConstraint(constraint);
		return testMessageReceived(predicate, expectedEventCount);
	}

	/**
	 * Checks the content of the forwarded event is correct too
	 */
	@Test
	public void testTypeEqualsStarted5() throws InterruptedException, ExecutionException, IOException {
		List<Event> events = testMessageReceivedOneConstraint(new Constraint("type", Operator.EQUALS,
				"org.linux.cron.Started5"), 1);
		assertEquals(1, events.size());
		Event event = events.get(0);
		assertEquals("bar", event.getApplication());
		assertEquals("domain.localhost.cz", event.getHostname());
		assertEquals(5, event.getLevel());
		assertEquals(4, event.getPriority());
		assertEquals("cron", event.getProcess());
		assertEquals(4219, event.getProcessId());
		assertEquals("org.linux.cron.Started5", event.getType());
	}

	@Test
	public void testProcessIdLessThan5000() throws InterruptedException, ExecutionException, IOException {
		testMessageReceivedOneConstraint(new Constraint("processId", Operator.LESS_THAN, "5000"), 10);
	}

	@Test
	public void testTypePrefix() throws InterruptedException, ExecutionException, IOException {
		testMessageReceivedOneConstraint(new Constraint("type", Operator.PREFIX, "org.linux.cron.Started"), 10);
	}

	@Test
	public void testLevelLessThan5() throws InterruptedException, ExecutionException, IOException {
		testMessageReceivedOneConstraint(new Constraint("level", Operator.LESS_THAN, "5"), 4);
	}

	@Test
	public void testApplicationCronPrefixLevelLessThan4() throws InterruptedException, ExecutionException, IOException {
		Predicate predicate = new Predicate();
		predicate.addConstraint(new Constraint("application", Operator.PREFIX, "Cron"));
		predicate.addConstraint(new Constraint("level", Operator.LESS_THAN, "4"));
		testMessageReceived(predicate, 3);
	}

}
