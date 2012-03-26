package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreFactory;
import org.codehaus.jackson.map.util.ISO8601Utils;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class StoreApp {

	public static void main(String[] args) throws IOException, DatabaseException {
		EventStore store = EventStoreFactory.getInstance();

		List<Event> list = store.getAllRecords();

		for (Event entry : list) {
			System.out.println("id: " + entry.getId() +
					" | detection: " + ISO8601Utils.format(entry.getDetectionTime(), true, TimeZone.getDefault()) +
					" | occurence: " + ISO8601Utils.format(entry.getOccurrenceTime(), true, TimeZone.getDefault()));
		}

		store.close();
	}
}
