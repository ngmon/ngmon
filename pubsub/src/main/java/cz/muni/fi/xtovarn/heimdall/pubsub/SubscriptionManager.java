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

/**
 * Manages associations between subscriptions and clients (users)
 */
public class SubscriptionManager {

	/**
	 * The algorithm/data structure used for the publish-subscribe matching
	 */
	private CountingTree countingTree = new CountingTree();

	/**
	 * Map from user logins to IDs of their subscriptions
	 */
	private Map<String, Set<Long>> loginToSubscriptionIds = new HashMap<>();

	/**
	 * Map from the subscription ID to the user login
	 */
	private Map<Long, String> subscriptionIdToLogin = new HashMap<>();

	/**
	 * Adds the subscription to the data structure
	 * 
	 * @param login
	 *            The user login
	 * @param predicate
	 *            The Predicate (specifies the sensor events to subscribe to)
	 * @return The new subscription ID (can be later used to cancel the
	 *         subscription)
	 */
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

	/**
	 * Remove (cancel) the subscription
	 * 
	 * @param login
	 *            The user who wants to cancel the subscription (to prevent
	 *            canceling the subscription which belongs to someone else)
	 * @param subscriptionId
	 *            The subscription ID
	 * @return True if the operation was successful (correct login and
	 *         subscription ID), false otherwise
	 */
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

	/**
	 * Retrieves all recipients who should receive the Event (who subscribed to
	 * this type of event before)
	 */
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
