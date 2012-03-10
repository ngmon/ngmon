package cz.muni.fi.xtovarn.heimdall.keycreator.event;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import org.codehaus.jackson.map.ObjectMapper;

public class TypeKeyCreator extends AbstractKeyCreator implements SecondaryKeyCreator {

	public TypeKeyCreator(ObjectMapper objectMapper) {
		super(objectMapper); // TODO Create static ObjectMapper object
	}

	@Override
	public boolean createSecondaryKey(SecondaryDatabase secondaryDatabase,
	                                  DatabaseEntry keyEntry,
	                                  DatabaseEntry dataEntry,
	                                  DatabaseEntry resultEntry) throws DatabaseException {

		Event event = entryToEvent(dataEntry);
		StringBinding.stringToEntry(event.getType(), resultEntry);

		return true; //TODO null
	}
}
