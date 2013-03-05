package cz.muni.fi.publishsubscribe.countingtree.index.type;

import cz.muni.fi.publishsubscribe.countingtree.Operator;
import cz.muni.fi.publishsubscribe.countingtree.index.Index;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.OperatorIndex;

/**
 * Index for storing values of a particular type
 * Also provides a method for associating an Operator (along with the type)
 * with a class providing the index implementation
 *
 * @param <T1> The type of the values the index stores
 */
public interface TypeIndex<T1 extends Comparable<T1>> extends Index<T1> {

	public boolean addOperatorIndex(Operator operator, OperatorIndex<T1> operatorIndex);
}
