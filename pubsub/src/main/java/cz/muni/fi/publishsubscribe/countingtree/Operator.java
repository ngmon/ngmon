package cz.muni.fi.publishsubscribe.countingtree;

import java.util.*;

/**
 * Represents all possible operators (used in Constraints)
 * and stores info about the operators supported by specific classes (like Long)
 */
public enum Operator {
	EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO, RANGE, PREFIX;

	private static Map<Class, Set<Operator>> operatorMap = new HashMap<>(2);

	static {
		Set<Operator> stringOperators = new HashSet<>();
		stringOperators.add(EQUALS);
		stringOperators.add(PREFIX);

		Set<Operator> longOperators = new HashSet<>();
		longOperators.add(EQUALS);
		longOperators.add(LESS_THAN);
		longOperators.add(LESS_THAN_OR_EQUAL_TO);
		longOperators.add(GREATER_THAN);
		longOperators.add(GREATER_THAN_OR_EQUAL_TO);
		
		Set<Operator> dateOperators = new HashSet<>();
		dateOperators.add(LESS_THAN);
		dateOperators.add(LESS_THAN_OR_EQUAL_TO);
		dateOperators.add(GREATER_THAN);
		dateOperators.add(GREATER_THAN_OR_EQUAL_TO);

		operatorMap.put(String.class, stringOperators);
		operatorMap.put(Long.class, longOperators);
		operatorMap.put(Date.class, dateOperators);

		Set<Operator> comparableOperators = new HashSet<>();
		comparableOperators.add(RANGE);

		// Hack
		operatorMap.put(LongRange.class, comparableOperators);
		operatorMap.put(DateRange.class, comparableOperators);

	}

	public static Set<Operator> getSupportedOperators(Class type) {

		if (!operatorMap.containsKey(type)) {
			return Collections.emptySet();
		}

		return operatorMap.get(type);
	}
}