package cz.muni.fi.xtovarn.heimdall.pipeline.handlers;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.netty.ConnectionPools;
import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.Handler;

public class DetermineRecipient implements Handler {

	@Override
	public Object handle(Object o) {
		ConnectionPools.getPool().send("xdanos", (Event) o);

		return null;
	}
}
