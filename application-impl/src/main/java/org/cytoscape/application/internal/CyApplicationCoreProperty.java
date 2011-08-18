package org.cytoscape.application.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyApplicationCoreProperty implements CyProperty<Properties>, SessionLoadedListener {
	private static final Logger logger = LoggerFactory.getLogger(CyApplicationCoreProperty.class);

	// This is in the resource file (jar)
	private static final String DEF_PROP_FILE_NAME = "cytoscape.props";

	private Properties props;

	/**
	 * Creates a new PropsReader object.
	 * 
	 */
	public CyApplicationCoreProperty(final CyApplicationConfiguration config) {
		InputStream is = null;

		final File propFile = new File(config.getSettingLocation(), "cytoscape.props");

		try {
			
			if (propFile.exists()) {
				is = new FileInputStream(propFile);
				props = new Properties();
				props.load(is);
				if(props.size() == 0) {
					// Need to load default
					is = this.getClass().getClassLoader().getResourceAsStream(DEF_PROP_FILE_NAME);
					props = new Properties();
					try {
						props.load(is);
					} catch (IOException e1) {
						logger.warn("Could not read core property.  Use empty one.", e1);
					}
				}
				logger.info("PropFile is: " + propFile.getAbsolutePath());
			} else {
				logger.warn("Could not read properties from config directry - trying to load default properties.");
				is = this.getClass().getClassLoader().getResourceAsStream(DEF_PROP_FILE_NAME);
				props = new Properties();
				try {
					props.load(is);
				} catch (IOException e1) {
					logger.warn("Could not read core property.  Use empty one.", e1);
				}
			}
		} catch (Exception e) {
			logger.warn("Could not read properties from config directry - trying to load default properties.", e);
			is = this.getClass().getClassLoader().getResourceAsStream(DEF_PROP_FILE_NAME);
			props = new Properties();
			try {
				props.load(is);
			} catch (IOException e1) {
				logger.warn("Could not read core property.  Use empty one.", e1);
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
				}
				is = null;
			}
		}
	}

	@Override
	public CyProperty.SavePolicy getSavePolicy() {
		return CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR;
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
