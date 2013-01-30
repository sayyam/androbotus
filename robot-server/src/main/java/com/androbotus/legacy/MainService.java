package com.androbotus.legacy;

/**
 * This is the main backend service that controls which 
 * @author maximlukichev
 *
 */
public interface MainService {
	/**
	 * Load and register all the modules from configuration
	 */
	public void startup() throws Exception;
	
	/**
	 * Unregister all the modules and stop the service
	 */
	public void shutdown() throws Exception;
	
	/**
	 * Register the module and subscribe it for the topic
	 * @param moduleClass the module class
	 * @param topic the topic to subscribe to
	 */
	public <T> void registerModule(Class<T> moduleClass, String topic);
	
	/**
	 * Unregister the module and unsubscribe it from the topic
	 * @param moduleClass the module class
	 * @param topic the topic to unsubscribe from
	 */
	public <T> void unregisterModule(Class<T> moduleClass, String topic);
}
