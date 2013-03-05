package cz.muni.fi.publishsubscribe.countingtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Stores one or more Filters Can be either true of false for a specific event
 * It's true for an event iff it's true for at least one of the Filters the
 * Predicate contains (We can also say the Predicate either does or does not
 * satisfy an event)
 */
public class Predicate {

	private Long id = null;
	private List<Filter> filters = new ArrayList<>();

	private List<Subscription> subscriptions = new ArrayList<>();

	private Integer cachedHashCode = null;

	public List<Filter> getFilters() {
		return filters;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void addFilter(Filter filter) {
		this.filters.add(filter);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Predicate predicate = (Predicate) o;

		// if both IDs have been set, I can just compare IDs
		if (id != null && predicate.id != null) {
			return id.equals(predicate.id);
		} else {
			return filters.equals(predicate.filters);
		}
	}
	
	public void computeHashCode() {
		cachedHashCode = filters.hashCode();
	}

	@Override
	public int hashCode() {
		if (cachedHashCode == null)
			computeHashCode();
		return cachedHashCode;
	}

	public void addSubscription(Subscription subscription) {
		subscriptions.add(subscription);
	}

	public void removeSubscription(Subscription subscription) {
		subscriptions.remove(subscription);
	}

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

}
