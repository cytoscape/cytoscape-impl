package org.cytoscape.internal.shutdown;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CytoscapeShutdownEvent;
import org.cytoscape.application.events.CytoscapeShutdownListener;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.io.FileOutputStream;


public class ConfigDirPropertyWriter implements CytoscapeShutdownListener {
	private final TaskManager taskManager;
	private final CyPropertyWriterManager propertyWriterManager;
	private final Map<CyProperty, Map> configDirProperties;
	private final CyApplicationConfiguration config;
	private static final Logger logger = LoggerFactory.getLogger(ConfigDirPropertyWriter.class);

	public ConfigDirPropertyWriter(final TaskManager taskManager,
				final CyPropertyWriterManager propertyWriterManager, final CyApplicationConfiguration config)
	{
		this.taskManager = taskManager;
		this.propertyWriterManager = propertyWriterManager;
		this.config = config;
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
			final String propertyFileName;
			if(propertyName.endsWith(".props"))
				propertyFileName = propertyName;
			else
				propertyFileName = propertyName + ".props";
			
			final File outputFile = new File(config.getSettingLocation(), propertyFileName);
			
			//final PropertyWriterFactory taskFactory =
			//	new PropertyWriterFactory(propertyWriterManager, keyAndValue.getKey(),
			//				  matchingFileFilter, outputFile);

			//taskFactory.getTaskIterator().next().run(arg0)
			//taskManager.execute(taskFactory);

			
			// write properties file
			// This is a work-around, because there are bugs in propertiesWriter 
			// (1) can not close outputStream (2) Execute System.exit() before propsWriterTask 
			Properties props = (Properties) keyAndValue.getKey().getProperties();

			try {
				FileOutputStream out = new FileOutputStream(outputFile);
				props.store(out, null);
				out.close();
			}
			catch(Exception e){
				logger.error("Error in wring properties file!");
			}
			
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
