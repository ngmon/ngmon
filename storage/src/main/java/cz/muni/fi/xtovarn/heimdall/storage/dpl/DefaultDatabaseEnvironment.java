package cz.muni.fi.xtovarn.heimdall.storage.dpl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import cz.muni.fi.xtovarn.heimdall.commons.Startable;

import java.io.File;

public class DefaultDatabaseEnvironment {
	private static Environment environment = null;
	private static EntityStore entityStore = null;

	private final EnvironmentConfig environmentConfig;
	private final StoreConfig storeConfig;


	public DefaultDatabaseEnvironment() {

		/* Setup environment */
		environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);

		/* Setup primary eventStore */
		storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);

	}

	public EntityStore setup(File baseDirectory) {
		/* Setup environment root path */

		environment = new Environment(baseDirectory, environmentConfig);

		entityStore = new EntityStore(environment, "myStore", storeConfig);

		return entityStore;
	}

	public void close() {
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
