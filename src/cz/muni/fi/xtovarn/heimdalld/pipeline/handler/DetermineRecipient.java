package cz.muni.fi.xtovarn.heimdalld.pipeline.handler;

import cz.muni.fi.xtovarn.heimdalld.db.entity.Event;
import cz.muni.fi.xtovarn.heimdalld.dispatcher.Subscription;

import java.util.HashSet;
import java.util.Set;

public class DetermineRecipient implements Handler {

	@Override
	public Object handle(Object o) {
		Set<String> recipients = new HashSet<String>(1);
		recipients.add("xdanos@1");

		return new Subscription(recipients, (Event) o);
	}
}
