package cz.muni.fi.xtovarn.heimdall.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientConnectionFactory {

	public static ClientApi getClient(String login, String passcode, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		Client client = new Client(timeout, unit);
		// wait for the channel to connect (waiting in the constructor is not enough)
		client.getChannelConnectedResult().get();
		// wait for the server to send CONNECTED (or ERROR)
		boolean connected = client.connect(login, passcode).get(timeout, unit);
		
		return connected ? client : null;
	}

}
