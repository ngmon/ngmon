package cz.muni.fi.xtovarn.heimdall.store;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.keycreator.event.TypeKeyCreator;
import cz.muni.fi.xtovarn.heimdall.util.JSONEventMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventStore {
	private static Environment environment = null;
	private static Database eventStore = null;
	private static SecondaryDatabase eventTypeIndex = null;
	private static Database sequenceDatabase = null;
	private static Sequence sequence = null;

	public void setup() throws FileNotFoundException, DatabaseException {

		/* Setup environment root path */
		File homeDirectory = new File("./database/evsbdb");

		/* Setup environment */
		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		environmentConfig.setInitializeCache(true);
		environmentConfig.setThreaded(true);
		environment = new Environment(homeDirectory, environmentConfig);

		/* Setup primary eventStore */
		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);
		databaseConfig.setType(DatabaseType.BTREE);
		eventStore = environment.openDatabase(null, "event_store.db", null, databaseConfig);

		/* Setup Event.type secondary eventStore */
		SecondaryConfig eventTypeSecondaryConfig = new SecondaryConfig();
		eventTypeSecondaryConfig.setAllowCreate(true);
		eventTypeSecondaryConfig.setType(DatabaseType.BTREE);
		eventTypeSecondaryConfig.setSortedDuplicates(true);
		TypeKeyCreator typeKeyCreator = new TypeKeyCreator(); // Event.type TypeKeyCreator
		eventTypeSecondaryConfig.setKeyCreator(typeKeyCreator);
		eventTypeIndex = environment.openSecondaryDatabase(null, "event_type_index.db", null, eventStore, eventTypeSecondaryConfig);

		/* Setup sequence eventStore */
		DatabaseConfig sequenceDatabaseConfig = new DatabaseConfig();
		sequenceDatabaseConfig.setAllowCreate(true);
		sequenceDatabaseConfig.setType(DatabaseType.HASH);
		sequenceDatabase = environment.openDatabase(null, "event_sequence.db", null, sequenceDatabaseConfig);

		/* Setup sequence handler for Event.id */
		SequenceConfig sequenceConfig = new SequenceConfig();
		sequenceConfig.setInitialValue(0);
		sequenceConfig.setAllowCreate(true);
		sequence = sequenceDatabase.openSequence(null, new DatabaseEntry("id".getBytes()), sequenceConfig);
	}

	public OperationStatus put(Event event) throws DatabaseException, IOException {
		DatabaseEntry key = new DatabaseEntry();

		/* Populate key */
		Long id = sequence.get(null, 1); // Get unique long key from sequence
		LongBinding.longToEntry(id, key); // Bind key to DatabaseEntry

		/* Parse and populate Event fields */
		event.setId(id);

		/* Populate data */
		DatabaseEntry data = JSONEventMapper.eventToEntry(event);

		return eventStore.put(null, key, data);
	}

	public List<Event> getAllRecords() throws DatabaseException, IOException {
		Cursor cursor = eventStore.openCursor(null, null);
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();

		List<Event> list = new ArrayList<Event>();

		OperationStatus retVal = cursor.getFirst(key, data, LockMode.DEFAULT);

		while (retVal == OperationStatus.SUCCESS) {
			Event event = JSONEventMapper.entryToEvent(data);
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
		eventStore.get(null, key, data, LockMode.DEFAULT);

		Event event = JSONEventMapper.entryToEvent(data);

		return event;
	}

	public void close() throws DatabaseException {

		if (sequence != null) {
			sequence.close();
		}

		if (sequenceDatabase != null) {
			sequenceDatabase.close();
		}

		if (eventTypeIndex != null) {
			eventTypeIndex.close();
		}

		if (eventStore != null) {
			eventStore.close();
		}

		if (environment != null) {
			environment.close();
		}
	}

}
