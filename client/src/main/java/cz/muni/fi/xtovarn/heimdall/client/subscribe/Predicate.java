package cz.muni.fi.xtovarn.heimdall.client.subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Predicate {

	private List<Constraint> constraints = new ArrayList<>();

	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
	}

	public void removeConstraints() {
		constraints.clear();
	}

	public Map<String, String> toStringMap() {
		Map<String, String> map = new HashMap<>();

		for (Constraint constraint : constraints) {
			map.put(constraint.getAttributeName(), constraint.getOperatorAndValueString());
		}

		return map;
	}

	public boolean isEmpty() {
		return constraints.isEmpty();
	}

}
