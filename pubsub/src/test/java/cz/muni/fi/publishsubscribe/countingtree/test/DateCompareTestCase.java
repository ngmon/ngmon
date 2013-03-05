package cz.muni.fi.publishsubscribe.countingtree.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

public class DateCompareTestCase {

	private static final String ATTRIBUTE_1 = "attribute1";

	private Predicate lessThan2000_01_01_20_00_00;
	private Predicate lessThanOrEqualTo2000_01_01_20_00_00;
	private Predicate greaterThan1990_01_01_20_00_00;
	private Predicate greaterThanOrEqualTo2010_01_01_20_00_00;
	
	private Subscription lessThan2000_01_01_20_00_00S = new Subscription();
	private Subscription lessThanOrEqualTo2000_01_01_20_00_00S = new Subscription();
	private Subscription greaterThan1990_01_01_20_00_00S = new Subscription();
	private Subscription greaterThanOrEqualTo2010_01_01_20_00_00S = new Subscription();

	private CountingTree tree;

	private Date getDate(int year, int month, int date, int hourOfDay,
			int minute, int second) {
		Calendar calendar = new GregorianCalendar();
		calendar.set(year, month, date, hourOfDay, minute, second);
		return calendar.getTime();
	}

	@Before
	public void prepareTree() {
		tree = new CountingTree();

		Constraint<Date> constraint1 = new Constraint<>(
				ATTRIBUTE_1,
				new AttributeValue<>(getDate(2000, 1, 1, 20, 0, 0), Date.class),
				Operator.LESS_THAN);
		Filter filter01 = new Filter();
		filter01.addConstraint(constraint1);

		lessThan2000_01_01_20_00_00 = new Predicate();
		lessThan2000_01_01_20_00_00.addFilter(filter01);

		tree.subscribe(lessThan2000_01_01_20_00_00, lessThan2000_01_01_20_00_00S);

		Constraint<Date> constraint2 = new Constraint<>(
				ATTRIBUTE_1,
				new AttributeValue<>(getDate(2000, 1, 1, 20, 0, 0), Date.class),
				Operator.LESS_THAN_OR_EQUAL_TO);
		Filter filter02 = new Filter();
		filter02.addConstraint(constraint2);

		lessThanOrEqualTo2000_01_01_20_00_00 = new Predicate();
		lessThanOrEqualTo2000_01_01_20_00_00.addFilter(filter02);

		tree.subscribe(lessThanOrEqualTo2000_01_01_20_00_00, lessThanOrEqualTo2000_01_01_20_00_00S);

		Constraint<Date> constraint3 = new Constraint<>(
				ATTRIBUTE_1,
				new AttributeValue<>(getDate(1990, 1, 1, 20, 0, 0), Date.class),
				Operator.GREATER_THAN);
		Filter filter03 = new Filter();
		filter03.addConstraint(constraint3);

		greaterThan1990_01_01_20_00_00 = new Predicate();
		greaterThan1990_01_01_20_00_00.addFilter(filter03);

		tree.subscribe(greaterThan1990_01_01_20_00_00, greaterThan1990_01_01_20_00_00S);

		Constraint<Date> constraint4 = new Constraint<>(
				ATTRIBUTE_1,
				new AttributeValue<>(getDate(2010, 1, 1, 20, 0, 0), Date.class),
				Operator.GREATER_THAN_OR_EQUAL_TO);
		Filter filter04 = new Filter();
		filter04.addConstraint(constraint4);

		greaterThanOrEqualTo2010_01_01_20_00_00 = new Predicate();
		greaterThanOrEqualTo2010_01_01_20_00_00.addFilter(filter04);

		tree.subscribe(greaterThanOrEqualTo2010_01_01_20_00_00, greaterThanOrEqualTo2010_01_01_20_00_00S);
	}

	@Test
	public void test2000_01_01_20_00_00() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(ATTRIBUTE_1, new AttributeValue<>(
				getDate(2000, 1, 1, 20, 0, 0), Date.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(2, predicates.size());
		assertTrue(predicates.contains(lessThanOrEqualTo2000_01_01_20_00_00S));
		assertTrue(predicates.contains(greaterThan1990_01_01_20_00_00S));
	}
	
	@Test
	public void test1980_01_01_20_00_00() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(ATTRIBUTE_1, new AttributeValue<>(
				getDate(1980, 1, 1, 20, 0, 0), Date.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(2, predicates.size());
		assertTrue(predicates.contains(lessThan2000_01_01_20_00_00S));
		assertTrue(predicates.contains(lessThanOrEqualTo2000_01_01_20_00_00S));
	}
	
	@Test
	public void test1995_01_01_20_00_00() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(ATTRIBUTE_1, new AttributeValue<>(
				getDate(1995, 1, 1, 20, 0, 0), Date.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(3, predicates.size());
		assertTrue(predicates.contains(lessThan2000_01_01_20_00_00S));
		assertTrue(predicates.contains(lessThanOrEqualTo2000_01_01_20_00_00S));
		assertTrue(predicates.contains(greaterThan1990_01_01_20_00_00S));
	}
	
	@Test
	public void test2005_01_01_20_00_00() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(ATTRIBUTE_1, new AttributeValue<>(
				getDate(2005, 1, 1, 20, 0, 0), Date.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
		assertTrue(predicates.contains(greaterThan1990_01_01_20_00_00S));
	}
	
	@Test
	public void test2015_01_01_20_00_00() {
		Event event = new Event();
		event.addAttribute(new Attribute<>(ATTRIBUTE_1, new AttributeValue<>(
				getDate(2015, 1, 1, 20, 0, 0), Date.class)));
		
		List<Subscription> predicates = tree.match(event);
		assertEquals(2, predicates.size());
		assertTrue(predicates.contains(greaterThan1990_01_01_20_00_00S));
		assertTrue(predicates.contains(greaterThanOrEqualTo2010_01_01_20_00_00S));
	}

}
