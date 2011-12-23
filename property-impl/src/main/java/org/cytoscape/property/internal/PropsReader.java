package org.cytoscape.property.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import static org.cytoscape.application.CyApplicationConfiguration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropsReader implements CyProperty<Properties>, SessionLoadedListener {
	private static final Logger logger = LoggerFactory.getLogger(PropsReader.class);
	private Properties props;

	/**
	 * Creates a new PropsReader object.
	 */
	public PropsReader(String propsName) {
		try {
			props = new Properties();
			readDefaultFromJar(props,propsName);
			readLocalModifications(props,propsName);
		} catch (Exception e) {
			logger.warn("Error reading properties file - using empty intance.", e);
			props = new Properties();
		}
	}

	private void readLocalModifications(Properties props, String propsName) throws Exception {
		InputStream is = null;
		try {
			final File configDir = new File(System.getProperty("user.home"), DEFAULT_CONFIG_DIR); 
	        final File localPropsFile = new File(configDir,propsName);

			if (localPropsFile.exists()) {
				is = new FileInputStream(localPropsFile);
				props.load(is);
				is.close();
			}
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException ioe) {}
				is = null;
			}
		}
	}

	private void readDefaultFromJar(Properties props, String propsName) throws Exception {
		InputStream is = null;
		try {
			if (propsName == null)
				throw new NullPointerException("propsName is null");

			is = this.getClass().getClassLoader().getResourceAsStream(propsName);

			props.load(is);
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException ioe) {}
				is = null;
			}
		}
	}

	@Override
	public CyProperty.SavePolicy getSavePolicy() {
		return CyProperty.SavePolicy.CONFIG_DIR;
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
