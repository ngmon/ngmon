package cz.muni.fi.xtovarn.heimdalld.pipeline.handler;

import cz.muni.fi.xtovarn.heimdalld.db.entity.Event;

import java.util.Date;

public class SetDetectionTime implements Handler {
	@Override
	public Object handle(Object o) {

		((Event)o).setDetectionTime(new Date(System.currentTimeMillis()));
		return o;
	}
}
