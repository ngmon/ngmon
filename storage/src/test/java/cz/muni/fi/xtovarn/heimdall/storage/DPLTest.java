package cz.muni.fi.xtovarn.heimdall.storage;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;
import cz.muni.fi.xtovarn.heimdall.dpl.EventDA;
import cz.muni.fi.xtovarn.heimdall.dpl.MyStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DPLTest {


	private MyStore myStore;
	private EventDA eventDA;

	@Before
	public void setUp() throws Exception {
		myStore = new MyStore();

		eventDA = new EventDA(myStore.initializeAndGetStore());
	}

	@Test
	public void testStorage() throws Exception {
		String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"cron\",\"processId\":\"4219\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":\"http://www.linux.org/schema/monitoring/cron/3.1/events.xsd\",\"schemaVersion\":\"3.1\",\"value\":4648,\"value2\":\"Fax4x46aeEF%aax4x%46aeEF\"}}}";

		Event expected = JSONStringParser.stringToEvent(json);

		eventDA.put(expected);

		Event actual = eventDA.getByHostname("domain.localhost.cz");

		assertEquals(expected, actual);
	}

	@After
	public void tearDown() throws Exception {
		myStore.stop();
	}
}
