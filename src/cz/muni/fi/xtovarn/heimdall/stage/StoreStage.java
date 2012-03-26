package cz.muni.fi.xtovarn.heimdall.stage;


import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class StoreStage extends AbstractStage<Event, Event> implements Stage<Event, Event> {

	private final EventStore store;

	public StoreStage(BlockingQueue<Event> inWorkQueue, BlockingQueue<Event> outWorkQueue, EventStore store) {
		super(inWorkQueue, outWorkQueue);
		this.store = store;
	}

	@Override
	public Event work(Event workItem) {
		try {
			store.put(workItem);
		} catch (DatabaseException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		return workItem;
	}

}
