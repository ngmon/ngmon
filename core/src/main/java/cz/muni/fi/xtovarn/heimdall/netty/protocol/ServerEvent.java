package cz.muni.fi.xtovarn.heimdall.netty.protocol;

public enum ServerEvent {
	NETTY_TCP_CONNECTED, ERROR, RECEIVED_CONNECT, RECEIVED_SUBSCRIBE, PROCESS_SUBSCRIPTION, RECEIVED_UNSUBSCRIBE
}
