/*
 * File: CytoscapeVersion.java Copyright (c) 2006, The Cytoscape Consortium
 * (www.cytoscape.org) The Cytoscape Consortium is: - Institute for Systems
 * Biology - University of California San Diego - Memorial Sloan-Kettering
 * Cancer Center - Institut Pasteur - Agilent Technologies This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
 * WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE. The software and documentation provided hereunder is on
 * an "as is" basis, and the Institute for Systems Biology and the Whitehead
 * Institute have no obligations to provide maintenance, support, updates,
 * enhancements or modifications. In no event shall the Institute for Systems
 * Biology and the Whitehead Institute be liable to any party for direct,
 * indirect, special, incidental or consequential damages, including lost
 * profits, arising out of the use of this software and its documentation, even
 * if the Institute for Systems Biology and the Whitehead Institute have been
 * advised of the possibility of such damage. See the GNU Lesser General Public
 * License for more details. You should have received a copy of the GNU Lesser
 * General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */
package org.cytoscape.plugin.internal.util;


/** CytoscapeVersion: identify (and describe) successive versions of cytoscape. */
public class CytoscapeVersion {
	/**
	 * This value will be initialized when the bundle get started
	 */
	public static String version= null;// = "3.0"; // =CytoscapeInit.getProperties().getProperty(
			//"cytoscape.version.number");

	private static String majorMinorVersion;

	private static String bugFixVersion = "0";

	/**
	 */
	public CytoscapeVersion() {
		//version = props.getProperty("cytoscape.version.number");
		if (version != null){

			String[] Versions = version.split("\\.");
			majorMinorVersion = Versions[0] + "." + Versions[1];

			if (Versions.length == 3) {
				bugFixVersion = Versions[2];
			}
		}
	}
	
	/**
	 * Returns the string 'Cytoscape Version ' and the version number Do not use
	 * to just get a version.
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getVersion() {
		return "Cytoscape Version " + version;
	}
	
	/**
	 * @return See getVersion()
	 */
	public String toString() {
		return getVersion();
	}

	/**
	 * This method gets the major and minor version for Cytsocape ignoring
	 * the bug fix version number.  Example: If Cytoscape is currently at 2.4.6
	 * this method will return 2.4
	 * 
	 * @return The major and minor version numbers of the currently running
	 *         Cytoscape. 
	 */
	public String getMajorVersion() {
		return majorMinorVersion;
	}

	/**
	 * This method gets the bug fix version for Cytsocape.  
	 * Example: If Cytoscape is currently at 2.4.6 this method will return 6
	 * If there is no bug fix version the default is 0
	 * 
	 * @return Just the bug fix version
	 */
	public String getBugFixVersion() {
		return bugFixVersion;
	}
	
	/**
	 * 
	 * @return Full version number for currently running Cytoscape.
	 * 		Ex. 2.4.6
	 */
	public String getFullVersion() {
		return version;
	}
}
