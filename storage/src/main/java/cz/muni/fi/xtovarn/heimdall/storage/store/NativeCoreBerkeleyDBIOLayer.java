//package cz.muni.fi.xtovarn.heimdall.storage.store;
//
//import com.sleepycat.db.*;
//import cz.muni.fi.xtovarn.heimdall.storage.event.keycreator.EventTypeKeyCreator;
//import cz.muni.fi.xtovarn.heimdall.commons.Startable;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//
///**
// * This is Berkeley DB Core (using Base Java API) implementation of DB Store. Note, that BDB Java Edition API differs.
// *
// *
// */
//
//@Deprecated
//public class NativeCoreBerkeleyDBIOLayer implements Startable {
//	private static Environment environment = null;
//	private static Database primaryDatabase = null;
//	private static SecondaryDatabase eventTypeIndex = null;
//	private static Database sequenceDatabase = null;
//	private static Sequence sequence = null;
//
//	private static final String BASE_DIRECTORY = "./database/events";
//
//
//	private final EnvironmentConfig environmentConfig;
//	private final DatabaseConfig databaseConfig;
//	private final SecondaryConfig eventTypeSecondaryConfig;
//	private final DatabaseConfig sequenceDatabaseConfig;
//	private final SequenceConfig sequenceConfig;
//
//	public NativeCoreBerkeleyDBIOLayer() {
//
//		/* Setup environment */
//		environmentConfig = new EnvironmentConfig();
//		environmentConfig.setAllowCreate(true);
//		environmentConfig.setInitializeCache(true);
//		environmentConfig.setThreaded(true);
//
//		/* Setup primary eventStore */
//		databaseConfig = new DatabaseConfig();
//		databaseConfig.setAllowCreate(true);
//		databaseConfig.setType(DatabaseType.BTREE);
//
//		/* Setup Event.type secondary eventStore */
//		eventTypeSecondaryConfig = new SecondaryConfig();
//		eventTypeSecondaryConfig.setAllowCreate(true);
//		eventTypeSecondaryConfig.setType(DatabaseType.BTREE);
//		eventTypeSecondaryConfig.setSortedDuplicates(true);
//		EventTypeKeyCreator typeKeyCreator = new EventTypeKeyCreator(); // Event.type EventTypeKeyCreator
//		eventTypeSecondaryConfig.setKeyCreator(typeKeyCreator);
//
//		/* Setup sequence eventStore */
//		sequenceDatabaseConfig = new DatabaseConfig();
//		sequenceDatabaseConfig.setAllowCreate(true);
//		sequenceDatabaseConfig.setType(DatabaseType.HASH);
//
//		/* Setup sequence handler for Event.id */
//		sequenceConfig = new SequenceConfig();
//		sequenceConfig.setInitialValue(0);
//		sequenceConfig.setAllowCreate(true);
//
//	}
//
//
//	@Override
//	public void start() {
//		try {
//			/* Setup environment root path */
//			File baseDirectory = new File(BASE_DIRECTORY);
//
//
//			environment = new Environment(baseDirectory, environmentConfig);
//
//			primaryDatabase = environment.openDatabase(null, "event_store.db", null, databaseConfig);
//
//			eventTypeIndex = environment.openSecondaryDatabase(null, "event_type_index.db", null, primaryDatabase, eventTypeSecondaryConfig);
//
//			sequenceDatabase = environment.openDatabase(null, "event_sequence.db", null, sequenceDatabaseConfig);
//
//			sequence = sequenceDatabase.openSequence(null, new DatabaseEntry("id".getBytes()), sequenceConfig);
//
//		} catch (FileNotFoundException e) {
//			System.err.println("FileNotFoundException: " + e.getCause());
//			e.printStackTrace();
//		} catch (DatabaseException e) {
//			System.err.println("DatabaseException: " + e.getCause());
//			e.printStackTrace();
//		}
//	}
//
//	public void stop() {
//		System.out.println("Closing " + this.getClass() + "...");
//
//		try {
//			if (sequence != null) {
//				sequence.close();
//			}
//
//			if (sequenceDatabase != null) {
//				sequenceDatabase.close();
//			}
//
//			if (eventTypeIndex != null) {
//				eventTypeIndex.close();
//			}
//
//			if (primaryDatabase != null) {
//				primaryDatabase.close();
//			}
//
//			if (environment != null) {
//				environment.close();
//			}
//		} catch (DatabaseException e) {
//			e.printStackTrace();
//		}
//
//		System.out.println(this.getClass() + " closed!");
//	}
//
//	public Database getPrimaryDatabase() {
//		return primaryDatabase;
//	}
//
//	public SecondaryDatabase getEventTypeIndex() {
//		return eventTypeIndex;
//	}
//
//	public Sequence getSequence() {
//		return sequence;
//	}
//}
