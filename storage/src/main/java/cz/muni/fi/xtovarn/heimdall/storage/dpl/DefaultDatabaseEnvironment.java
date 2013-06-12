package cz.muni.fi.xtovarn.heimdall.storage.dpl;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DefaultDatabaseEnvironment {
	private static Environment environment = null;
	private static EntityStore entityStore = null;

	private final EnvironmentConfig environmentConfig;
	private final StoreConfig storeConfig;

	private static Logger logger = LogManager.getLogger(DefaultDatabaseEnvironment.class);

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
		logger.info("Closing " + this.getClass() + "...");

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

		logger.info(this.getClass() + " closed!");
	}
}
