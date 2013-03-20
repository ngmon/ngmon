package cz.muni.fi.xtovarn.heimdall.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cz.muni.fi.xtovarn.heimdall.client.ClientApi;
import cz.muni.fi.xtovarn.heimdall.client.ClientConnectionFactory;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Constraint;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Operator;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;

public class ClientTest {

	private static final String VALID_USER_PASSWORD = "password0";
	private static final String VALID_USER_NAME = "user0";
	private static final String INVALID_USER_NAME = "userFoo";
	private static final String INVALID_USER_PASSWORD = "passwordFoo";

	private static final int TIMEOUT_VALUE = 5;
	private static final TimeUnit TIMEOUT_TIME_UNIT = TimeUnit.MINUTES;

	private ClientApi client = null;

	@Before
	public void before() throws InterruptedException, ExecutionException, TimeoutException {
		client = ClientConnectionFactory.getClient(VALID_USER_NAME, VALID_USER_PASSWORD, TIMEOUT_VALUE,
				TIMEOUT_TIME_UNIT);
	}

	@After
	public void after() throws InterruptedException {
		client.stop();
	}

	@Test
	public void connect() throws InterruptedException, ExecutionException {
		assertTrue(client.isConnected());
	}

	@Test
	public void connectInvalidUser() throws InterruptedException, ExecutionException, TimeoutException {
		ClientApi client2 = ClientConnectionFactory.getClient(INVALID_USER_NAME, INVALID_USER_PASSWORD, TIMEOUT_VALUE,
				TIMEOUT_TIME_UNIT);
		assertNull(client2);
	}

	@Test
	public void connectInvalidPassword() throws InterruptedException, ExecutionException, TimeoutException {
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
	public void subcribe() throws InterruptedException, ExecutionException {
		connect();
		assertEquals(0, client.getSubscriptionIds().size());
		assertNotNull(client.subscribe(getPredicate()).get());
		assertTrue(client.wasLastSubscriptionSuccessful());
		assertEquals(1, client.getSubscriptionIds().size());
		assertNotNull(client.getLastSubscriptionId());
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

}
