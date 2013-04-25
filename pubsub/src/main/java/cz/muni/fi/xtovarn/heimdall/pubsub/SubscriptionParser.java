package cz.muni.fi.xtovarn.heimdall.pubsub;

import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;

import cz.muni.fi.langparser.LangParser;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.Filter;
import cz.muni.fi.publishsubscribe.countingtree.Predicate;

/**
 * Converts the predicate represented as a map to an object (of type Predicate)
 */
public class SubscriptionParser {

	/**
	 * Converts the predicate represented as a map to an object (of type
	 * Predicate)
	 * 
	 * @param map
	 *            A map representing the predicate - from attribute names to
	 *            operator and value pairs
	 * @return The corresponding Predicate object
	 * @throws ParseException
	 *             If the parsing failed (for example because a string with
	 *             operator and value pair has invalid format)
	 */
	public static Predicate parseSubscription(Map<String, String> map) throws IndexOutOfBoundsException, ParseException {
		Filter filter = new Filter();

		// iterate through all constraints (represented as map entries)
		for (Entry<String, String> entry : map.entrySet()) {
			LangParser parser = new LangParser(entry.getKey(), entry.getValue());
			Constraint<?> constraint = parser.parse();
			filter.addConstraint(constraint);
		}

		Predicate predicate = new Predicate();
		predicate.addFilter(filter);

		return predicate;
	}

}
