package org.cytoscape.property.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import static org.cytoscape.application.CyApplicationConfiguration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropsReader extends  AbstractConfigDirPropsReader 
	implements SessionLoadedListener {

	private static final Logger logger = LoggerFactory.getLogger(PropsReader.class);

	/**
	 * Creates a new PropsReader object.
	 */
	public PropsReader(String propsName) {
		super(propsName,CyProperty.SavePolicy.CONFIG_DIR);
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		logger.debug("Updating Properties from loaded session...");
		
		Properties newProps = null;
		CySession sess = e.getLoadedSession();
		
		if (sess != null)
			newProps = sess.getCytoscapeProperties();
		else
			logger.warn("Loaded session is null.");
		
		if (newProps == null) {
			logger.warn("Could not get new properties from loaded session - using empty properties.");
			newProps = new Properties();
		}

		props.clear();
	 	props.putAll(newProps);	
	}
}
