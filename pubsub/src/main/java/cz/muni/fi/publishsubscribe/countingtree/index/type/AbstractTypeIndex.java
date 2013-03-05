package cz.muni.fi.publishsubscribe.countingtree.index.type;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
import cz.muni.fi.publishsubscribe.countingtree.index.operator.OperatorIndex;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTypeIndex<T1 extends Comparable<T1>> implements
		TypeIndex<T1> {

	private final Map<Operator, OperatorIndex<T1>> operatorIndexes;

	public AbstractTypeIndex() {
		this.operatorIndexes = new EnumMap<>(Operator.class);
	}

	@Override
	public List<Collection<Constraint<T1>>> getConstraints(T1 attributeValue) {
		List<Collection<Constraint<T1>>> constraints = new LinkedList<>();

		for (OperatorIndex<T1> operatorIndex : this.operatorIndexes.values()) {
			constraints.addAll(operatorIndex.getConstraints(attributeValue));
		}

		return constraints;
	}

	@Override
	public boolean addConstraint(Constraint<T1> constraint) {

		return this.operatorIndexes.get(constraint.getOperator())
				.addConstraint(constraint);
	}

	@Override
	public boolean removeConstraint(Constraint<T1> constraint) {

		return this.operatorIndexes.get(constraint.getOperator())
				.removeConstraint(constraint);
	}

	public final boolean addOperatorIndex(Operator operator,
			OperatorIndex<T1> operatorIndex) {

		if (this.operatorIndexes.containsKey(operator)) {
			return false;
		}

		this.operatorIndexes.put(operator, operatorIndex);

		return true;
	}

}
