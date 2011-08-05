package org.cytoscape.task.internal.quickstart.datasource;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public interface InteractionFilePreprocessor {

	/**
	 * 
	 * @param sourceFileLocation
	 * @throws IOException
	 */
	void processFile() throws IOException;

	/**
	 * Check local data is up-to-date or not.
	 * 
	 * @return true if latest.
	 */
	boolean isLatest();

	Map<String, URL> getDataSourceMap();
}
