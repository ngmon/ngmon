package cz.muni.fi.xtovarn.heimdall.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cz.muni.fi.xtovarn.heimdall.client.ClientApi;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Constraint;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Operator;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;

public class ClientTest {

	private static final String VALID_USER_PASSWORD = "password0";
	private static final String VALID_USER_NAME = "user0";
	private static final String INVALID_USER_NAME = "userFoo";
	private static final String INVALID_USER_PASSWORD = "passwordFoo";

	private ClientApi client = null;

	@Before
	public void before() throws InterruptedException, ExecutionException {
		client = new ClientApi();
	}

	@After
	public void after() throws InterruptedException {
		client.stop();
	}

	@Test
	public void connect() throws InterruptedException, ExecutionException {
		Future<Boolean> result = client.connect(VALID_USER_NAME, VALID_USER_PASSWORD);
		assertTrue(result.get());
		assertTrue(client.isConnected());
	}

	@Test
	public void connectInvalidUser() throws InterruptedException, ExecutionException {
		assertFalse(client.connect(INVALID_USER_NAME, VALID_USER_PASSWORD).get());
		assertFalse(client.isConnected());
	}

	@Test
	public void connectInvalidPassword() throws InterruptedException, ExecutionException {
		assertFalse(client.connect(INVALID_USER_NAME, INVALID_USER_PASSWORD).get());
		assertFalse(client.isConnected());
	}

	private Predicate getPredicate() {
		Predicate predicate = new Predicate();
		predicate.addConstraint(new Constraint("attribute1", Operator.LESS_THAN, "5"));
		predicate.addConstraint(new Constraint("attribute2", Operator.EQUALS, "abcd"));
		return predicate;
	}

	@Test
	public void subscribe() throws InterruptedException, ExecutionException {
		connect();
		assertEquals(0, client.getSubscriptionIds().size());
		assertNotNull(client.subscribe(getPredicate()).get());
		assertTrue(client.wasLastSubscriptionSuccessful());
		assertEquals(1, client.getSubscriptionIds().size());
		assertNotNull(client.getLastSubscriptionId());
	}

	@Test
	public void subscribeWithoutConnect() throws InterruptedException, ExecutionException {
		try {
			assertNull(client.subscribe(getPredicate()));
			fail();
		} catch (IllegalStateException e) {
			assertFalse(client.wasLastSubscriptionSuccessful());
			assertEquals(0, client.getSubscriptionIds().size());
			assertNull(client.getLastSubscriptionId());
		}
	}

}
