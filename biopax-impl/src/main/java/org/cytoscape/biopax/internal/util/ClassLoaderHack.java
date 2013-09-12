package org.cytoscape.biopax.internal.util;

/**
 * This is to fix JVM's factory auto-discovery, etc..
 * (things such as JAXB and StAX might not always work 
 * within OSGI environment...)
 * 
 * @author rodche
 *
 */
public class ClassLoaderHack {
	public static final void runWithHack(Runnable runnable, Class<?> clazz) {
		
		Thread thread = Thread.currentThread();
		ClassLoader loader = thread.getContextClassLoader();
		try {
			thread.setContextClassLoader(clazz.getClassLoader());
			runnable.run();
		} finally {
			thread.setContextClassLoader(loader);
		}
		
	}
}