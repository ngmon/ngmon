package cz.muni.fi.xtovarn.heimdall.storage;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.sleepycat.persist.EntityStore;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.DefaultEnvironment;
import cz.muni.fi.xtovarn.heimdall.storage.dpl.EventDataAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class DPLTest {

	@Rule
	public static final TemporaryFolder JUNIT_TEMPORARY_DIRECTORY = new TemporaryFolder();
	private static final File BASE_DIRECTORY = JUNIT_TEMPORARY_DIRECTORY.newFolder("junit_testdatabase");

	private DefaultEnvironment myEnvironment;
	private EventDataAccessor eventDataAccessor;

	private static final InputStream JSON_FILE_INPUT_STREAM = DPLTest.class.getResourceAsStream("event1.json");
	private static final String DETECTION_TIME = "2013-02-26T12:24:05.425+00:00";
	private static String JSON_STRING = "";

	@Before
	public void setUp() throws Exception {

		// Setup Storage
		myEnvironment = new DefaultEnvironment();
		EntityStore entityStore = myEnvironment.setup(BASE_DIRECTORY);
		entityStore.truncateClass(Event.class);
		eventDataAccessor = new EventDataAccessor(entityStore);

		// Read JSON from File
		JSON_STRING = this.readInputStream(JSON_FILE_INPUT_STREAM);
	}

	@Test
	public void testStorage() throws Exception {

		Event expected = JSONStringParser.stringToEvent(JSON_STRING);
		expected.setDetectionTime(ISO8601Utils.parse(DETECTION_TIME));
		eventDataAccessor.put(expected);
		Event actual = eventDataAccessor.getEventById(1L);

		assertEquals(expected, actual);
	}

	@After
	public void tearDown() throws Exception {
		myEnvironment.close();
		this.recursiveDelete(BASE_DIRECTORY);
	}

	private String readInputStream(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	private void recursiveDelete(File file) {
		File[] files = file.listFiles();
		if (files != null)
			for (File each : files)
				recursiveDelete(each);
		file.delete();
	}
}
