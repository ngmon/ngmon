package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreBDB;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.smile.SmileFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException, FileNotFoundException, JsonGenerationException {
		ObjectMapper mapper = new ObjectMapper(new SmileFactory());

		mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);

		EventStoreBDB store = new EventStoreBDB(mapper);
		store.setup();
		Database db = store.getDatabase();
		SecondaryDatabase sdb = store.getSecondaryDatabase();

		DatabaseEntry theKey = new DatabaseEntry();
		DatabaseEntry theData = new DatabaseEntry();

		Long id = store.getSequence().get(null, 1);

		Event event = new Event();

		event.setApplication("Cron");
		event.setHostname("domain.localhost.cz");
		event.setId(id);
		event.setPriority(5);
		event.setProcess("proc_cron NAme");
		event.setProcessId("id005");
		event.setPriority(4);
		event.setSeverity(5);
		event.setTime(new Date(System.currentTimeMillis()));
		event.setType("org.linux.cron.Started");
		Payload payload = new Payload();
		payload.add("value", 4648);
		payload.add("value2", "aax4x46aeEF");
		event.setPayload(payload);

		LongBinding.longToEntry(id, theKey);
		theData.setData(mapper.writeValueAsBytes(event));

		OperationStatus ops = db.put(null, theKey, theData);
		System.out.println(ops.toString());

		SecondaryCursor cursor = sdb.openSecondaryCursor(null, null);
		StringBinding.stringToEntry("org.", theKey);

		OperationStatus retVal = cursor.getSearchKeyRange(theKey, theData, LockMode.DEFAULT);

		while (retVal == OperationStatus.SUCCESS) {
			System.out.println(StringBinding.entryToString(theKey) + ":/:" + mapper.readValue(theData.getData(), Event.class));

			retVal = cursor.getNextNoDup(theKey, theData, LockMode.DEFAULT);
		}

		cursor.close();
		store.close();
	}
}
