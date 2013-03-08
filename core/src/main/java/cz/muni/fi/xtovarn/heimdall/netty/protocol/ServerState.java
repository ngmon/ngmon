package cz.muni.fi.xtovarn.heimdall.netty.protocol;

public enum ServerState {
	CREATED, CONNECTED, SENDING, PRE_CONNECTED, DISCONNECTED, SUBSCRIPTION_RECEIVED, UNSUBSCRIBE_RECEIVED
}
