package org.cytoscape.internal.shutdown;


import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.swing.events.CytoscapeShutdownEvent;
import org.cytoscape.application.swing.events.CytoscapeShutdownListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskManager;


public class ConfigDirPropertyWriter implements CytoscapeShutdownListener {
	private final TaskManager taskManager;
	private final CyPropertyWriterManager propertyWriterManager;
	private final Map<CyProperty, Map> configDirProperties;

	ConfigDirPropertyWriter(final TaskManager taskManager,
				final CyPropertyWriterManager propertyWriterManager)
	{
		this.taskManager = taskManager;
		this.propertyWriterManager = propertyWriterManager;
		configDirProperties = new HashMap<CyProperty, Map>();
	}

	public void handleEvent(final CytoscapeShutdownEvent event) {
		CyFileFilter matchingFileFilter = null;
		for (final CyFileFilter fileFilter : propertyWriterManager.getAvailableWriterFilters()) {
			if (fileFilter.getExtensions().contains("props")) {
				matchingFileFilter = fileFilter;
				break;
			}
		}
		if (matchingFileFilter == null)
			throw new IllegalStateException("could not find a properties CyFileFilter!");

		for (final Map.Entry<CyProperty, Map> keyAndValue : configDirProperties.entrySet()) {
			final String propertyName = (String)keyAndValue.getValue().get("cyPropertyName");
			final String outputFileName =
				System.getProperty("user.home") + "/" + CyProperty.DEFAULT_CONFIG_DIR
				+ "/" + propertyName + ".props";
			final File outputFile = new File(outputFileName);
			final PropertyWriterFactory taskFactory =
				new PropertyWriterFactory(propertyWriterManager, keyAndValue.getKey(),
							  matchingFileFilter, outputFile);
			taskManager.execute(taskFactory);
		}
	}

	public void addCyProperty(final CyProperty newCyProperty, final Map properties) {
		if (newCyProperty.getSavePolicy() == CyProperty.SavePolicy.CONFIG_DIR
		    || newCyProperty.getSavePolicy() == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR)
			configDirProperties.put(newCyProperty, properties);
	}

	public void removeCyProperty(final CyProperty oldCyProperty, final Map properties) {
		if (oldCyProperty.getSavePolicy() == CyProperty.SavePolicy.CONFIG_DIR
		    || oldCyProperty.getSavePolicy() == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR)
			configDirProperties.remove(oldCyProperty);
		
	}
}