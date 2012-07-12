package cz.muni.fi.xtovarn.heimdall.db.store;

import com.google.inject.Inject;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.json.JSONEventMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventStoreImpl implements EventStore {

	private final EventStoreIOLayer ebdb;

	@Inject
	public EventStoreImpl(EventStoreIOLayer ebdb) {
		this.ebdb = ebdb;
	}

	public Database getPrimaryDatabase() {
		return ebdb.getPrimaryDatabase();
	}

	public SecondaryDatabase getEventTypeIndex() {
		return ebdb.getEventTypeIndex();
	}

	public Sequence getSequence() {
		return ebdb.getSequence();
	}

	public synchronized OperationStatus put(Event event) throws DatabaseException, IOException {
		DatabaseEntry key = new DatabaseEntry();

		/* Populate key */
		Long id = getSequence().get(null, 1); // Get unique long key from sequence
		LongBinding.longToEntry(id, key); // Bind key to DatabaseEntry

		/* Parse and populate Event fields */
		event.setId(id);

		/* Populate data */
		DatabaseEntry data = new DatabaseEntry(JSONEventMapper.eventAsBytes(event));

		return getPrimaryDatabase().put(null, key, data);
	}

	public List<Event> getAllRecords() throws DatabaseException, IOException {
		Cursor cursor = getPrimaryDatabase().openCursor(null, null);
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		List<Event> list = new ArrayList<Event>();

		OperationStatus retVal = cursor.getFirst(key, data, LockMode.DEFAULT);

		while (retVal == OperationStatus.SUCCESS) {
			Event event = JSONEventMapper.bytesToEvent(data.getData());
			list.add(event);
			retVal = cursor.getNext(key, data, LockMode.DEFAULT);
		}

		cursor.close();

		return list;
	}

	public Event getEventById(Long id) throws DatabaseException, IOException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		LongBinding.longToEntry(id, key);
		getPrimaryDatabase().get(null, key, data, LockMode.DEFAULT);

		return JSONEventMapper.bytesToEvent(data.getData());
	}
}
