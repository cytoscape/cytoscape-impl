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
	public static final String PLUGINS_FOLDER = "plugins/";
	
	public static final String NETWORK_ROOT = "Network Root";
	
	public static final String ID_MAPPING_TABLE = "__TEMP_ENTRY_IDS";
	public static final String NETWORK_POINTERS_TABLE = "__TEMP_NODE_NETWORK_POINTERS";
	public static final String ORIGINAL_ID_COLUMN = "original_id";
	public static final String ENTRY_SUID_COLUMN = "entry_suid";
	public static final String INDEX_COLUMN = "index";
	public static final String NODE_SUID_COLUMN = "node_suid";
	public static final String ORIGINAL_NETWORK_ID_COLUMN = "original_network_id";
	
	private static boolean readingSessionFile;
	private static boolean writingSessionFile;
	
	private static Long idMappingTableSUID;
	private static Long networkPointersTableSUID;
	
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
		CyTable table = metadata.getCyTable();
		String networkFileName = getNetworkFileName(network);
		String namespace = escape(metadata.getNamespace());
		String type = escape(metadata.getType().getCanonicalName());
		String tableTitle = escape(table.getTitle());
		return String.format("%s/%s-%s-%s%s", networkFileName, namespace, type, tableTitle, TABLE_EXT);
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
		String name = escape(network.getCyRow().get(CyNetwork.NAME, String.class));
		
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

	public static Long getIdMappingTableSUID() {
		return idMappingTableSUID;
	}

	public static void setIdMappingTableSUID(Long idMappingTableSUID) {
		SessionUtil.idMappingTableSUID = idMappingTableSUID;
	}

	public static Long getNetworkPointersTableSUID() {
		return networkPointersTableSUID;
	}

	public static void setNetworkPointersTableSUID(Long networkPointersTableSUID) {
		SessionUtil.networkPointersTableSUID = networkPointersTableSUID;
	}
}
