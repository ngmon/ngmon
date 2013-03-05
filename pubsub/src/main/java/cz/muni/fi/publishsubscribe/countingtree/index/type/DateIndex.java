package cz.muni.fi.publishsubscribe.countingtree.index.type;

import java.util.Date;

import cz.muni.fi.publishsubscribe.countingtree.Operator;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.GreaterThanIndex;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.GreaterThanOrEqualToIndex;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.LessThanIndex;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.LessThanOrEqualToIndex;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.RangeIndex;

public class DateIndex extends AbstractTypeIndex<Date> {

	public DateIndex() {
		super();
		
		this.addOperatorIndex(Operator.LESS_THAN, new LessThanIndex<Date>());
		this.addOperatorIndex(Operator.LESS_THAN_OR_EQUAL_TO, new LessThanOrEqualToIndex<Date>());
		this.addOperatorIndex(Operator.GREATER_THAN, new GreaterThanIndex<Date>());
		this.addOperatorIndex(Operator.GREATER_THAN_OR_EQUAL_TO, new GreaterThanOrEqualToIndex<Date>());
		this.addOperatorIndex(Operator.RANGE, new RangeIndex<Date>());
	}
}
