package cz.muni.fi.xtovarn.heimdall.netty.message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Directive {
	CONNECT(10), // for client to request authenticated connection to the server
	CONNECTED(11), // for server to confirm the authenticated connection succeeded
	SUBSCRIBE(20), // for client to subscribe to sensor events
	ACK(24), // for server to confirm the action succeeded
	UNSUBSCRIBE(21), // for client to unsubscribe
	READY(22), // for client to start receiving sensor events forwarded by server
	GET(23), // for client to request "missed" sensor events
	DISCONNECT(25), // for client to inform the server it's going to disconnect
	STOP(26), // for client to stop receiving sensor events forwarded by server
	SEND_JSON(100), // for server to forward a sensor event using the JSON format
	SEND_SMILE(101), // for server to forward a sensor event using the SMILE (JSON binary) format
	ERROR(111); // for server to inform the client the action failed

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
