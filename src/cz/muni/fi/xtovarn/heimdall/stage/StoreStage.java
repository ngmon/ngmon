package cz.muni.fi.xtovarn.heimdall.stage;


import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.util.JSONStringParser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class StoreStage extends AbstractStage<String, Event> implements Stage<String, Event> {

	private final EventStore store;

	public StoreStage(BlockingQueue<String> inWorkQueue, BlockingQueue<Event> outWorkQueue, EventStore store) {
		super(inWorkQueue, outWorkQueue);
		this.store = store;
	}

	@Override
	public Event work(String json) {
		Event event = null;

		try {
			event = JSONStringParser.stringToEvent(json);
		} catch (JsonParseException e) {
			System.err.println(e.getMessage());
		} catch (JsonMappingException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		if (event != null) {
			event.setDetectionTime(new Date(System.currentTimeMillis()));
		}

		try {
			store.put(event);
		} catch (DatabaseException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		return event;
	}

}
