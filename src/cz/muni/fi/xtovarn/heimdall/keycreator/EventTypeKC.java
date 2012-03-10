package cz.muni.fi.xtovarn.heimdall.keycreator;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class EventTypeKC implements SecondaryKeyCreator {

	private ObjectMapper objectMapper;

	public EventTypeKC(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean createSecondaryKey(SecondaryDatabase secondaryDatabase,
	                                  DatabaseEntry keyEntry,
	                                  DatabaseEntry dataEntry,
	                                  DatabaseEntry resultEntry) throws DatabaseException {

		Event event = null;
		try {
			event = objectMapper.readValue(dataEntry.getData(), Event.class);
		} catch (IOException e) {
			e.printStackTrace();  //TODO To change body of catch statement use File | Settings | File Templates.
		}

		assert event != null;
		StringBinding.stringToEntry(event.getType(), resultEntry);

		return true;
	}
}
