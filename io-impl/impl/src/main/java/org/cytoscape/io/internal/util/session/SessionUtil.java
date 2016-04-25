package org.cytoscape.io.internal.util.session;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionUtil {
	
	// Document versions
	public static final String CYS_VERSION = "3.0.0";
	public static final String CYSESSION_VERSION = "3.0";
	
	public static final String NETWORKS_FOLDER = "networks/";
	public static final String NETWORK_VIEWS_FOLDER = "views/";
	public static final String PROPERTIES_FOLDER = "properties/";
	public static final String TABLES_FOLDER = "tables/";
	public static final String IMAGES_FOLDER = "images/";
	public static final String APPS_FOLDER = "apps/";
	
	public static final String BOOKMARKS_FILE = "session_bookmarks.xml";
	public static final String CYSESSION_FILE = "cysession.xml";
	public static final String VIZMAP_PROPS_FILE = "vizmap.props";
	public static final String VIZMAP_XML_FILE = "vizmap.xml";
	public static final String CYTABLE_STATE_FILE = "cytables.xml";
	
	public static final String PROPERTIES_EXT = ".props";
	public static final String TABLE_EXT = ".cytable";
	public static final String VERSION_EXT = ".version";
	public static final String XGMML_EXT = ".xgmml";
	
	public static final String NETWORK_ROOT = "Network Root";
	
	private static boolean readingSessionFile; // TODO: delete it and find a better solution!
	
	private static final Logger logger = LoggerFactory.getLogger(SessionUtil.class);

	private SessionUtil() {
	}

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
		return String.format("%s-%s/%s-%s-%s%s", networkId, networkFileName, namespace, type, tableTitle, TABLE_EXT);
	}
	
	public static String getXGMMLFilename(CyNetwork network) throws UnsupportedEncodingException {
		String name = getNetworkFileName(network);
		String id = escape(Long.toString(network.getSUID()));
		return String.format("%s-%s%s", id, name, XGMML_EXT);
	}
	
	public static String getXGMMLFilename(CyNetworkView view) throws UnsupportedEncodingException {
		String title = getNetworkViewFileName(view);
		String viewId = escape(Long.toString(view.getSUID()));
		String netId = escape(Long.toString(view.getModel().getSUID()));
		return String.format("%s-%s-%s%s", netId, viewId, title, XGMML_EXT);
	}
	
	public static String getNetworkFileName(CyNetwork network) throws UnsupportedEncodingException {
		String name = network.getRow(network).get(CyNetwork.NAME, String.class);
		
		if (name == null || name.isEmpty())
			name = network.getSUID().toString();
		
		name = escape(name);
		
		return name;
	}
	
	public static String getNetworkViewFileName(CyNetworkView view) throws UnsupportedEncodingException {
		String name = escape(view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE));
		
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
}
