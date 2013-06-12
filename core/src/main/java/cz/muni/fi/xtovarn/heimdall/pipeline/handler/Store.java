package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sleepycat.je.DatabaseException;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.storage.EventStore;

import java.io.IOException;

/**
 * Saves the event to a (persistent) storage
 */
public class Store implements Handler {

	private final EventStore store;
	
	private static Logger logger = LogManager.getLogger(Store.class);

	public Store(EventStore store) {
		this.store = store;
	}

	@Override
	public Object handle(Object o) {
		try {
			store.put((Event) o);
		} catch (DatabaseException e) {
			logger.error("DatabaseException " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception " + e.getMessage());
			e.printStackTrace();
		}

		return o;
	}
}
