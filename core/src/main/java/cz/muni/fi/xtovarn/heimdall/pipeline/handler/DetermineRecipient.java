package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Subscription;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManagerSingleton;

import java.util.HashSet;
import java.util.Set;

public class DetermineRecipient implements Handler {

	private SubscriptionManager subscriptionManager;
	private SecureChannelGroup secureChannelGroup;

	public DetermineRecipient(SecureChannelGroup secureChannelGroup) {
		subscriptionManager = SubscriptionManagerSingleton.getSubscriptionManager();
		this.secureChannelGroup = secureChannelGroup;
	}

	@Override
	public Object handle(Object o) {
		Event event = (Event) o;
		
		/*-Set<String> recipients = subscriptionManager.getRecipients(event);
		for (String recipient : recipients) {
		}*/

		return new Subscription(new HashSet<String>(), event);
	}
}
