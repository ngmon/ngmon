package cz.muni.fi.xtovarn.heimdall.dpl;

import com.sleepycat.je.*;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.EntityModel;
import cz.muni.fi.xtovarn.heimdall.commons.Startable;
import cz.muni.fi.xtovarn.heimdall.storage.event.keycreator.EventTypeKeyCreator;

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

	@Override
	public void start() {
		/* Setup environment root path */
		File baseDirectory = new File(BASE_DIRECTORY);

		environment = new Environment(baseDirectory, environmentConfig);

		entityStore = new EntityStore(environment,"myStore",storeConfig);

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
