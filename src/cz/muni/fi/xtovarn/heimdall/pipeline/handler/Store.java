package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;

import java.io.IOException;

public class Store implements Handler {

	private final EventStore store;

	public Store(EventStore store) {
		this.store = store;
	}

	@Override
	public Object handle(Object o) {
		try {
			OperationStatus status = store.put((Event) o);
		} catch (DatabaseException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		return o;
	}
}
