package cz.muni.fi.xtovarn.heimdall.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientConnectionFactory {

	public static class ConnectionException extends Exception {

		public ConnectionException() {
			super();
		}

		public ConnectionException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
			super(arg0, arg1, arg2, arg3);
		}

		public ConnectionException(String arg0, Throwable arg1) {
			super(arg0, arg1);
		}

		public ConnectionException(String arg0) {
			super(arg0);
		}

		public ConnectionException(Throwable arg0) {
			super(arg0);
		}

	}

	public static ClientApi getClient(String login, String passcode, long timeout, TimeUnit unit)
			throws ConnectionException {
		Client client;
		// wait for the server to send CONNECTED (or ERROR)
		boolean connected;
		try {
			client = new Client(timeout, unit);
			// wait for the channel to connect (waiting in the constructor is not
			// enough)
			client.getChannelConnectedResult().get();
			connected = client.connect(login, passcode).get(timeout, unit);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new ConnectionException(e);
		}

		return connected ? client : null;
	}

}
