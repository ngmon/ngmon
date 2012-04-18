package cz.muni.fi.xtovarn.heimdalld.db.store;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdalld.db.event.keycreator.TypeKeyCreator;
import org.picocontainer.Startable;

import java.io.File;
import java.io.FileNotFoundException;

public class EventStoreIOLayer implements Startable {
	private static Environment environment = null;
	private static Database primaryDatabase = null;
	private static SecondaryDatabase eventTypeIndex = null;
	private static Database sequenceDatabase = null;
	private static Sequence sequence = null;

	private static final String BASE_DIRECTORY = "./database/events";

	public void start() {
		try {
			/* Setup environment root path */
			File baseDirectory = new File(BASE_DIRECTORY);

			/* Setup environment */
			EnvironmentConfig environmentConfig = new EnvironmentConfig();
			environmentConfig.setAllowCreate(true);
			environmentConfig.setInitializeCache(true);
			environmentConfig.setThreaded(true);
			environment = new Environment(baseDirectory, environmentConfig);

			/* Setup primary eventStore */
			DatabaseConfig databaseConfig = new DatabaseConfig();
			databaseConfig.setAllowCreate(true);
			databaseConfig.setType(DatabaseType.BTREE);
			primaryDatabase = environment.openDatabase(null, "event_store.db", null, databaseConfig);

			/* Setup Event.type secondary eventStore */
			SecondaryConfig eventTypeSecondaryConfig = new SecondaryConfig();
			eventTypeSecondaryConfig.setAllowCreate(true);
			eventTypeSecondaryConfig.setType(DatabaseType.BTREE);
			eventTypeSecondaryConfig.setSortedDuplicates(true);
			TypeKeyCreator typeKeyCreator = new TypeKeyCreator(); // Event.type TypeKeyCreator
			eventTypeSecondaryConfig.setKeyCreator(typeKeyCreator);
			eventTypeIndex = environment.openSecondaryDatabase(null, "event_type_index.db", null, primaryDatabase, eventTypeSecondaryConfig);

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

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	public void stop() {
		try {
			if (sequence != null) {
				sequence.close();
			}

			if (sequenceDatabase != null) {
				sequenceDatabase.close();
			}

			if (eventTypeIndex != null) {
				eventTypeIndex.close();
			}

			if (primaryDatabase != null) {
				primaryDatabase.close();
			}

			if (environment != null) {
				environment.close();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		System.out.println("Database Closed!");
	}

	public Database getPrimaryDatabase() {
		return primaryDatabase;
	}

	public SecondaryDatabase getEventTypeIndex() {
		return eventTypeIndex;
	}

	public Sequence getSequence() {
		return sequence;
	}
}
