package cz.muni.fi.publishsubscribe.countingtree.index.operator;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Index for the GREATER_THAN_OR_EQUAL_TO Operator
 *
 * @param <T1> The type of the values the index stores
 */
public class GreaterThanOrEqualToIndex<T1 extends Comparable<T1>> extends AbstractNavigableMapIndex<T1> {

	@Override
	public List<Collection<Constraint<T1>>> getConstraints(T1 attributeValue) {

		Collection<Constraint<T1>> values = this.constraints.headMap(attributeValue, true).values();
		List<Collection<Constraint<T1>>> list = new ArrayList<>();
		list.add(values);
		
		return list;
	}
}
