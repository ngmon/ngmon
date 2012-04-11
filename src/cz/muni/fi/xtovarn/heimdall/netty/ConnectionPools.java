package cz.muni.fi.xtovarn.heimdall.netty;

public class ConnectionPools {
	private static final ConnectionPool pool = new ConnectionPool();

	public static ConnectionPool getPool() {
		return pool;
	}
}
