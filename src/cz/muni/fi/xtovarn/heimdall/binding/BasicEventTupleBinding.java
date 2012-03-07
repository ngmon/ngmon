package cz.muni.fi.xtovarn.heimdall.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import cz.muni.fi.xtovarn.heimdall.entity.Event;

import java.util.Date;
import java.util.TimeZone;

public class BasicEventTupleBinding extends TupleBinding<Event> {

	@Override
	public Event entryToObject(TupleInput tupleInput) {
		Long id = tupleInput.readLong();
		Date time = new Date(tupleInput.readLong());
		String service = tupleInput.readString();
		String message = tupleInput.readString();
		TimeZone timeZone = TimeZone.getTimeZone(tupleInput.readString());

		Event event = new Event(id, time, service, message);
		event.setTimeZone(timeZone);

		return event;
	}

	@Override
	public void objectToEntry(Event event, TupleOutput tupleOutput) {
		tupleOutput.writeLong(event.getId());
		tupleOutput.writeLong(event.getTime().getTime());
		tupleOutput.writeString(event.getType());
		tupleOutput.writeString(event.getPayload());
		tupleOutput.writeString(event.getTimeZone().getID());
	}
}
