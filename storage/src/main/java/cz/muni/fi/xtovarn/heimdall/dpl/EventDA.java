package cz.muni.fi.xtovarn.heimdall.dpl;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;

import java.util.Date;

public class EventDA {

	PrimaryIndex<Long, Event> primaryIndex;

	SecondaryIndex<Date, Long, Event> occurrenceTimeIndex;
	SecondaryIndex<Date, Long, Event> detectionTimeIndex;
	SecondaryIndex<String, Long, Event> hostnameIndex;
	SecondaryIndex<String, Long, Event> typeIndex;
	SecondaryIndex<String, Long, Event> applicationIndex;
	SecondaryIndex<String, Long, Event> processIndex;
	SecondaryIndex<String, Long, Event> processIdIndex;
	SecondaryIndex<Integer, Long, Event> severityIndex;
	SecondaryIndex<Integer, Long, Event> priorityIndex;

	public EventDA(EntityStore store) {
		primaryIndex = store.getPrimaryIndex(Long.class, Event.class);

		occurrenceTimeIndex = store.getSecondaryIndex(primaryIndex, Date.class, "occurrenceTime");
		detectionTimeIndex = store.getSecondaryIndex(primaryIndex, Date.class, "detectionTime");
		hostnameIndex = store.getSecondaryIndex(primaryIndex, String.class, "hostname");
		typeIndex = store.getSecondaryIndex(primaryIndex, String.class, "type");
		applicationIndex = store.getSecondaryIndex(primaryIndex, String.class, "application");
		processIndex = store.getSecondaryIndex(primaryIndex, String.class, "process");
		processIdIndex = store.getSecondaryIndex(primaryIndex, String.class, "processId");
		severityIndex = store.getSecondaryIndex(primaryIndex, Integer.class, "severity");
		priorityIndex = store.getSecondaryIndex(primaryIndex, Integer.class, "priority");
	}

	public void put(Event event) {
		primaryIndex.put(event);
	}

	public Event getByHostname(String hostname) {
		return hostnameIndex.get(hostname);
	}
}
