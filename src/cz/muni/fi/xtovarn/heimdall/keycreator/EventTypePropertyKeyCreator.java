package cz.muni.fi.xtovarn.heimdall.keycreator;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import cz.muni.fi.xtovarn.heimdall.binding.BasicEventTupleBinding;
import cz.muni.fi.xtovarn.heimdall.entity.Event;

public class EventTypePropertyKeyCreator implements SecondaryKeyCreator {

	private BasicEventTupleBinding eventTupleBinding;

	public EventTypePropertyKeyCreator(BasicEventTupleBinding eventTupleBinding) {
		this.eventTupleBinding = eventTupleBinding;
	}

	@Override
	public boolean createSecondaryKey(SecondaryDatabase secondaryDatabase,
	                                  DatabaseEntry keyEntry,
	                                  DatabaseEntry dataEntry,
	                                  DatabaseEntry resultEntry) throws DatabaseException {

		Event event = eventTupleBinding.entryToObject(dataEntry);
		StringBinding.stringToEntry(event.getType(), resultEntry);

		return true;
	}
}
