package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Subscription;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.handler.utils.EventConverter;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;

/**
 * This handler appends to the sensor event the appropriate recipients (clients)
 * which subscribed to this event before
 */
public class DetermineRecipient implements Handler {

	private final SubscriptionManager subscriptionManager;
	private SecureChannelGroup secureChannelGroup;
	private EventConverter eventConverter = new EventConverter();

	public DetermineRecipient(SecureChannelGroup secureChannelGroup, SubscriptionManager subscriptionManager) {
		this.secureChannelGroup = secureChannelGroup;
		this.subscriptionManager = subscriptionManager;
	}

	@Override
	public Object handle(Object o) {
		Event event = (Event) o;

		Set<String> recipients;

		try {
			// get matching recipients
			recipients = subscriptionManager.getRecipients(eventConverter.ngmonEventToPubsubEvent(event));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
			throw new RuntimeException(e);
		}

		// keep only recipients which are connected and receiving (are in the
		// RECEIVING state, i.e. sent READY)
		recipients.retainAll(secureChannelGroup.getReceivingUsers());

		// append the recipients
		return new Subscription(recipients, event);
	}
}
