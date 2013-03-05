package cz.muni.fi.publishsubscribe.countingtree.index.type;

import java.util.Date;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.DateRange;
import cz.muni.fi.publishsubscribe.countingtree.LongRange;

/**
 * Creates a TypeIndex based on a type (class)
 */
public class TypeIndexFactory {

	public static <T1 extends Comparable<T1>, T2 extends Constraint<T1>> TypeIndex<T1> getTypeIndex(Class<T1> type) {

		if (type == String.class) {
			return (TypeIndex<T1>) new StringIndex();
		} else if (type == Long.class) {
			return (TypeIndex<T1>) new LongIndex();
		} else if (type == LongRange.class) {
			return (TypeIndex<T1>) new LongIndex();
		} else if (type == Date.class) {
			return (TypeIndex<T1>) new DateIndex();
		} else if (type == DateRange.class) {
			return (TypeIndex<T1>) new DateIndex();
		} else {
			throw new IllegalArgumentException(String.format("Index for type %s is not supported", type.getClass().getName()));
		}

	}
}
