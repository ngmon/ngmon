package cz.muni.fi.xtovarn.heimdalld.dispatcher;

import cz.muni.fi.xtovarn.heimdalld.db.entity.Event;

import java.util.Set;

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
