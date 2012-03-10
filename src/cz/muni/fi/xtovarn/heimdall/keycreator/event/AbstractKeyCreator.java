package cz.muni.fi.xtovarn.heimdall.keycreator.event;

import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public abstract class AbstractKeyCreator {

	private ObjectMapper objectMapper;

	public AbstractKeyCreator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	protected Event entryToEvent(DatabaseEntry dataEntry) {
		Event event = null;
		try {
			event = objectMapper.readValue(dataEntry.getData(), Event.class);
		} catch (IOException e) {
			e.printStackTrace();  //TODO To change body of catch statement use File | Settings | File Templates.
		}

		return event;
	}

}
