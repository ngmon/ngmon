package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;

/**
 * Objects of this class are used for processing received sensor events
 */
public interface EventReceivedHandler {

	public void handleEvent(Event event);

}
