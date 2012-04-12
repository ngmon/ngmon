package cz.muni.fi.xtovarn.heimdall.pipeline.handlers;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.Handler;

import java.util.Date;

public class SetDetectionTime implements Handler {
	@Override
	public Object handle(Object o) {

		((Event)o).setDetectionTime(new Date(System.currentTimeMillis()));
		return o;
	}
}
