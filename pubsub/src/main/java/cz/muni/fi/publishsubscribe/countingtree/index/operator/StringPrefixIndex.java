package cz.muni.fi.publishsubscribe.countingtree.index.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.ternarysearchtree.TernarySearchTree;

/**
 * Index for String prefixes (PREFIX Operator).
 * Uses TernarySearchTree for fast lookups
 */
public class StringPrefixIndex implements OperatorIndex<String> {

	protected TernarySearchTree<Constraint<String>> tree = new TernarySearchTree<>();

	@Override
	public boolean addConstraint(Constraint<String> constraint) {
		String value = constraint.getAttributeValue().getValue();
		return tree.put(value, constraint) != null;
	}

	@Override
	public boolean removeConstraint(Constraint<String> constraint) {
		return (tree.remove(constraint.getAttributeValue().getValue()) != null);
	}

	@Override
	public List<Collection<Constraint<String>>> getConstraints(String attributeValue) {
		List<Constraint<String>> allPrefixes = tree.getAllPrefixes(attributeValue);
		List<Collection<Constraint<String>>> list = new ArrayList<>();
		list.add(allPrefixes);
		
		return list;
	}

}
