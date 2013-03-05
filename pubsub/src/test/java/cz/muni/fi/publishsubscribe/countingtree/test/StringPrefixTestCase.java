package cz.muni.fi.publishsubscribe.countingtree.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
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

public class StringPrefixTestCase {

	private static final String PROCESS_ID_ATTR = "processId";
	private static final String PACKAGE_ATTR = "package";

	private CountingTree tree;

	Constraint<Long> pIdLessThan1000;
	Constraint<String> comPrefix;
	Constraint<String> comJavaPrefix;
	Constraint<String> netPrefix;

	Filter filterComPidLessThan1000;
	Predicate predicateComPidLessThan1000;
	Subscription subscriptionComPidLessThan1000 = new Subscription();

	Filter filterComJava;
	Predicate predicateComJava;
	Subscription subscriptionComJava = new Subscription();

	Filter filterNet;
	Predicate predicateNet;
	Subscription subscriptionNet = new Subscription();

	@Before
	public void prepareTree() {
		tree = new CountingTree();

		pIdLessThan1000 = new Constraint<>(PROCESS_ID_ATTR,
				new AttributeValue<>(1000L, Long.class), Operator.LESS_THAN);

		comPrefix = new Constraint<>(PACKAGE_ATTR, new AttributeValue<>("com",
				String.class), Operator.PREFIX);
		comJavaPrefix = new Constraint<>(PACKAGE_ATTR, new AttributeValue<>(
				"com.java", String.class), Operator.PREFIX);
		netPrefix = new Constraint<>(PACKAGE_ATTR, new AttributeValue<>("net",
				String.class), Operator.PREFIX);

		// first predicate - "com" prefix + processId < 1000

		filterComPidLessThan1000 = new Filter();
		filterComPidLessThan1000.addConstraint(comPrefix);
		filterComPidLessThan1000.addConstraint(pIdLessThan1000);

		predicateComPidLessThan1000 = new Predicate();
		predicateComPidLessThan1000.addFilter(filterComPidLessThan1000);

		tree.subscribe(predicateComPidLessThan1000, subscriptionComPidLessThan1000);

		// second predicate - "com.java" prefix

		filterComJava = new Filter();
		filterComJava.addConstraint(comJavaPrefix);

		predicateComJava = new Predicate();
		predicateComJava.addFilter(filterComJava);

		tree.subscribe(predicateComJava, subscriptionComJava);

		// third predicate - "net" prefix

		filterNet = new Filter();
		filterNet.addConstraint(netPrefix);

		predicateNet = new Predicate();
		predicateNet.addFilter(filterNet);

		tree.subscribe(predicateNet, subscriptionNet);
	}

	@Test
	public void testNoMatchingSubscribers() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(PACKAGE_ATTR, new AttributeValue<>(
				"foo", String.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(0, predicates.size());
	}

	@Test
	public void testNoMatchingSubscribers2() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(PACKAGE_ATTR, new AttributeValue<>(
				"com", String.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(0, predicates.size());
	}

	@Test
	public void testComJavaPrefix() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(PACKAGE_ATTR, new AttributeValue<>(
				"com.java", String.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(subscriptionComJava));
	}

	@Test
	public void testComJavaGlassfishPid500() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(PACKAGE_ATTR, new AttributeValue<>(
				"com.java.glassfish", String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(500L, Long.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(2, predicates.size());
		assertTrue(predicates.contains(subscriptionComPidLessThan1000));
		assertTrue(predicates.contains(subscriptionComJava));
	}
	
	@Test
	public void testComPid500() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(PACKAGE_ATTR, new AttributeValue<>(
				"com", String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(500L, Long.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(subscriptionComPidLessThan1000));
	}
	
	@Test
	public void testNetPrefix() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(PACKAGE_ATTR, new AttributeValue<>(
				"net.foo", String.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(subscriptionNet));
	}

}
