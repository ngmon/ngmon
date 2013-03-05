package cz.muni.fi.publishsubscribe.countingtree.test;

import static org.junit.Assert.assertEquals;

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
import cz.muni.fi.publishsubscribe.countingtree.DateRange;
import cz.muni.fi.publishsubscribe.countingtree.Event;
import cz.muni.fi.publishsubscribe.countingtree.Filter;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
import cz.muni.fi.publishsubscribe.countingtree.Predicate;
import cz.muni.fi.publishsubscribe.countingtree.Subscription;

public class DateRangeTestCase {

	private static final String STRING_ATTRIBUTE_NAME = "stringAttribute";
	private static final String DATE_ATTRIBUTE_NAME = "dateAttribute";

	public static final Attribute<String> STRING_ATTRIBUTE = new Attribute<>(
			STRING_ATTRIBUTE_NAME, new AttributeValue<>("foo", String.class));

	private Constraint<DateRange> rangeConstraint;

	private CountingTree tree;

	@Before
	public void prepareTree() {
		this.tree = new CountingTree();

		Calendar calendarStart = new GregorianCalendar();
		calendarStart.set(2000, 1, 1, 12, 0, 0);
		Calendar calendarEnd = new GregorianCalendar();
		calendarEnd.set(2001, 5, 5, 18, 20, 0);
		rangeConstraint = new Constraint<>(DATE_ATTRIBUTE_NAME,
				new AttributeValue<>(new DateRange(calendarStart.getTime(),
						calendarEnd.getTime()), DateRange.class),
				Operator.RANGE);

		Filter filter = new Filter();
		filter.addConstraint(rangeConstraint);

		Predicate predicate = new Predicate();
		predicate.addFilter(filter);

		this.tree.subscribe(predicate, new Subscription());
	}

	@Test
	public void testMatchRange() {
		Event event = new Event();
		event.addAttribute(STRING_ATTRIBUTE);
		Calendar cal = new GregorianCalendar();
		cal.set(2001, 4, 1, 1, 2, 3);
		event.addAttribute(new Attribute<>(DATE_ATTRIBUTE_NAME,
				new AttributeValue<>(cal.getTime(), Date.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(1, predicates.size());
	}

	@Test
	public void testNoMatchRange() {
		Event event = new Event();
		event.addAttribute(STRING_ATTRIBUTE);
		Calendar cal = new GregorianCalendar();
		cal.set(2001, 5, 5, 18, 20, 1);
		event.addAttribute(new Attribute<>(DATE_ATTRIBUTE_NAME,
				new AttributeValue<>(cal.getTime(), Date.class)));

		List<Subscription> predicates = tree.match(event);
		assertEquals(0, predicates.size());
	}

}
