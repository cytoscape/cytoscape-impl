/*
  File: CytoscapeVersion.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.application.CytoscapeVersion;
import org.cytoscape.property.CyProperty;

/** 
 * Identify the version of cytoscape. 
 */
public class CyVersion implements CytoscapeVersion {

	private final static Pattern p = Pattern.compile(CytoscapeVersion.VERSION_REGEX);

	private final int major;
	private final int minor;
	private final int bugfix;
	private final String qualifier;
	private final String version;

	public CyVersion(final CyProperty<Properties> props) {
		version = props.getProperties().getProperty(CytoscapeVersion.VERSION_PROPERTY_NAME);

		if ( version == null )
			throw new NullPointerException("No version number found in the provided properties with property name: " + CytoscapeVersion.VERSION_PROPERTY_NAME);

		Matcher m = p.matcher(version);

		if ( !m.matches() )
			throw new IllegalArgumentException("Malformed version number: " + version + "  The version number must match this regular expression: " + CytoscapeVersion.VERSION_REGEX);

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
	 * @inheritdoc
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
