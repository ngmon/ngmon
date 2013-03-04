package cz.muni.fi.xtovarn.heimdall.connection;

public class ConnectionManagerSingleton {
	
	private static ConnectionManager connectionManager = null;

	public static ConnectionManager getConnectionManager() {
		if (connectionManager == null) {
			connectionManager = new ConnectionManager();
		}
		
		return connectionManager;
	}

}
