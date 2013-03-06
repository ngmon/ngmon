package cz.muni.fi.xtovarn.heimdall.pubsub;

public class SubscriptionManagerSingleton {

	// instantiating this way should be thread-safe
	private static SubscriptionManager subscriptionManager = new SubscriptionManager();
	
	public static SubscriptionManager getSubscriptionManager() {
		return subscriptionManager;
	}

}
