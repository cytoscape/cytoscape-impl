package org.cytoscape.search.internal;

import org.junit.rules.ExternalResource;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;

/**
 * This rule will silence the cytoscape console logger when running a JUnit test.
 */
public class LogSilenceRule extends ExternalResource {
	
	private int logLevelBackup;
	
	@Override
	protected void before() {
		silenceLog();
	}
	
	@Override
	protected void after() {
		restoreLog();
	}
	
	
	private void silenceLog() {
		logLevelBackup = DefaultServiceLog.getStaticLogLevel();
		DefaultServiceLog.setLogLevel("ERROR");
	}
	
	private void restoreLog() {
		String level = DefaultServiceLog.levels[logLevelBackup];
		DefaultServiceLog.setLogLevel(level);
	}
	
}