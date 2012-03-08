package cz.muni.fi.xtovarn.heimdall.store;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.keycreator.EventTypePropertyKeyCreator;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;

public class EventStoreBDB {
	private static Environment environment = null;
	private static Database database = null;
	private static SecondaryDatabase secondaryDatabase = null;
	private static Database sequenceDatabase = null;
	private static Sequence sequence = null;
	private static ObjectMapper objectMapper = null;

	public EventStoreBDB(ObjectMapper mapper) {
		objectMapper = mapper;
	}

	public void setup() throws FileNotFoundException, DatabaseException {
		File dbHomeDirectory = new File("./database/evsbdb");
		String databaseName = "event_store.db";

		EventTypePropertyKeyCreator keyCreator = new EventTypePropertyKeyCreator(objectMapper);

		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		environmentConfig.setInitializeCache(true);
		environmentConfig.setThreaded(true);

		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);
		databaseConfig.setType(DatabaseType.BTREE);

		SecondaryConfig secondaryConfig = new SecondaryConfig();
		secondaryConfig.setAllowCreate(true);
		secondaryConfig.setType(DatabaseType.BTREE);
		secondaryConfig.setSortedDuplicates(true);
		secondaryConfig.setKeyCreator(keyCreator);

		DatabaseConfig sequenceDatabaseConfig = new DatabaseConfig();
		sequenceDatabaseConfig.setAllowCreate(true);
		sequenceDatabaseConfig.setType(DatabaseType.HASH);

		SequenceConfig sequenceConfig = new SequenceConfig();
		sequenceConfig.setInitialValue(0);
		sequenceConfig.setAllowCreate(true);

		environment = new Environment(dbHomeDirectory, environmentConfig);
		database = environment.openDatabase(null, databaseName, null, databaseConfig);
		secondaryDatabase = environment.openSecondaryDatabase(null, "type_index.db", null, database, secondaryConfig);
		sequenceDatabase = environment.openDatabase(null, "event_id_sequence.db", null, sequenceDatabaseConfig);
		sequence = sequenceDatabase.openSequence(null, new DatabaseEntry("id".getBytes()), sequenceConfig);
	}

	public Database getDatabase() {
		return database;
	}

	public SecondaryDatabase getSecondaryDatabase() {
		return secondaryDatabase;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void close() throws DatabaseException {

		if (sequence != null) {
			sequence.close();
		}

		if (sequenceDatabase != null) {
			sequenceDatabase.close();
		}


		if (secondaryDatabase != null) {
			secondaryDatabase.close();
		}

		if (database != null) {
			database.close();
		}

		if (environment != null) {
			environment.close();
		}
	}

}
