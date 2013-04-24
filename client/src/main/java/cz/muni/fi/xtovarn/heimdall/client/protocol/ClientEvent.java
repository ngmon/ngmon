package cz.muni.fi.xtovarn.heimdall.client.protocol;

/**
 * Event causing the client to change state; Usually fired by Client method call
 * (subscribe, ready...) or as a response to a (received) server message
 */
public enum ClientEvent {
	NETTY_TCP_CONNECTED,
	ERROR,
	REQUEST_CONNECT,
	RECEIVED_CONNECTED,
	REQUEST_SUBSCRIBE,
	RECEIVED_ACK,
	REQUEST_UNSUBSCRIBE,
	REQUEST_READY,
	REQUEST_STOP,
	REQUEST_GET,
	RECEIVED_ACK_FOR_READY,
	REQUEST_DISCONNECT,
	RECEIVED_DISCONNECTED
}
