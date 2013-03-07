package cz.muni.fi.xtovarn.heimdall.pubsub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.muni.fi.publishsubscribe.countingtree.CountingTree;
import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.publishsubscribe.countingtree.Subscription;

public class SubscriptionManager {

	private CountingTree countingTree = new CountingTree();
	private Map<String, Set<Long>> loginToSubscriptionIds = new HashMap<>();

	public synchronized Long addSubscription(String login, Predicate predicate) {
		Subscription subscription = new Subscription();
		this.countingTree.subscribe(predicate, subscription);
		Long subscriptionId = subscription.getId();

		Set<Long> subscriptionIds = loginToSubscriptionIds.get(login);
		if (subscriptionIds == null) {
			subscriptionIds = new HashSet<>();
			subscriptionIds.add(subscriptionId);
			this.loginToSubscriptionIds.put(login, subscriptionIds);
		} else {
			subscriptionIds.add(subscriptionId);
		}

		return subscriptionId;
	}

	public synchronized boolean removeSubscription(String login, Long subscriptionId) {
		Set<Long> subscriptionIds = this.loginToSubscriptionIds.get(login);
		if (subscriptionIds == null)
			return false;
		return subscriptionIds.remove(subscriptionId);
	}

}
