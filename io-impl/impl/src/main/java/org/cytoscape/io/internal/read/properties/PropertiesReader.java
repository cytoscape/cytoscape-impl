package org.cytoscape.io.internal.read.properties;

import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.io.internal.read.AbstractPropertyReader;

public class PropertiesReader extends AbstractPropertyReader {

	public PropertiesReader(InputStream inputStream) {
		super(inputStream);
	}

	public void run(TaskMonitor tm) throws Exception {
		Properties props = new Properties();
		props.load(inputStream);
		propertyObject = props; 
	}
}
