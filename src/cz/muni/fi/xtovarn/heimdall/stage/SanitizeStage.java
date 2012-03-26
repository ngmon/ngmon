package cz.muni.fi.xtovarn.heimdall.stage;

import cz.muni.fi.xtovarn.heimdall.entity.Event;

import java.util.Date;
import java.util.concurrent.BlockingQueue;


public class SanitizeStage extends AbstractStage<Event, Event> implements Stage<Event, Event> {

	public SanitizeStage(BlockingQueue<Event> inWorkQueue, BlockingQueue<Event> outWorkQueue) {
		super(inWorkQueue, outWorkQueue);
	}

	@Override
	public Event work(Event workItem) {
		workItem.setDetectionTime(new Date(System.currentTimeMillis()));

		return workItem;
	}
}
