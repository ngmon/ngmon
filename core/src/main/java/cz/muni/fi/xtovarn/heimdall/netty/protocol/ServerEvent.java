package cz.muni.fi.xtovarn.heimdall.netty.protocol;

/**
 * Event causing the server to change state; Usually fired by client request
 * (subscribe, ready...) or after the request has been processed (subscribe)
 */
public enum ServerEvent {
	NETTY_TCP_CONNECTED,
	ERROR,
	RECEIVED_CONNECT,
	RECEIVED_SUBSCRIBE,
	SUBSCRIPTION_PROCESSED,
	RECEIVED_UNSUBSCRIBE,
	UNSUBSCRIBE_PROCESSED,
	RECEIVED_DISCONNECT,
	READY,
	STOP,
	GET
}
