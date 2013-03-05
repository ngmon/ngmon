package cz.muni.fi.publishsubscribe.countingtree.index;

import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.index.type.TypeIndex;
import cz.muni.fi.publishsubscribe.countingtree.index.type.TypeIndexFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The front-end class for storing Constraints in indices
 * Also returns Constraints satisfying a specific attribute object (name + value)
 */
public class AttributeIndex {

	/** A map from attribute names to TypeIndex which stores the relevant values */
	private final Map<String, TypeIndex<? extends Comparable<?>>> attributes = new HashMap<>(10);
	private Map<Constraint<?>, Integer> constraintCounter = new HashMap<>();

	public <T1 extends Comparable<T1>, T2 extends Constraint<T1>> boolean addConstraint(T2 constraint) {

		String attributeName = constraint.getAttributeName();
		AttributeValue<T1> attributeValue = constraint.getAttributeValue();

		TypeIndex<T1> typeIndex = (TypeIndex<T1>) this.attributes.get(attributeName);

		if (typeIndex != null) {

			typeIndex.addConstraint(constraint);

		} else {

			typeIndex = TypeIndexFactory.getTypeIndex(attributeValue.getType());

			typeIndex.addConstraint(constraint);
			this.attributes.put(attributeName, typeIndex);
		}

		incrementConstraintCounter(constraint);

		return true;
	}

	private <T1 extends Comparable<T1>, T2 extends Constraint<T1>> void incrementConstraintCounter(T2 constraint) {
		Integer count = constraintCounter.get(constraint);
		if (count == null)
			constraintCounter.put(constraint, 1);
		else
			constraintCounter.put(constraint, count + 1);
	}

	/**
	 * Removes a constraint only if removeConstraint() has been called so many
	 * times as addConstraint() (for the specific Constraint) (in other words,
	 * if the associated counter is 1)
	 * 
	 * @return true if the Constraint is found, false otherwise
	 */
	public <T1 extends Comparable<T1>, T2 extends Constraint<T1>> boolean removeConstraint(T2 constraint) {
		Integer count = constraintCounter.get(constraint);
		if (count == null)
			return false;

		String attributeName = constraint.getAttributeName();
		TypeIndex<T1> typeIndex = (TypeIndex<T1>) this.attributes.get(attributeName);
		// no index for the attribute
		if (typeIndex == null)
			return false;

		count--;

		if (count == 0) {
			typeIndex.removeConstraint(constraint);
			constraintCounter.remove(constraint);
		}
		else
			constraintCounter.put(constraint, count);

		return true;

	}

	public <T1 extends Comparable<T1>, T2 extends Constraint<T1>> List<Collection<Constraint<T1>>> getConstraints(String attributeName, AttributeValue<T1> attributeValue) {

		TypeIndex<T1> typeIndex = (TypeIndex<T1>) this.attributes.get(attributeName);

		if (typeIndex == null) {
			return Collections.emptyList();
		}

		return (List<Collection<Constraint<T1>>>) typeIndex.getConstraints(attributeValue.getValue());
	}
}
