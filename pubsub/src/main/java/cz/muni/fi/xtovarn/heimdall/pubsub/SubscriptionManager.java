package cz.muni.fi.xtovarn.heimdall.pubsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cz.muni.fi.publishsubscribe.countingtree.CountingTree;
import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.publishsubscribe.countingtree.Subscription;

public class SubscriptionManager {

	private CountingTree countingTree = new CountingTree();
	private Map<Integer, Collection<Subscription>> connectionIdToSubscriptions = new HashMap<>();

	public synchronized Subscription addSubscription(Integer connectionId, Predicate predicate) {
		Subscription subscription = new Subscription();
		this.countingTree.subscribe(predicate, subscription);

		Collection<Subscription> subscriptions = connectionIdToSubscriptions.get(connectionId);
		if (subscriptions == null) {
			subscriptions = new ArrayList<>();
			subscriptions.add(subscription);
			this.connectionIdToSubscriptions.put(connectionId, subscriptions);
		} else {
			subscriptions.add(subscription);
		}
		
		return subscription;
	}

}
