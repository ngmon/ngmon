package cz.muni.fi.xtovarn.heimdall.storage.store;

import com.sleepycat.je.*;
import cz.muni.fi.xtovarn.heimdall.commons.Startable;
import cz.muni.fi.xtovarn.heimdall.storage.event.keycreator.EventTypeKeyCreator;

import java.io.File;

public class JavaEditionBerkeleyDBIOLayer implements Startable {
	private static Environment environment = null;
	private static Database primaryDatabase = null;
	private static SecondaryDatabase eventTypeIndex = null;
	private static Database sequenceDatabase = null;
	private static Sequence sequence = null;

	private static final String BASE_DIRECTORY = "./database/events";


	private final EnvironmentConfig environmentConfig;
	private final DatabaseConfig databaseConfig;
	private final SecondaryConfig eventTypeSecondaryConfig;
	private final DatabaseConfig sequenceDatabaseConfig;
	private final SequenceConfig sequenceConfig;

	public JavaEditionBerkeleyDBIOLayer() {

		/* Setup environment */
		environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);

		/* Setup primary eventStore */
		databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);

		/* Setup Event.type secondary eventStore */
		eventTypeSecondaryConfig = new SecondaryConfig();
		eventTypeSecondaryConfig.setAllowCreate(true);
		eventTypeSecondaryConfig.setSortedDuplicates(true);
		eventTypeSecondaryConfig.setKeyCreator(new EventTypeKeyCreator());

		/* Setup sequence eventStore */
		sequenceDatabaseConfig = new DatabaseConfig();
		sequenceDatabaseConfig.setAllowCreate(true);

		/* Setup sequence handler for Event.id */
		sequenceConfig = new SequenceConfig();
		sequenceConfig.setInitialValue(0);
		sequenceConfig.setAllowCreate(true);

	}


	@Override
	public void start() {
			/* Setup environment root path */
			File baseDirectory = new File(BASE_DIRECTORY);

			environment = new Environment(baseDirectory, environmentConfig);

			primaryDatabase = environment.openDatabase(null, "event_store.db", databaseConfig);

			eventTypeIndex = environment.openSecondaryDatabase(null, "event_type_index.db", primaryDatabase, eventTypeSecondaryConfig);

			sequenceDatabase = environment.openDatabase(null, "event_sequence.db", sequenceDatabaseConfig);

			sequence = sequenceDatabase.openSequence(null, new DatabaseEntry("id".getBytes()), sequenceConfig);

	}

	public void stop() {
		System.out.println("Closing " + this.getClass() + "...");

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

		System.out.println(this.getClass() + " closed!");
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
