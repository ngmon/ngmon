package cz.muni.fi.xtovarn.heimdall.client.test;

import static org.junit.Assert.*;

import org.junit.Test;

import cz.muni.fi.xtovarn.heimdall.client.ClientApi;

public class ClientTest {

	private static final int WAIT_TIME = 1000;
	private static final String VALID_USER_PASSWORD = "password0";
	private static final String VALID_USER_NAME = "user0";
	private static final String INVALID_USER_NAME = "userFoo";
	private static final String INVALID_USER_PASSWORD = "passwordFoo";

	@Test
	public void connect() throws InterruptedException {
		ClientApi client = new ClientApi();
		client.connect(VALID_USER_NAME, VALID_USER_PASSWORD);
		Thread.sleep(WAIT_TIME);
		assertTrue(client.isConnected());
	}

	@Test
	public void connectInvalidUser() throws InterruptedException {
		ClientApi client = new ClientApi();
		client.connect(INVALID_USER_NAME, VALID_USER_PASSWORD);
		Thread.sleep(WAIT_TIME);
		assertFalse(client.isConnected());
	}

	@Test
	public void connectInvalidPassword() throws InterruptedException {
		ClientApi client = new ClientApi();
		client.connect(INVALID_USER_NAME, INVALID_USER_PASSWORD);
		Thread.sleep(WAIT_TIME);
		assertFalse(client.isConnected());
	}

}
