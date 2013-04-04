package cz.muni.fi.xtovarn.heimdall.netty.handler;

import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.ServerEvent;

public class HandlerUtils {

	public static ServerEvent directiveToServerEvent(Directive directive) {
		switch (directive) {
		case CONNECT:
			return ServerEvent.RECEIVED_CONNECT;
		case SUBSCRIBE:
			return ServerEvent.RECEIVED_SUBSCRIBE;
		case UNSUBSCRIBE:
			return ServerEvent.RECEIVED_UNSUBSCRIBE;
		case DISCONNECT:
			return ServerEvent.RECEIVED_DISCONNECT;
		case READY:
			return ServerEvent.READY;
		case STOP:
			return ServerEvent.STOP;
		case GET:
			return ServerEvent.GET;
		default:
			return null;
		}
	}

}
