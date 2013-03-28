package cz.muni.fi.xtovarn.heimdall.client;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;

public interface ClientApi {

	public Long getConnectionId();

	public boolean isConnected();

	public Future<Long> subscribe(Predicate predicate) throws InterruptedException, ExecutionException;

	public List<Long> getSubscriptionIds();

	public Long getLastSubscriptionId();

	public boolean wasLastSubscriptionSuccessful();

	public Future<Boolean> unsubscribe(Long subscriptionId) throws InterruptedException, ExecutionException;
	
	public Future<Boolean> ready();

	public void stop();

	public Future<Boolean> stopSending();

}