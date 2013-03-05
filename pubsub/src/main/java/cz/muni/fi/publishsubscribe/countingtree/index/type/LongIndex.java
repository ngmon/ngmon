package cz.muni.fi.publishsubscribe.countingtree.index.type;

import cz.muni.fi.publishsubscribe.countingtree.Operator;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.*;

public class LongIndex extends AbstractTypeIndex<Long> {

	public LongIndex() {
		super();

		this.addOperatorIndex(Operator.EQUALS, new EqualsIndex<Long>());
		this.addOperatorIndex(Operator.LESS_THAN, new LessThanIndex<Long>());
		this.addOperatorIndex(Operator.LESS_THAN_OR_EQUAL_TO, new LessThanOrEqualToIndex<Long>());
		this.addOperatorIndex(Operator.GREATER_THAN, new GreaterThanIndex<Long>());
		this.addOperatorIndex(Operator.GREATER_THAN_OR_EQUAL_TO, new GreaterThanOrEqualToIndex<Long>());
		this.addOperatorIndex(Operator.RANGE, new RangeIndex<Long>());
	}
}
