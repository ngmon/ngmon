package cz.muni.fi.xtovarn.heimdall.processor;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;

import java.io.IOException;

public class Store extends AbstractAction {

	private final EventStore store;

	public Store(AbstractAction successor, EventStore store) {
		super(successor);
		this.store = store;
	}

	public Store(EventStore store) {
		this.store = store;
	}

	@Override
	protected void processEvent(Event event) {
		try {
			store.put(event);
		} catch (DatabaseException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
