package cz.muni.fi.xtovarn.heimdall.storage;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.storage.store.BerkeleyDBEventStoreImpl;
import cz.muni.fi.xtovarn.heimdall.storage.store.JavaEditionBerkeleyDBIOLayer;
import cz.muni.fi.xtovarn.heimdall.storage.store.EventStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StoreTest {

	EventStore eventStore;
	private JavaEditionBerkeleyDBIOLayer ioLayer;

	@Before
	public void setUp() throws Exception {
		ioLayer = new JavaEditionBerkeleyDBIOLayer();
		ioLayer.start();

		eventStore = new BerkeleyDBEventStoreImpl(ioLayer);

	}

	@Test
	public void testPut() throws Exception {

		Event expected = new Event();
		expected.setHostname("Test");

		eventStore.put(expected);

		Event actual = eventStore.getAllRecords().get(0);

		assertEquals(expected, actual);
	}

	@After
	public void tearDown() throws Exception {
		ioLayer.stop();
	}
}
