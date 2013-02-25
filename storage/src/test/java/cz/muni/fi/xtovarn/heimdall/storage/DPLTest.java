package cz.muni.fi.xtovarn.heimdall.storage;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.sleepycat.persist.EntityStore;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.DefaultEnvironment;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.EventDataAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DPLTest {

	private DefaultEnvironment myEnvironment;
	private EventDataAccessor eventDataAccessor;

	private static final String BASE_DIRECTORY = "./database/testdb";

	@Before
	public void setUp() throws Exception {
		myEnvironment = new DefaultEnvironment();

		EntityStore entityStore = myEnvironment.setup(new File(BASE_DIRECTORY));

		entityStore.truncateClass(Event.class);

		eventDataAccessor = new EventDataAccessor(entityStore);
	}

	@Test
	public void testStorage() throws Exception {
		String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"cron\",\"processId\":\"4219\",\"level\":5,\"priority\":4,\"_\":{\"schema\":\"http://www.linux.org/schema/monitoring/cron/3.1/events.xsd\",\"schemaVersion\":\"3.1\",\"value\":4648,\"value2\":\"Fax4x46aeEF%aax4x%46aeEF\"}}}";

		Event expected = JSONStringParser.stringToEvent(json);

		eventDataAccessor.put(expected);

		Event actual = eventDataAccessor.getByHostname("domain.localhost.cz");

		assertEquals(expected, actual);
	}

	@After
	public void tearDown() throws Exception {
		myEnvironment.close();
	}
}
