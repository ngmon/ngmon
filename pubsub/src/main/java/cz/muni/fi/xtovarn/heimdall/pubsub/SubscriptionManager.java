package cz.muni.fi.xtovarn.heimdall.pubsub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.muni.fi.publishsubscribe.countingtree.CountingTree;
import cz.muni.fi.publishsubscribe.countingtree.Event;
import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.publishsubscribe.countingtree.Subscription;

public class SubscriptionManager {

	private CountingTree countingTree = new CountingTree();
	private Map<String, Set<Long>> loginToSubscriptionIds = new HashMap<>();
	private Map<Long, String> subscriptionIdToLogin = new HashMap<>();

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

		subscriptionIdToLogin.put(subscriptionId, login);

		return subscriptionId;
	}

	public synchronized boolean removeSubscription(String login, Long subscriptionId) {
		// need to check whether the user sent correct ID
		// (and not one that "belongs" to another user, for example)
		Set<Long> subscriptionIds = this.loginToSubscriptionIds.get(login);
		if (subscriptionIds == null)
			return false;

		boolean removeResult = subscriptionIds.remove(subscriptionId);
		if (removeResult) {
			this.countingTree.unsubscribe(subscriptionId);
			subscriptionIdToLogin.remove(subscriptionId);
		}

		return removeResult;
	}

	// TODO - is it necessary to synchronize this?
	public synchronized Set<String> getRecipients(Event event) {
		List<Subscription> subscriptions = countingTree.match(event);
		Set<String> recipients = new HashSet<>();
		
		for (Subscription subscription : subscriptions) {
			String login = subscriptionIdToLogin.get(subscription.getId());
			// this shouldn't happen
			if (login != null)
				recipients.add(login);
		}
		
		return recipients;
	}

}
