package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class StoreApp {

	public static void main(String[] args) throws IOException, DatabaseException {
		EventStore store = new EventStore();

		Event event = new Event();

		event.setApplication("Cron");
		event.setHostname("domain.localhost.cz");
		event.setPriority(5);
		event.setProcess("proc_cron NAme");
		event.setProcessId("id005");
		event.setPriority(4);
		event.setSeverity(5);
		event.setOccurrenceTime(new Date(System.currentTimeMillis()));
		event.setType("org.linux.cron.Started");
		Payload payload = new Payload();
		payload.add("value", 4648);
		payload.add("value2", "aax4x46aeEF");
		event.setPayload(payload);

		OperationStatus ops = store.put(event);
		System.out.println(ops.toString());
		
		List<Event> list = store.getAllRecords();

		for(Event entry : list){
			System.out.println(entry.toString());
		}

		store.close();
	}
}