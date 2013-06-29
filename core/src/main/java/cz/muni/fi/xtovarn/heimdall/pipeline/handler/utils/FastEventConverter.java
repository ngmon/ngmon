package cz.muni.fi.xtovarn.heimdall.pipeline.handler.utils;

import cz.muni.fi.publishsubscribe.countingtree.Attribute;
import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
import cz.muni.fi.publishsubscribe.countingtree.Event;
import cz.muni.fi.publishsubscribe.countingtree.EventImpl;

public class FastEventConverter {

	public Event ngmonEventToPubsubEvent(
			cz.muni.fi.xtovarn.heimdall.commons.entity.Event ngmonEvent) {
		EventImpl pubSubEvent = new EventImpl();
        pubSubEvent.addAttribute(new Attribute<>("level", new AttributeValue<>((long) ngmonEvent.getLevel(), Long.class)));
        pubSubEvent.addAttribute(new Attribute<>("type", new AttributeValue<>(ngmonEvent.getType(), String.class)));

		return pubSubEvent;
	}
}
