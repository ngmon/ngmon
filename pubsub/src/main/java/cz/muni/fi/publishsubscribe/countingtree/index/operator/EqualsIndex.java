package cz.muni.fi.publishsubscribe.countingtree.index.operator;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;

import java.util.*;

/**
 * Index for the EQUALS Operator
 * Optimized for fast lookups of a specific value and returning the
 * associated values (based on HashMap)
 *
 * @param <T1> The type of the values the index stores
 */
public class EqualsIndex<T1 extends Comparable<T1>> implements OperatorIndex<T1> {

	protected Map<T1, Constraint<T1>> constraints = new HashMap<>();

	public boolean addConstraint(Constraint<T1> constraint) {

		T1 value = constraint.getAttributeValue().getValue();

		if (this.constraints.containsKey(value)) {
			return false;
		}

		this.constraints.put(value, constraint);

		return true;
	}

	@Override
	public boolean removeConstraint(Constraint<T1> constraint) {
		return (constraints.remove(constraint.getAttributeValue().getValue()) != null);
	}

	// Returning only single Constraint!!
	public List<Collection<Constraint<T1>>> getConstraints(T1 attributeValue) {

		Constraint<T1> constraint = this.constraints.get(attributeValue);

		if (constraint == null) {
			return Collections.emptyList();
		}

		List<Constraint<T1>> constraintList = new ArrayList<>();

		constraintList.add(constraint);
		
		List<Collection<Constraint<T1>>> list = new ArrayList<>();
		list.add(constraintList);

		return list;
	}
}
