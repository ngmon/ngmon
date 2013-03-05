package cz.muni.fi.publishsubscribe.countingtree.index;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;

import java.util.Collection;
import java.util.List;

/**
 * An index - supports fast insertions and retrievals (of Constraints)
 *
 * @param <T1> The type of the values the index stores
 */
public interface Index<T1 extends Comparable<T1>> {

	public boolean addConstraint(Constraint<T1> constraint);
	public boolean removeConstraint(Constraint<T1> constraint);
	public List<Collection<Constraint<T1>>> getConstraints(T1 attributeValue);

}
