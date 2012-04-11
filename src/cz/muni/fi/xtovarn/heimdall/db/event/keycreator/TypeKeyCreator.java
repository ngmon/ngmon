package cz.muni.fi.xtovarn.heimdall.db.event.keycreator;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONEventMapper;

import java.io.IOException;

public class TypeKeyCreator implements SecondaryKeyCreator {

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
