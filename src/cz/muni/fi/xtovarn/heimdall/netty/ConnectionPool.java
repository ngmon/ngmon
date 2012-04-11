package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
	private Map<String, Connection> connections = new HashMap<String, Connection>(3);

	public void add(Connection connection) {
		connections.put(connection.getId(), connection);
	}

	public void remove(String id) {
		connections.remove(id);
	}

	public void send(String id, Event event) {
		Connection connection = connections.get(id);

		if (connection != null) {
			connection.send(event);
		}
	}
}
