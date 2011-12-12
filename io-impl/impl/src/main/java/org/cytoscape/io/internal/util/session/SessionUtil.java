/*
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
package org.cytoscape.io.internal.util.session;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionUtil {
	
	// Document versions
	public static final String CYS_VERSION = "3.0.0";
	public static final String CYSESSION_VERSION = "3.0";
	
	public static final String BOOKMARKS_FILE = "session_bookmarks.xml";
	public static final String CYSESSION = "cysession.xml";
	public static final String VIZMAP_PROPS = "vizmap.props";
	public static final String VIZMAP_XML = "vizmap.xml";
	public static final String CY_PROPS = "cytoscape3.props";
	public static final String XGMML_EXT = ".xgmml";
	public static final String VERSION_EXT = ".version";
	public static final String TABLE_EXT = ".cytable";
	public static final String CYTABLE_METADATA_FILE = "cytable.metadata";
	
	public static final String NETWORKS_FOLDER = "networks/";
	public static final String NETWORK_VIEWS_FOLDER = "views/";
	public static final String TABLES_FOLDER = "tables/";
	public static final String APPS_FOLDER = "apps/";
	
	public static final String NETWORK_ROOT = "Network Root";
	
	private static boolean readingSessionFile;
	private static boolean writingSessionFile;
	
	private static final Logger logger = LoggerFactory.getLogger(SessionUtil.class);
	
	public static String escape(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8").replace("-", "%2D");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String unescape(String escapedText) {
		try {
			return URLDecoder.decode(escapedText, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getNetworkTableFilename(CyNetwork network, CyTableMetadata metadata) throws UnsupportedEncodingException {
		CyTable table = metadata.getTable();
		Long networkId = network.getSUID();
		String networkFileName = getNetworkFileName(network);
		String namespace = escape(metadata.getNamespace());
		String type = escape(metadata.getType().getCanonicalName());
		String tableTitle = escape(table.getTitle());
		return String.format("%s_%s/%s-%s-%s%s", networkId, networkFileName, namespace, type, tableTitle, TABLE_EXT);
	}
	
	public static String getXGMMLFilename(CyNetwork network) throws UnsupportedEncodingException {
		String name = getNetworkFileName(network);
		String id = escape(Long.toString(network.getSUID()));
		return String.format("%s_%s%s", id, name, XGMML_EXT);
	}
	
	public static String getXGMMLFilename(CyNetworkView view) throws UnsupportedEncodingException {
		String title = getNetworkViewFileName(view);
		String viewId = escape(Long.toString(view.getSUID()));
		String netId = escape(Long.toString(view.getModel().getSUID()));
		return String.format("%s_%s_%s%s", netId, viewId, title, XGMML_EXT);
	}
	
	public static String getNetworkFileName(CyNetwork network) throws UnsupportedEncodingException {
		String name = escape(network.getRow(network).get(CyNetwork.NAME, String.class));
		
		if (name == null || name.isEmpty())
			name = Long.toString(network.getSUID());
		
		return name;
	}
	
	public static String getNetworkViewFileName(CyNetworkView view) throws UnsupportedEncodingException {
		String name = escape(view.getVisualProperty(MinimalVisualLexicon.NETWORK_TITLE));
		
		if (name == null || name.isEmpty())
			name = getNetworkFileName(view.getModel());
		
		return name;
	}

	public static int getMajorVersion(String version) {
		if (version != null) {
			String[] numbers = version.split(".");
			if (numbers.length > 0) {
				try {
					return Integer.parseInt(numbers[0]);
				} catch (Exception ex) {
					logger.error("Invalid version string: " + version);
				}
			}
		}
		
		return 0;
	}

	public static boolean isReadingSessionFile() {
		return readingSessionFile;
	}

	public static void setReadingSessionFile(boolean readingSessionFile) {
		SessionUtil.readingSessionFile = readingSessionFile;
	}

	public static boolean isWritingSessionFile() {
		return writingSessionFile;
	}

	public static void setWritingSessionFile(boolean writingSessionFile) {
		SessionUtil.writingSessionFile = writingSessionFile;
	}
}
