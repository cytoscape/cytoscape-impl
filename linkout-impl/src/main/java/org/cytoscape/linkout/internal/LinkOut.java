
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.linkout.internal;

import org.cytoscape.application.CyApplicationConfiguration;

import org.cytoscape.property.CyProperty;

import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.EdgeViewTaskFactory;

import org.cytoscape.util.swing.OpenBrowser;

import org.cytoscape.view.model.View;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyColumn;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;


import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generates links to external web pages specified in the cytoscape.props file.
 * Nodes can be linked to external web pages by specifying web resources in the linkout.properties file
 * The format for a weblink is in the form of a <key> = <value> pair where <key> is the name of the
 * website (e.g. NCBI, E!, PubMed, SGD,etc) and <value> is the URL. The key name must be preceded by the keyword "url." to distinguish
 * this property from other properties.
 * In the URL string the placeholder %ID% will be replaced with the node label that is visible on the node.
 * If no label is visible, the node identifier (far left of attribute browser) will be used.
 * It is the users responsibility
 * to ensure that the URL is correct and the node's name will match the required query value.
 * Examples:
 *    url.NCBI=http\://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd\=Search&amp;db\=Protein&amp;term\=%ID%&amp;doptcmdl\=GenPept
 *    url.SGD=http\://db.yeastgenome.org/cgi-bin/locus.pl?locus\=%ID%
 *    url.E\!Ensamble=http\://www.ensembl.org/Homo_sapiens/textview?species\=all&amp;idx\=All&amp;q\=%ID%
 *    url.Pubmed=http\://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd\=Search&amp;db\=PubMed&amp;term\=%ID%
 *
 */
public class LinkOut {

	private static final Logger logger = LoggerFactory.getLogger(LinkOut.class);

	// keywords that marks properties that should be added to LinkOut
	public static final String NODEMARKER = "nodelinkouturl.";
	public static final String EDGEMARKER = "edgelinkouturl.";

	private final Properties props;
	private final CyServiceRegistrar registrar;
	private final OpenBrowser browser;
	private final CyApplicationConfiguration config;

	/**
	 * Creates a new LinkOut object.
	 *
	 * @param props  DOCUMENT ME!
	 * @param registrar  DOCUMENT ME!
	 * @param browser  DOCUMENT ME!
	 * @param config  DOCUMENT ME!
	 */
	public LinkOut(CyProperty<Properties> propService, CyServiceRegistrar registrar, OpenBrowser browser,
	               final CyApplicationConfiguration config) {
		this.props = propService.getProperties();
		this.registrar = registrar;
		this.browser = browser;
		this.config = config;

		readLocalProperties();

		addStaticNodeLinks();
		addStaticEdgeLinks();

		addDynamicLinks();
	}

	/**
	 * Read properties values from linkout.props file included in the
	 * linkout.jar file and apply those properties to the base Cytoscape
	 * properties. Only apply the properties from the jar file if NO linkout
	 * properties already exist. This allows linkout properties to be specified
	 * on the command line, editted in the preferences dialog, and to be saved
	 * with other properties.
	 */
	private void readLocalProperties() {
		File propertyFile= new File(config.getSettingLocation(), File.separator + "linkout.props");
		try {
			if (propertyFile.canRead())
				props.load(new FileInputStream(propertyFile));
		} catch (Exception e) {
			logger.warn("Couldn't load linkout props from \'" + propertyFile.getAbsolutePath() + "\'!", e);
		}
	}

	private void addStaticNodeLinks() {
		try {
			for (Object pk : props.keySet()) {
				String propKey = pk.toString();
				String url = props.getProperty(propKey);
				Properties dict = createProperties(propKey, NODEMARKER);
				if (url == null || dict == null ) {
					logger.debug("Bad URL for propKey: " + propKey);
					continue;
				}
				NodeViewTaskFactory evtf = new NodeLinkoutTaskFactory(browser,url);
				registrar.registerService(evtf, NodeViewTaskFactory.class, dict);
			}
		} catch (Exception e) {
			logger.warn("Problem processing node URLs", e);
		}
	}

	private void addStaticEdgeLinks() {
		try {
			for (Object pk : props.keySet()) {
				String propKey = pk.toString();
				String url = props.getProperty(propKey);
				Properties dict = createProperties(propKey, EDGEMARKER);
				if (url == null || dict == null ) {
					logger.debug("Bad URL for propKey: " + propKey);
					continue;
				}
				EdgeViewTaskFactory evtf = new EdgeLinkoutTaskFactory(browser,url);
				registrar.registerService(evtf, EdgeViewTaskFactory.class, dict);
			}
		} catch (Exception e) {
			logger.warn("Problem processing edge URLs", e);
		}
	}


	private Properties createProperties(String propKey, String marker) {
		int p = propKey.lastIndexOf(marker);
		if (p == -1)
			return null;
		p = p + marker.length();
		Properties dict = new Properties();
		String menuKey = "LinkOut." + propKey.substring(p);
		dict.setProperty("preferredMenu", menuKey);
		return dict;
	}

	private void addDynamicLinks() {

		Properties ndict = new Properties();
		ndict.setProperty("preferredTaskManager","menu");
		ndict.setProperty("title","LinkOut Dynamic");
		// menu titles are generated dynamically
		NodeViewTaskFactory dynamicNodeUrls = new DynamicNodeLinkoutTaskFactory(browser);
		registrar.registerService(dynamicNodeUrls, NodeViewTaskFactory.class, ndict);

		Properties edict = new Properties();
		edict.setProperty("preferredTaskManager","menu");
		ndict.setProperty("title","LinkOut Dynamic");
		// menu titles are generated dynamically
		EdgeViewTaskFactory dynamicEdgeUrls = new DynamicEdgeLinkoutTaskFactory(browser);
		registrar.registerService(dynamicEdgeUrls, EdgeViewTaskFactory.class, edict);
	}

}
