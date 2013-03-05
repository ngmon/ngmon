package cz.muni.fi.publishsubscribe.countingtree.index.operator;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.Range;
import cz.muni.fi.publishsubscribe.countingtree.RangeTree;

import java.util.*;

/**
 * Index for Ranges (RANGE Operator). Uses RangeTree for fast lookups
 *
 * @param <T1> The type of the values the index stores
 */
public class RangeIndex<T1 extends Comparable<T1>> implements OperatorIndex<T1> {

	private RangeTree<T1> rangeTree = new RangeTree<>();
	private Map<Range<T1>, Constraint<T1>> constraintLookup = new HashMap<>();

	@Override
	public boolean addConstraint(Constraint<T1> constraint) {
		Range<T1> range = (Range<T1>) constraint.getAttributeValue().getValue();

		this.rangeTree.addRange(range);
		this.constraintLookup.put(range, constraint);

		return true;
	}

	@Override
	public boolean removeConstraint(Constraint<T1> constraint) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<Collection<Constraint<T1>>> getConstraints(T1 attributeValue) {

		Set<Range<T1>> ranges = this.rangeTree.getRangesContaining(attributeValue);

		if (ranges.isEmpty()) {
			return Collections.emptyList();
		}

		List<Constraint<T1>> constraints = new ArrayList<>();

		for (Range<T1> range : ranges) {
			constraints.add(this.constraintLookup.get(range));
		}
		
		List<Collection<Constraint<T1>>> list = new ArrayList<>();
		list.add(constraints);

		return list;
	}
}
