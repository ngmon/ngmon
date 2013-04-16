package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;

public interface EventReceivedHandler {
	
	public void handleEvent(Event event);

}
