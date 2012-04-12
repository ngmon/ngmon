package cz.muni.fi.xtovarn.heimdall.pipeline.handlers;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.Handler;

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
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		return o;
	}
}
