package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreIOLayer;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreFactory;

import java.io.IOException;
import java.util.List;

public class StoreApp {

	public static void main(String[] args) throws IOException, DatabaseException {
		/*EventStoreIOLayer store = EventStoreFactory.getSingleInstance();

		List<Event> list = store.getAllRecords();

		for (Event entry : list) {
			System.out.println(entry.toString());
		}

		store.close();*/
	}
}
