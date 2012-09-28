package cz.muni.fi.xtovarn.heimdall.storage.event.keycreator;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONEventMapper;

import java.io.IOException;

public class EventTypeKeyCreator implements SecondaryKeyCreator {

	@Override
	public boolean createSecondaryKey(SecondaryDatabase secondaryDatabase,
	                                  DatabaseEntry keyEntry,
	                                  DatabaseEntry dataEntry,
	                                  DatabaseEntry resultEntry) throws DatabaseException {

		Event event = null;
		try {
			event = JSONEventMapper.bytesToEvent(dataEntry.getData());
		} catch (IOException e) {
			e.printStackTrace();  //TODO To change body of catch statement use File | Settings | File Templates.
		}
		if (event != null) {
			StringBinding.stringToEntry(event.getType(), resultEntry);
		}

		return true; //TODO null
	}
}
