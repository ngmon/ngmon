package cz.muni.fi.xtovarn.heimdall.db.store;

import com.sleepycat.db.DatabaseException;

import java.io.FileNotFoundException;

public class EventStoreFactory {

	private static EventStore instance = null;

	public static EventStore getSingleInstance() throws FileNotFoundException, DatabaseException {
		if (instance == null) {
			instance = new EventStore();
		}
		return instance;
	}
}
