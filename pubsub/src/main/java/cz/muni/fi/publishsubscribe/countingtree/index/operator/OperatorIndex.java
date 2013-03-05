package cz.muni.fi.publishsubscribe.countingtree.index.operator;

import cz.muni.fi.publishsubscribe.countingtree.index.Index;

/**
 * Index for a specific operator (like EQUALS, GREATER_THAN_OR_EQUAL_TO etc.)
 *
 * @param <T1> The type of the values the index stores
 */
public interface OperatorIndex<T1 extends Comparable<T1>> extends Index<T1> {

}
