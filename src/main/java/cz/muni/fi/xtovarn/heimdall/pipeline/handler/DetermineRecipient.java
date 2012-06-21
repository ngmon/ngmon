package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Subscription;

import java.util.HashSet;
import java.util.Set;

public class DetermineRecipient implements Handler {

	@Override
	public Object handle(Object o) {
		Set<String> recipients = new HashSet<String>(1);

		recipients.add("xdanos@1");
		recipients.add("xdanos@2");
		recipients.add("xdanos@3");
		recipients.add("xdanos@4");
		recipients.add("xdanos@5");
		recipients.add("xdanos@6");
		recipients.add("xdanos@7");
		recipients.add("xdanos@8");
		recipients.add("xdanos@9");
		recipients.add("xdanos@10");

		return new Subscription(recipients, (Event) o);
	}
}
