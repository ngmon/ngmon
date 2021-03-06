package cz.muni.fi.xtovarn.heimdall.storage.dpl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.storage.EventStore;

import java.io.IOException;
import java.util.*;

public class EventDataAccessor implements EventStore {

	PrimaryIndex<Long, Event> primaryIndex;

	SecondaryIndex<Date, Long, Event> occurrenceTimeIndex;
	SecondaryIndex<Date, Long, Event> detectionTimeIndex;
	SecondaryIndex<String, Long, Event> hostnameIndex;
	SecondaryIndex<String, Long, Event> typeIndex;
	SecondaryIndex<String, Long, Event> applicationIndex;
	SecondaryIndex<String, Long, Event> processIndex;
	SecondaryIndex<String, Long, Event> processIdIndex;
	SecondaryIndex<Integer, Long, Event> levelIndex;
	SecondaryIndex<Integer, Long, Event> priorityIndex;

	public EventDataAccessor(EntityStore store) {
		primaryIndex = store.getPrimaryIndex(Long.class, Event.class);

		occurrenceTimeIndex = store.getSecondaryIndex(primaryIndex, Date.class, "occurrenceTime");
		detectionTimeIndex = store.getSecondaryIndex(primaryIndex, Date.class, "detectionTime");
		hostnameIndex = store.getSecondaryIndex(primaryIndex, String.class, "hostname");
		typeIndex = store.getSecondaryIndex(primaryIndex, String.class, "type");
		applicationIndex = store.getSecondaryIndex(primaryIndex, String.class, "application");
		processIndex = store.getSecondaryIndex(primaryIndex, String.class, "process");
		processIdIndex = store.getSecondaryIndex(primaryIndex, String.class, "processId");
		levelIndex = store.getSecondaryIndex(primaryIndex, Integer.class, "level");
		priorityIndex = store.getSecondaryIndex(primaryIndex, Integer.class, "priority");
	}

	@Override
	public Event put(Event event) {
		return primaryIndex.put(event);
	}

	@Override
	public List<Event> getAllRecords() throws DatabaseException, IOException {

		return new ArrayList<Event>(primaryIndex.sortedMap().values());
	}

	@Override
	public Event getEventById(Long id) throws DatabaseException, IOException {

		return primaryIndex.get(id);
	}

	public Event getByHostname(String hostname) {

		return hostnameIndex.get(hostname);
	}
}
