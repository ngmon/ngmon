package cz.muni.fi.publishsubscribe.countingtree.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import cz.muni.fi.publishsubscribe.countingtree.Attribute;
import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.CountingTree;
import cz.muni.fi.publishsubscribe.countingtree.Event;
import cz.muni.fi.publishsubscribe.countingtree.Filter;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.publishsubscribe.countingtree.Subscription;

public class VariousTestCase {

	private static class FooClass implements Comparable<FooClass> {

		@Override
		public int compareTo(FooClass arg0) {
			return 0;
		}

	}

	@Test
	public void testEmptyTree() {
		CountingTree tree = new CountingTree();

		Event event = new Event();
		event.addAttribute(new Attribute<>("app", new AttributeValue<>("foo",
				String.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(0, predicates.size());
	}

	@Test
	public void testUnsubscribeFromEmptyTree() {
		CountingTree tree = new CountingTree();

		Subscription subscriptionFoo = new Subscription();
		subscriptionFoo.setId(1000000L);

		assertFalse(tree.unsubscribe(subscriptionFoo));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnsupportedOperator() {
		@SuppressWarnings("unused")
		Constraint<String> illegalConstraint = new Constraint<>("foo",
				new AttributeValue<>("foo", String.class), Operator.RANGE);
	}

	@Test
	public void unsubscribeSubscriptionWithNullId() {
		CountingTree tree = new CountingTree();

		Subscription subscription = new Subscription();
		subscription.setId(null);

		assertFalse(tree.unsubscribe(subscription));
	}

	@Test
	public void removeConstraintFromEqualsIndexTwice() {
		CountingTree tree = new CountingTree();

		Constraint<String> constraint = new Constraint<>("foo",
				new AttributeValue<>("foo", String.class), Operator.EQUALS);
		Filter filter = new Filter();
		filter.addConstraint(constraint);
		Predicate predicate = new Predicate();
		predicate.addFilter(filter);

		long subscriptionId = tree.subscribe(predicate, new Subscription());
		assertTrue(tree.unsubscribe(subscriptionId));
		assertFalse(tree.unsubscribe(subscriptionId));
	}

}
