package org.cytoscape.application.internal;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.application.CyVersion;
import org.cytoscape.property.CyProperty;

/** 
 * Identify the version of cytoscape. 
 */
public class CyVersionImpl implements CyVersion {

	private static final String PROPERTY_FILE = "config.properties";

	private static final String APPLICATION_VERSION_PROPERTY = "application.version";

	private final static Pattern p = Pattern.compile(CyVersion.VERSION_REGEX);

	private final int major;
	private final int minor;
	private final int bugfix;
	private final String qualifier;
	private final String version;

	@SuppressWarnings("deprecation")
	public CyVersionImpl(final CyProperty<Properties> props) {
		InputStream stream = getClass().getResourceAsStream(PROPERTY_FILE);
		if ( stream == null )
			throw new NullPointerException("Application properties are missing");
		
		Properties properties = new Properties();
		try {
			properties.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		version = properties.getProperty(APPLICATION_VERSION_PROPERTY); 

		if ( version == null )
			throw new NullPointerException("No version number found in the provided properties");

		Matcher m = p.matcher(version);

		if ( !m.matches() )
			throw new IllegalArgumentException("Malformed version number: " + version + "  The version number must match this regular expression: " + CyVersion.VERSION_REGEX);

		props.getProperties().setProperty(CyVersion.VERSION_PROPERTY_NAME, version);

		major = Integer.parseInt(m.group(1));
		minor = Integer.parseInt(m.group(2));
		bugfix = Integer.parseInt(m.group(3));
		qualifier = m.group(4);
	}

	/**
	 * @inheritdoc
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @inheritdoc
	 */
	public int getMajorVersion() {
		return major;
	}

	/**
	 * @inheritdoc
	 */
	public int getMinorVersion() {
		return minor;
	}

	/**
	 * @inheritDoc
	 */
	public int getBugFixVersion() {
		return bugfix;
	}

	/**
	 * @inheritdoc
	 */
	public String getQualifier() {
		return qualifier;
	}
}
