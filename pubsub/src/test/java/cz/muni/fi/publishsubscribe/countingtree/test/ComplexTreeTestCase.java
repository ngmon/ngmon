package cz.muni.fi.publishsubscribe.countingtree.test;

import cz.muni.fi.publishsubscribe.countingtree.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComplexTreeTestCase {

	private static final String PROCESS_ID_ATTR = "processId";
	private static final String APPLICATION_ATTR = "application";

	private static final String APACHE_SERVER = "Apache Server";
	private static final String POSTGRE_SQL = "PostgreSQL";

	private CountingTree tree;

	private Predicate predicate01;
	private Predicate predicate02;
	private Predicate predicate03;
	private Predicate predicate04;
	
	private Subscription subscription01 = new Subscription();
	private Subscription subscription02 = new Subscription();
	private Subscription subscription03 = new Subscription();
	private Subscription subscription04 = new Subscription();

	@Before
	public void prepareTree() {
		tree = new CountingTree();

		// Apache, processId < 1000
		Constraint<String> apacheConstraint = new Constraint<>(
				APPLICATION_ATTR, new AttributeValue<>(APACHE_SERVER,
				String.class), Operator.EQUALS);

		Constraint<Long> pIdLessThan1000 = new Constraint<>(PROCESS_ID_ATTR,
				new AttributeValue<>(1000L, Long.class), Operator.LESS_THAN);

		Filter filter01 = new Filter();
		filter01.addConstraint(apacheConstraint);
		filter01.addConstraint(pIdLessThan1000);

		predicate01 = new Predicate();
		predicate01.addFilter(filter01);

		tree.subscribe(predicate01, subscription01);

		// PostgreSQL, processId >= 1000
		Constraint<String> postgreSqlConstraint = new Constraint<String>(
				APPLICATION_ATTR, new AttributeValue<String>(POSTGRE_SQL,
				String.class), Operator.EQUALS);

		Constraint<Long> pIdGreaterThanOrEqual1000 = new Constraint<>(
				PROCESS_ID_ATTR, new AttributeValue<>(1000L, Long.class),
				Operator.GREATER_THAN_OR_EQUAL_TO);

		Filter filter02 = new Filter();
		filter02.addConstraint(postgreSqlConstraint);
		filter02.addConstraint(pIdGreaterThanOrEqual1000);

		predicate02 = new Predicate();
		predicate02.addFilter(filter02);

		tree.subscribe(predicate02, subscription02);

		// processId > 2000
		Constraint<Long> pIdGreaterThan2000 = new Constraint<>(
				PROCESS_ID_ATTR, new AttributeValue<>(2000L, Long.class),
				Operator.GREATER_THAN);

		Filter filter03 = new Filter();
		filter03.addConstraint(pIdGreaterThan2000);

		predicate03 = new Predicate();
		predicate03.addFilter(filter03);

		tree.subscribe(predicate03, subscription03);

		// Apache OR PostgreSQL, processId >= 2000
		Filter apacheFilter = new Filter();
		apacheFilter.addConstraint(apacheConstraint);

		Constraint<Long> pIdGreaterThanOrEqual2000 = new Constraint<>(
				PROCESS_ID_ATTR, new AttributeValue<>(2000L, Long.class),
				Operator.GREATER_THAN_OR_EQUAL_TO);

		Filter filter04 = new Filter();
		filter04.addConstraint(postgreSqlConstraint);
		filter04.addConstraint(pIdGreaterThanOrEqual2000);

		predicate04 = new Predicate();
		predicate04.addFilter(apacheFilter);
		predicate04.addFilter(filter04);

		tree.subscribe(predicate04, subscription04);

		//tree.createIndexTable();
	}

	@Test
	public void testNoMatchingSubscribers() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>("foo", String.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(0, predicates.size());
	}

	@Test
	public void testApacheEvent() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>(APACHE_SERVER, String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(1234L, Long.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(subscription04));
	}

	@Test
	public void testApache1000Event() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>(APACHE_SERVER, String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(1000L, Long.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(subscription04));
	}

	@Test
	public void testApache999Event() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>(APACHE_SERVER, String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(999L, Long.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(2, predicates.size());
		assertTrue(predicates.contains(subscription01));
		assertTrue(predicates.contains(subscription04));
	}

	@Test
	public void testFoo2000Event() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>("foo", String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(2000L, Long.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(0, predicates.size());
	}

	@Test
	public void testFoo3000Event() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>("foo", String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(3000L, Long.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(subscription03));
	}

	@Test
	public void postgreSql3000Event() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>(POSTGRE_SQL, String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(3000L, Long.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(3, predicates.size());
		assertTrue(predicates.contains(subscription02));
		assertTrue(predicates.contains(subscription03));
		assertTrue(predicates.contains(subscription04));
	}

	@Test
	public void postgreSql1000Event() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>(POSTGRE_SQL, String.class)));
		event.addAttribute(new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(1000L, Long.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(subscription02));
	}

	@Test
	public void testApache3000Event() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(APPLICATION_ATTR,
				new AttributeValue<>(APACHE_SERVER, String.class)));
		Attribute<Long> attribute = new Attribute<>(PROCESS_ID_ATTR,
				new AttributeValue<>(3000L, Long.class));
		event.addAttribute(attribute);

		List<Subscription> predicates = tree.match(event);
		assertEquals(2, predicates.size());
		assertTrue(predicates.contains(subscription03));
		assertTrue(predicates.contains(subscription04));
	}
}
