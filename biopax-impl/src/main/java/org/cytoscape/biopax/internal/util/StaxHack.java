package org.cytoscape.biopax.internal.util;

import com.ctc.wstx.stax.WstxInputFactory;

public class StaxHack {
	public static final void runWithHack(Runnable runnable) {
		Thread thread = Thread.currentThread();
		ClassLoader loader = thread.getContextClassLoader();
		try {
			thread.setContextClassLoader(WstxInputFactory.class.getClassLoader());
			runnable.run();
		} finally {
			thread.setContextClassLoader(loader);
		}
	}
}