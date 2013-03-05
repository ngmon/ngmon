package cz.muni.fi.publishsubscribe.countingtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main (front-end) class for the algorithm; Usage: Add the required
 * Predicates (subscriptions) first, then get the Predicates matching the
 * Event(s)
 */
public class CountingTree {

	private Long subscriptionNextId = 1L;

	// I need to be able to get the Predicate with all the data (list of
	// subscriptions)
	private Map<Predicate, Predicate> predicates = new HashMap<>();
	private FilterMatcher matcher = new FilterMatcher();
	private Map<Subscription, Predicate> subscriptionToPredicate = new HashMap<>();

	public Long subscribe(Predicate predicate, Subscription subscription) {
		subscription.setId(subscriptionNextId);

		// compute hash codes now to slightly improve match() performance
		predicate.computeHashCode();
		for (Filter filter : predicate.getFilters())
			filter.computeHashCode();

		Predicate fullPredicate = predicates.get(predicate);
		// the same predicate has already been inserted
		if (fullPredicate != null) {
			fullPredicate.addSubscription(subscription);
			subscriptionToPredicate.put(subscription, fullPredicate);
		} else {
			predicate.addSubscription(subscription);
			predicates.put(predicate, predicate);

			Set<Predicate> predicateSet = new HashSet<>();
			predicateSet.add(predicate);
			subscriptionToPredicate.put(subscription, predicate);

			matcher.addPredicate(predicate);
		}

		return subscriptionNextId++;
	}

	/**
	 * Only for compatibility reasons, not intended for common use
	 */
	@Deprecated
	public Long subscribe(Predicate predicate) {
		return subscribe(predicate, new Subscription());
	}

	public boolean unsubscribe(Long subscriptionId) {
		Subscription subscription = new Subscription();
		subscription.setId(subscriptionId);
		return unsubscribe(subscription);
	}

	public boolean unsubscribe(Subscription subscription) {
		if (subscription.getId() == null)
			return false;

		if (!subscriptionToPredicate.containsKey(subscription))
			return false;

		Predicate predicate = subscriptionToPredicate.get(subscription);
		matcher.removePredicate(predicate);
		subscriptionToPredicate.remove(subscription);

		return true;
	}

	public <T1 extends Comparable<T1>, T2 extends Constraint<T1>> List<Subscription> match(
			Event event) {

		if (matcher == null) {
			return new ArrayList<Subscription>();
		}
		
		int allSubcriptionsSize = subscriptionToPredicate.size();

		List<Subscription> subscriptions = new ArrayList<>();
		Set<Predicate> matchedPredicates = new HashSet<>();

		Map<Long, Integer> counters = new HashMap<>();

		List<Attribute<? extends Comparable<?>>> attributes = event
				.getAttributes();

		for (Attribute<? extends Comparable<?>> uncastAttribute : attributes) {
			Attribute<T1> attribute = (Attribute<T1>) uncastAttribute;

			List<Collection<Constraint<T1>>> foundConstraintLists = this.matcher
					.getConstraintLists(attribute);
			for (Collection<Constraint<T1>> foundConstraints : foundConstraintLists) {
				for (Constraint<T1> constraint : foundConstraints) {
					for (Filter filter : constraint.getFilters()) {
						Integer counter = counters.get(filter.getId());
						int filterConstraintsSize = filter.getConstraints()
								.size();
						boolean matched = (counter != null && (counter >= filterConstraintsSize));
						if (!matched) {
							if (counter == null)
								counter = 0;
							counters.put(filter.getId(), ++counter);
							matched = (counter >= filterConstraintsSize);
						}
						if (matched) {
							for (Predicate predicate : filter.getPredicates()) {
								if (!(matchedPredicates.contains(predicate))) {
									subscriptions.addAll(predicate
											.getSubscriptions());
									matchedPredicates.add(predicate);
									if (subscriptions.size() >= allSubcriptionsSize)
										return subscriptions;
								}
							}
						}
					}
				}
			}
		}

		return subscriptions;
	}
}
