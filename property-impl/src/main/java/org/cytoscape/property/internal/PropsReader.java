package org.cytoscape.property.internal;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropsReader implements CyProperty<Properties>, SessionLoadedListener {
	private static final Logger logger = LoggerFactory.getLogger(PropsReader.class);
	private Properties props;

	/**
	 * Creates a new PropsReader object.
	 */
	public PropsReader(String resourceLocation) {
		InputStream is = null; 

		try {
			if (resourceLocation == null)
				throw new NullPointerException("resourceLocation is null");

			is = this.getClass().getClassLoader().getResourceAsStream(resourceLocation);

			props = new Properties();
			props.load(is);

		} catch (Exception e) {
			logger.warn("Could not read properties file - using empty intance.", e);
			props = new Properties();
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException ioe) {}
				is = null;
			}
		}
	}

	@Override
	public CyProperty.SavePolicy getSavePolicy() {
		return CyProperty.SavePolicy.DO_NOT_SAVE;
	}
	
	@Override
	public Properties getProperties() {
		return props;
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
		
		this.props = newProps;
	}
}
