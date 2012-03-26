package cz.muni.fi.xtovarn.heimdall.processor;

import cz.muni.fi.xtovarn.heimdall.entity.Event;

import java.util.Date;

public class Enrich extends AbstractAction {

	public Enrich(AbstractAction successor) {
		super(successor);
	}

	public Enrich() {
	}

	@Override
	protected void processEvent(Event event) {
		event.setDetectionTime(new Date(System.currentTimeMillis()));
	}
}
