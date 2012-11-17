package cz.muni.fi.xtovarn.heimdall.netty.message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Directive {
	CONNECT(10),
	CONNECTED(11),
	SUBSCRIBE(20),
	UNSUBSCRIBE(21),
	READY(22),
	GET(23),
	SEND_JSON(100),
	SEND_SMILE(101),
	ERROR(111);

	private static final Map<Short, Directive> lookup = new HashMap<Short, Directive>();

	static {
		for (Directive s : EnumSet.allOf(Directive.class)) {
			lookup.put(s.getCode(), s);
		}
	}

	private short code;

	private Directive(int code) {
		this.code = (short) code;
	}

	public short getCode() {
		return code;
	}

	public static Directive get(short code) {
		return lookup.get(code);
	}
}
