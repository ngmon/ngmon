package cz.muni.fi.xtovarn.heimdall.storage.store;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;

import java.io.IOException;
import java.util.List;

public interface EventStore {

	OperationStatus put(Event event) throws DatabaseException, IOException;

	List<Event> getAllRecords() throws DatabaseException, IOException;

	Event getEventById(Long id) throws DatabaseException, IOException;
}