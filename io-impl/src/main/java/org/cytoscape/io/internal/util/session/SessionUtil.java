package org.cytoscape.io.internal.util.session;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;

public class SessionUtil {
	public static final String CYSESSION = "cysession.xml";
	public static final String VIZMAP_PROPS = "vizmap.props";
	public static final String VIZMAP_XML = "vizmap.xml";
	public static final String CY_PROPS = "cytoscape.props";
	public static final String XGMML_EXT = ".xgmml";
	public static final String BOOKMARKS_FILE = "session_bookmarks.xml";
	public static final String TABLE_EXT = ".cytable";
	public static final String CYTABLE_METADATA_FILE = "cytable.metadata";
	public static final String NETWORK_ROOT = "Network Root";

	public static final Pattern NETWORK_PATTERN = Pattern.compile(".*/([^/]+)[.]xgmml");
	public static final Pattern NETWORK_TABLE_PATTERN = Pattern.compile(".*/(([^/]+)/([^/]+)-([^/]+)-([^/]+)[.]cytable)");
	public static final Pattern GLOBAL_TABLE_PATTERN = Pattern.compile(".*/(global/(\\d+)-([^/]+)[.]cytable)");

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
		CyTable table = metadata.getCyTable();
		String networkFileName = getNetworkFileName(network);
		String namespace = escape(metadata.getNamespace());
		String type = escape(metadata.getType().getCanonicalName());
		String tableTitle = escape(table.getTitle());
		return String.format("%s/%s-%s-%s.cytable", networkFileName, namespace, type, tableTitle );
	}
	
	public static String getNetworkFileName(CyNetwork network) throws UnsupportedEncodingException {
		return escape(network.getCyRow().get("name", String.class));
	}
}
