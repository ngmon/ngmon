package cz.muni.fi.xtovarn.heimdall.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import cz.muni.fi.xtovarn.heimdall.client.ClientConnectionFactory.ConnectionException;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Constraint;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Operator;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;

public class ClientTest {

	private static final String VALID_USER_PASSWORD = "password0";
	private static final String VALID_USER_NAME = "user0";
	private static final String INVALID_USER_NAME = "userFoo";
	private static final String INVALID_USER_PASSWORD = "passwordFoo";

	private static final int TIMEOUT_VALUE = 5;
	private static final TimeUnit TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

	private ClientApi client = null;

	@Before
	public void before() throws ConnectionException {
		client = ClientConnectionFactory.getClient(VALID_USER_NAME, VALID_USER_PASSWORD, TIMEOUT_VALUE,
				TIMEOUT_TIME_UNIT);
		assertNotNull(client);
		assertTrue(client.isConnected());
	}

	@After
	public void after() throws InterruptedException {
		client.stop();
	}

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

}
