package cz.muni.fi.xtovarn.heimdall.keycreator;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class EventTypeKC extends AbstractEventKC implements SecondaryKeyCreator {

	public EventTypeKC(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public boolean createSecondaryKey(SecondaryDatabase secondaryDatabase,
	                                  DatabaseEntry keyEntry,
	                                  DatabaseEntry dataEntry,
	                                  DatabaseEntry resultEntry) throws DatabaseException {

		Event event = entryToObject(dataEntry);
		StringBinding.stringToEntry(event.getType(), resultEntry);

		return true; //TODO null
	}
}
