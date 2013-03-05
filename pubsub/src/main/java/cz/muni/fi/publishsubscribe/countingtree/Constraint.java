package cz.muni.fi.publishsubscribe.countingtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An elementary rule which can be either true or false for a specific event
 * Example: attributeName is "port", operator is "less than or equal to" and
 * attributeValue is "1000L" (We can also say the Constraint either does or does
 * not satisfy an event)
 */
public class Constraint<T1 extends Comparable<T1>> {

	private String attributeName;
	private Operator operator;
	private AttributeValue<T1> attributeValue;

	private List<Filter> filters = new ArrayList<>();

	private Integer cachedHashCode = null;

	public Constraint(String attributeName, AttributeValue<T1> attributeValue,
			Operator operator) {

		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
		this.operator = operator;

		if (!Operator.getSupportedOperators(attributeValue.getType()).contains(
				operator)) {
			throw new IllegalArgumentException(String.format(
					"Unsupported Operator: %s for Class: %s",
					operator.toString(), attributeValue.getType()));
		}
	}

	public String getAttributeName() {
		return attributeName;
	}

	public AttributeValue<T1> getAttributeValue() {
		return attributeValue;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Constraint<?> that = (Constraint<?>) o;

		if (!attributeName.equals(that.attributeName))
			return false;
		if (operator != that.operator)
			return false;
		if (!attributeValue.equals(that.attributeValue))
			return false;

		return true;
	}

	public int computeHashCode() {
		int result = attributeName.hashCode();
		result = 31 * result + attributeValue.hashCode();
		result = 31 * result + operator.hashCode();
		return result;
	}

	@Override
	public int hashCode() {
		if (cachedHashCode == null)
			cachedHashCode = computeHashCode();
		return cachedHashCode;
	}

	public void addFilter(Filter filter) {
		filters.add(filter);
	}

	public void removeFilter(Filter filter) {
		filters.remove(filter);
	}

	public boolean noFilters() {
		return filters.isEmpty();
	}

	public List<Filter> getFilters() {
		return filters;
	}

}
