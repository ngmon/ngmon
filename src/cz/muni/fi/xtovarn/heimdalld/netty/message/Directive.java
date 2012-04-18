package cz.muni.fi.xtovarn.heimdalld.netty.message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Directive {
	GREET(1),
	AUTH_REQUEST(40),
	AUTH_RESPONSE(41),
	CHALLENGE(42),
	COMMAND(60),
	SEND_JSON(100),
	SEND_SMILE(101);

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
