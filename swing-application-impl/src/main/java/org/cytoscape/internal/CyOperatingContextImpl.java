/*
 File: CyOpertatingContextImpl.java

 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal;


import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.property.CyProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Basic access to Cytoscape's operating context. 
 */
// TODO This is currently only used to store properties when saved in the preferences
// dialog - this needs to be supported more generally on an application level, perhaps
// on a shutdown event?
public class CyOperatingContextImpl {
	public static final String PROPS = "cytoscape.props";

	private static final Logger logger = LoggerFactory.getLogger(CyOperatingContextImpl.class);

	private CyProperty<Properties> props;
	private final CyApplicationConfiguration config;

	public CyOperatingContextImpl(CyProperty<Properties> props, final CyApplicationConfiguration config) {
		if ( props == null )
			throw new NullPointerException("Cytoscape Properties is null");
		if(config == null)
			throw new NullPointerException("Application Config is missing.");

		this.props = props;
		this.config = config;

		loadLocalProps();
	}

	private void loadLocalProps() {
		try {
			File vmp = getConfigFile(PROPS);

			if (vmp != null)
				props.getProperties().load(new FileInputStream(vmp));
			else
				logger.warn("couldn't read " + PROPS + " from " + config.getSettingLocation());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Returns cytoscape.props.
	 */
	// TODO Should we be returning a copy here to keep thing synchronized or
	// do we want just one properties object?
	public Properties getProperties() {
		return props.getProperties();
	}


	/**
	 * Returns the specified file if it's found in the config directory. 
	 */
	public File getConfigFile(String file_name) {
		try {
			File file = new File(config.getSettingLocation(), file_name);

			if (file.createNewFile())
				logger.warn("Config file: " + file + " created.");

			return file;
		} catch (Exception e) {
			logger.warn("error getting config file:" + file_name);
		}

		return null;
	}
}

