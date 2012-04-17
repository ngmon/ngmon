package cz.muni.fi.xtovarn.heimdall.db.store;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.json.JSONEventMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventStoreImpl implements EventStore {

	private final Database primaryDatabase;
	private final SecondaryDatabase eventTypeIndex;
	private final Sequence sequence;

	public EventStoreImpl(EventStoreIOLayer ebdb) {
		primaryDatabase = ebdb.getPrimaryDatabase();
		eventTypeIndex = ebdb.getEventTypeIndex();
		sequence = ebdb.getSequence();
	}

	public OperationStatus put(Event event) throws DatabaseException, IOException {
		DatabaseEntry key = new DatabaseEntry();

		/* Populate key */
		Long id = sequence.get(null, 1); // Get unique long key from sequence
		LongBinding.longToEntry(id, key); // Bind key to DatabaseEntry

		/* Parse and populate Event fields */
		event.setId(id);

		/* Populate data */
		DatabaseEntry data = new DatabaseEntry(JSONEventMapper.eventAsBytes(event));

		return primaryDatabase.put(null, key, data);
	}

	public List<Event> getAllRecords() throws DatabaseException, IOException {
		Cursor cursor = primaryDatabase.openCursor(null, null);
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
		primaryDatabase.get(null, key, data, LockMode.DEFAULT);

		return JSONEventMapper.bytesToEvent(data.getData());
	}
}
