package cz.muni.fi.xtovarn.heimdall.db.store;

import com.sleepycat.db.DatabaseException;

import java.io.FileNotFoundException;

public class EventStoreFactory {

	private static EventStoreIOLayer instance = null;

	public static EventStoreIOLayer getSingleInstance() throws FileNotFoundException, DatabaseException {
		if (instance == null) {
			instance = new EventStoreIOLayer();
		}
		return instance;
	}
}
