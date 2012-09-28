package cz.muni.fi.xtovarn.heimdall.dpl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import cz.muni.fi.xtovarn.heimdall.commons.Startable;

import java.io.File;

public class MyStore implements Startable {
	private static Environment environment = null;
	private static EntityStore entityStore = null;

	private static final String BASE_DIRECTORY = "./database/events";

	private final EnvironmentConfig environmentConfig;
	private final StoreConfig storeConfig;


	public MyStore() {

		/* Setup environment */
		environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);

		/* Setup primary eventStore */
		storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);

	}

	public EntityStore initializeAndGetStore() {
		start();

		return entityStore;
	}

	@Override
	public void start() {
		/* Setup environment root path */
		File baseDirectory = new File(BASE_DIRECTORY);

		environment = new Environment(baseDirectory, environmentConfig);

		entityStore = new EntityStore(environment, "myStore", storeConfig);

	}

	@Override
	public void stop() {
		System.out.println("Closing " + this.getClass() + "...");

		try {
			if (entityStore != null) {
				entityStore.close();
			}

			if (environment != null) {
				environment.close();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		System.out.println(this.getClass() + " closed!");
	}
}
