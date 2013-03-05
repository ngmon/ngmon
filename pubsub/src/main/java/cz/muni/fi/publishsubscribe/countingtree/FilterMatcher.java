package cz.muni.fi.publishsubscribe.countingtree;

import cz.muni.fi.publishsubscribe.countingtree.index.AttributeIndex;

import java.util.*;

/**
 * Stores data for the Counting Tree (stores Constraints inside Predicates using
 * AttributeIndex), returns all Filters matching a specific Event (by examining
 * the individual Constraints inside the Filter)
 */
public class FilterMatcher {

	private AttributeIndex attributeIndex = new AttributeIndex();

	private Map<Filter, Filter> filters = new HashMap<>();
	private Map<Constraint<?>, Constraint<?>> constraints = new HashMap<>();

	private Long filterId = 1L;

	public FilterMatcher() {
	}

	public void addPredicate(Predicate predicate) {
		List<Filter> predicateFilters = predicate.getFilters();

		for (Filter filter : predicateFilters) {

			Filter fullFilter = filters.get(filter);
			// the filter has already been inserted
			if (fullFilter != null) {
				fullFilter.addPredicate(predicate);
				// new filter
			} else {
				filter.setId(filterId++);
				filters.put(filter, filter);
				filter.addPredicate(predicate);

				List<Constraint<? extends Comparable<?>>> filterConstraints = filter
						.getConstraints();
				for (Constraint<? extends Comparable<?>> constraint : filterConstraints) {

					this.attributeIndex.addConstraint(constraint);

					Constraint<?> fullConstraint = constraints.get(constraint);
					if (fullConstraint != null) {
						fullConstraint.addFilter(filter);
					} else {
						constraints.put(constraint, constraint);
						constraint.addFilter(filter);
					}
				}
			}
		}
	}

	public void removePredicate(Predicate predicate) {
		for (Filter filter : predicate.getFilters()) {
			filter.removePredicate(predicate);
			if (filter.noPredicates())
				filters.remove(filter);

			for (Constraint<?> constraint : filter.getConstraints()) {
				constraint.removeFilter(filter);
				if (constraint.noFilters())
					constraints.remove(constraint);

				attributeIndex.removeConstraint(constraint);
			}
		}
	}

	public <T1 extends Comparable<T1>, T2 extends Constraint<T1>> List<Collection<Constraint<T1>>> getConstraintLists(
			Attribute<T1> attribute) {
		return this.attributeIndex.getConstraints(attribute.getName(),
				attribute.getValue());
	}

}
