package cz.muni.fi.xtovarn.heimdall.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;

public interface ClientApi {

	public Future<Long> subscribe(Predicate predicate) throws InterruptedException, ExecutionException;
	public Future<Boolean> unsubscribe(Long subscriptionId) throws InterruptedException, ExecutionException;
	
	public Future<Boolean> ready();
	public Future<Boolean> stopSending();
	public Future<Boolean> get();
	
	public void setEventReceivedHandler(EventReceivedHandler handler);
	public void setServerResponseExceptionHandler(ServerResponseExceptionHandler handler);

	public Future<Boolean> disconnect();
	public void stop();
	
	public void reset();

}