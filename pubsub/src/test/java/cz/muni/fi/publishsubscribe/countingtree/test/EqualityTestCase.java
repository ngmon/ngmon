package cz.muni.fi.publishsubscribe.countingtree.test;

import cz.muni.fi.publishsubscribe.countingtree.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EqualityTestCase {

	private static final String PROCESS_ID_ATTR = "processId";
	private static final String POSTGRE_SQL = "PostgreSQL";
	private static final String APACHE_SERVER = "Apache Server";
	private static final String APPLICATION_ATTR = "application";
	
	public static final Attribute<Long> ATTRIBUTE_1234L = new Attribute<>(PROCESS_ID_ATTR,
			new AttributeValue<>(1234L, Long.class));

	public static final Attribute<Long> ATTRIBUTE_2000L = new Attribute<>(PROCESS_ID_ATTR,
			new AttributeValue<>(2000L, Long.class));

	public static final Attribute<Long> ATTRIBUTE1000L = new Attribute<>(PROCESS_ID_ATTR,
			new AttributeValue<>(1000L, Long.class));

	public static final Attribute<String> STRING_ATTRIBUTE_FOO = new Attribute<>(APPLICATION_ATTR,
			new AttributeValue<>("foo", String.class));

	public static final Attribute<String> ATTRIBUTE_POSTGRE_SQL = new Attribute<>(APPLICATION_ATTR,
			new AttributeValue<>(POSTGRE_SQL, String.class));

	public static final Attribute<Long> LONG_ATTRIBUTE_SEVERITY = new Attribute<>("severity",
			new AttributeValue<>(1L, Long.class));

	public static final Attribute<String> ATTRIBUTE_APACHE_SERVER = new Attribute<>(APPLICATION_ATTR,
			new AttributeValue<>(APACHE_SERVER, String.class));

	public Constraint<String> STRING_CONSTRAINT_POSTGRE;

	public Constraint<String> STRING_CONSTRAINT_APACHE;
	
	public Constraint<String> STRING_CONSTRAINT_APACHE_2;

	public Constraint<Long> LONG_CONSTRAINT_1000L;

	public Constraint<Long> LONG_CONSTRAINT3000L;

	public Constraint<Long> LONG_CONSTRAINT_2000L;

	private CountingTree tree;

	private Predicate apache1000Predicate;
	private Predicate apachePredicate1;
	private Predicate apachePredicate2;
	private Predicate processId1000Predicate;
	private Predicate processId2000Predicate;
	private Predicate postgreSql3000Predicate;
	private Predicate postgreSqlPredicate;
	private Predicate apacheOrPostgreSqlPredicate;
	
	private Subscription apache1000Subscription = new Subscription();
	private Subscription apacheSubscription1  = new Subscription();
	private Subscription apacheSubscription2 = new Subscription();
	private Subscription processId1000Subscription = new Subscription();
	private Subscription processId2000Subscription = new Subscription();
	private Subscription postgreSql3000Subscription = new Subscription();
	private Subscription postgreSqlSubscription = new Subscription();
	private Subscription apacheOrPostgreSqlSubscription = new Subscription();

	@Before
	public void prepareTree() {
		prepareConstants();
		
		tree = new CountingTree();

		// Apache, 1000
		Constraint<String> apacheConstraint = STRING_CONSTRAINT_APACHE;
		Constraint<String> apacheConstraint2 = STRING_CONSTRAINT_APACHE_2;

		Constraint<Long> processId1000Constraint = LONG_CONSTRAINT_1000L;
		Filter apache1000Filter = new Filter();
		apache1000Filter.addConstraint(apacheConstraint);
		apache1000Filter.addConstraint(processId1000Constraint);
		apache1000Predicate = new Predicate();
		apache1000Predicate.addFilter(apache1000Filter);

		tree.subscribe(apache1000Predicate, apache1000Subscription);

		// Apache - two subscriptions
		Filter apacheFilter = new Filter();
		apacheFilter.addConstraint(apacheConstraint2);
		apachePredicate1 = new Predicate();
		apachePredicate1.addFilter(apacheFilter);

		tree.subscribe(apachePredicate1, apacheSubscription1);

		Filter apacheFilter2 = new Filter();
		apacheFilter2.addConstraint(apacheConstraint);
		apachePredicate2 = new Predicate();
		apachePredicate2.addFilter(apacheFilter2);

		tree.subscribe(apachePredicate2, apacheSubscription2);

		// Process ID = 1000
		Filter processId1000Filter = new Filter();
		processId1000Filter.addConstraint(processId1000Constraint);
		processId1000Predicate = new Predicate();
		processId1000Predicate.addFilter(processId1000Filter);

		tree.subscribe(processId1000Predicate, processId1000Subscription);

		// Process ID = 2000
		Constraint<Long> processId2000Constraint = LONG_CONSTRAINT_2000L;
		Filter processId2000Filter = new Filter();
		processId2000Filter.addConstraint(processId2000Constraint);
		processId2000Predicate = new Predicate();
		processId2000Predicate.addFilter(processId2000Filter);

		tree.subscribe(processId2000Predicate, processId2000Subscription);

		// PostgreSQL, 3000
		Constraint<String> postgreSqlConstraint = STRING_CONSTRAINT_POSTGRE;
		Constraint<Long> processId3000Constraint = LONG_CONSTRAINT3000L;
		Filter postgreSql3000Filter = new Filter();
		postgreSql3000Filter.addConstraint(postgreSqlConstraint);
		postgreSql3000Filter.addConstraint(processId3000Constraint);

		postgreSql3000Predicate = new Predicate();
		postgreSql3000Predicate.addFilter(postgreSql3000Filter);

		tree.subscribe(postgreSql3000Predicate, postgreSql3000Subscription);

		// PostgreSQL
		Filter postgreSqlFilter = new Filter();
		postgreSqlFilter.addConstraint(postgreSqlConstraint);
		postgreSqlPredicate = new Predicate();
		postgreSqlPredicate.addFilter(postgreSqlFilter);

		tree.subscribe(postgreSqlPredicate, postgreSqlSubscription);

		// Apache or PostgreSQL
		Filter apacheFilter3 = new Filter();
		apacheFilter3.addConstraint(apacheConstraint);
		postgreSqlFilter = new Filter();
		postgreSqlFilter.addConstraint(postgreSqlConstraint);
		apacheOrPostgreSqlPredicate = new Predicate();
		apacheOrPostgreSqlPredicate.addFilter(apacheFilter3);
		apacheOrPostgreSqlPredicate.addFilter(postgreSqlFilter);

		tree.subscribe(apacheOrPostgreSqlPredicate, apacheOrPostgreSqlSubscription);

		//tree.createIndexTable();
	}

	private void prepareConstants() {
		STRING_CONSTRAINT_POSTGRE = new Constraint<>(APPLICATION_ATTR,
				new AttributeValue<>(POSTGRE_SQL, String.class), Operator.EQUALS);

		STRING_CONSTRAINT_APACHE = new Constraint<>(APPLICATION_ATTR,
				new AttributeValue<>(APACHE_SERVER, String.class), Operator.EQUALS);
		
		STRING_CONSTRAINT_APACHE_2 = new Constraint<>(APPLICATION_ATTR,
				new AttributeValue<>(APACHE_SERVER, String.class), Operator.EQUALS);

		LONG_CONSTRAINT_1000L = new Constraint<>(PROCESS_ID_ATTR,
				new AttributeValue<>(1000L, Long.class), Operator.EQUALS);

		LONG_CONSTRAINT3000L = new Constraint<>(PROCESS_ID_ATTR,
				new AttributeValue<>(3000L, Long.class), Operator.EQUALS);

		LONG_CONSTRAINT_2000L = new Constraint<>(PROCESS_ID_ATTR,
				new AttributeValue<>(2000L, Long.class), Operator.EQUALS);
	}

	@Test
	public void testNoMatchingSubscribers() {
		Event event = new Event();
		event.addAttribute(STRING_ATTRIBUTE_FOO);
		event.addAttribute(ATTRIBUTE_1234L);
		event.addAttribute(LONG_ATTRIBUTE_SEVERITY);

		List<Subscription> predicates = tree.match(event);
		assertEquals(0, predicates.size());
	}

	@Test
	public void testApacheEvent() {
		Event event = new Event();
		event.addAttribute(ATTRIBUTE_APACHE_SERVER);
		event.addAttribute(ATTRIBUTE_1234L);

		List<Subscription> predicates = tree.match(event);
		assertEquals(3, predicates.size());
		assertTrue(predicates.contains(apacheSubscription1));
		assertTrue(predicates.contains(apacheSubscription2));
		assertTrue(predicates.contains(apacheOrPostgreSqlSubscription));
	}

	@Test
	public void testApache1000Event() {
		Event event = new Event();
		event.addAttribute(ATTRIBUTE_APACHE_SERVER);
		event.addAttribute(ATTRIBUTE1000L);

		List<Subscription> predicates = tree.match(event);
		assertEquals(5, predicates.size());
		assertTrue(predicates.contains(apache1000Subscription));
		assertTrue(predicates.contains(apacheSubscription1));
		assertTrue(predicates.contains(apacheSubscription2));
		assertTrue(predicates.contains(processId1000Subscription));
		assertTrue(predicates.contains(apacheOrPostgreSqlSubscription));
	}

	@Test
	public void testApache2000Event() {
		Event event = new Event();
		event.addAttribute(ATTRIBUTE_APACHE_SERVER);
		event.addAttribute(ATTRIBUTE_2000L);

		List<Subscription> predicates = tree.match(event);
		assertEquals(4, predicates.size());
		assertTrue(predicates.contains(apacheSubscription1));
		assertTrue(predicates.contains(apacheSubscription2));
		assertTrue(predicates.contains(processId2000Subscription));
		assertTrue(predicates.contains(apacheOrPostgreSqlSubscription));
	}

	@Test
	public void testPostgreSqlEvent() {
		Event event = new Event();
		event.addAttribute(ATTRIBUTE_POSTGRE_SQL);
		event.addAttribute(ATTRIBUTE_2000L);

		List<Subscription> predicates = tree.match(event);
		assertEquals(3, predicates.size());
		assertTrue(predicates.contains(processId2000Subscription));
		assertTrue(predicates.contains(postgreSqlSubscription));
		assertTrue(predicates.contains(apacheOrPostgreSqlSubscription));
	}

	@Test
	public void testProcessId1000Event() {
		Event event = new Event();
		event.addAttribute(ATTRIBUTE1000L);

		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(processId1000Subscription));

	}
}
