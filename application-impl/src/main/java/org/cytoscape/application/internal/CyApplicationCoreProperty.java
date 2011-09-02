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
	private static final String DEF_PROP_FILE_NAME = "cytoscape3.props";

	private Properties props;

	/**
	 * Creates a new PropsReader object.
	 * 
	 */
	public CyApplicationCoreProperty(final CyApplicationConfiguration config) {

		InputStream is = null;

		// Load default Cytoscape properties
		Properties defaultProps = new Properties();
		
		is = this.getClass().getClassLoader().getResourceAsStream(DEF_PROP_FILE_NAME);
		try {
			defaultProps.load(is);
		} catch (IOException e1) {
			logger.warn("Could not read core property.  Use empty one.", e1);
		}
		
		//Load existing properties from config directory if any
		final File propFile = new File(config.getSettingLocation(), "cytoscape3.props");
		
		props = new Properties();
		
		try {
			if (propFile.exists()) {
				is = new FileInputStream(propFile);
				props.load(is);
			} 
		} catch (Exception e) {
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
				}
				is = null;
			}
		}
		
		if(props.size() == 0){
			// Properties does not exist in config directory, use the default properties
			props = defaultProps;
		}
		else {
			// Properties already existed, merge default properties with existing properties
			Object[] keys = defaultProps.keySet().toArray();
			for (Object key: keys){
				if (!props.containsKey(key)){
					props.setProperty((String)key, defaultProps.getProperty((String)key));
				}
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
