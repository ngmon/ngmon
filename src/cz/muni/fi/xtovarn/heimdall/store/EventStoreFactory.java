package cz.muni.fi.xtovarn.heimdall.store;

import com.sleepycat.db.DatabaseException;

import java.io.FileNotFoundException;

public class EventStoreFactory {

	private static EventStore instance = null;

	public static EventStore getInstance() throws FileNotFoundException, DatabaseException {
		if (instance == null) {
			instance = new EventStore();
		}
		return instance;
	}
}