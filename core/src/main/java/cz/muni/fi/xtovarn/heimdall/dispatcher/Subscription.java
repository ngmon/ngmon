package cz.muni.fi.xtovarn.heimdall.dispatcher;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;

import java.util.Set;

/**
 * Saves a sensor event and the recipients for this event
 */
public class Subscription {
	private final Set<String> recipients;
	private final Event event;

	public Subscription(Set<String> recipients, Event event) {
		this.recipients = recipients;
		this.event = event;
	}

	public Set<String> getRecipients() {
		return recipients;
	}

	public Event getEvent() {
		return event;
	}
}
