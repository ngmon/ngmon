package cz.muni.fi.xtovarn.heimdall.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cz.muni.fi.xtovarn.heimdall.client.ClientApi;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Constraint;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Operator;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;

public class ClientTest {

	private static final int WAIT_TIME = 1000;
	private static final String VALID_USER_PASSWORD = "password0";
	private static final String VALID_USER_NAME = "user0";
	private static final String INVALID_USER_NAME = "userFoo";
	private static final String INVALID_USER_PASSWORD = "passwordFoo";

	private ClientApi client = null;

	@Before
	public void before() throws InterruptedException {
		client = new ClientApi();
		Thread.sleep(WAIT_TIME);
	}

	@After
	public void after() throws InterruptedException {
		client.stop();
		client = null;
		Thread.sleep(WAIT_TIME);
	}

	private void connectClient() throws InterruptedException {
		client.connect(VALID_USER_NAME, VALID_USER_PASSWORD);
		Thread.sleep(WAIT_TIME);
		assertTrue(client.isConnected());
	}

	@Test
	public void connect() throws InterruptedException {
		client.connect(VALID_USER_NAME, VALID_USER_PASSWORD);
		Thread.sleep(WAIT_TIME);
		assertTrue(client.isConnected());
	}

	@Test
	public void connectInvalidUser() throws InterruptedException {
		client.connect(INVALID_USER_NAME, VALID_USER_PASSWORD);
		Thread.sleep(WAIT_TIME);
		assertFalse(client.isConnected());
	}

	@Test
	public void connectInvalidPassword() throws InterruptedException {
		client.connect(INVALID_USER_NAME, INVALID_USER_PASSWORD);
		Thread.sleep(WAIT_TIME);
		assertFalse(client.isConnected());
	}

	private Predicate getPredicate() {
		Predicate predicate = new Predicate();
		predicate.addConstraint(new Constraint("attribute1", Operator.LESS_THAN, "5"));
		predicate.addConstraint(new Constraint("attribute2", Operator.EQUALS, "abcd"));
		return predicate;
	}

	@Test
	public void subscribe() throws InterruptedException {
		connectClient();
		assertEquals(0, client.getSubscriptionIds().size());
		client.subscribe(getPredicate());
		Thread.sleep(WAIT_TIME);
		assertTrue(client.wasLastSubscriptionSuccessful());
		assertEquals(1, client.getSubscriptionIds().size());
		assertNotNull(client.getLastSubscriptionId());
	}

	@Test
	public void subscribeWithoutConnect() throws InterruptedException {
		try {
			client.subscribe(getPredicate());
			fail();
		} catch (IllegalStateException e) {
			Thread.sleep(WAIT_TIME);
			assertFalse(client.wasLastSubscriptionSuccessful());
			assertEquals(0, client.getSubscriptionIds().size());
			assertNull(client.getLastSubscriptionId());
		}
	}

}
