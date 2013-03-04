package cz.muni.fi.xtovarn.heimdall.pubsub;

import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;

import cz.muni.fi.langparser.LangParser;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.Filter;
import cz.muni.fi.publishsubscribe.countingtree.Predicate;

public class SubscriptionParser {

	public static Predicate parseSubscription(Map<String, String> map)
			throws IndexOutOfBoundsException, ParseException {
		Filter filter = new Filter();

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
