package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import com.sleepycat.je.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.storage.EventStore;

import java.io.IOException;

public class Store implements Handler {

	private final EventStore store;

	public Store(EventStore store) {
		this.store = store;
	}

	@Override
	public Object handle(Object o) {
		try {
			store.put((Event) o);
		} catch (DatabaseException e) {
			System.err.println("DatabaseException " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Exception " + e.getMessage());
			e.printStackTrace();
		}

		return o;
	}
}
