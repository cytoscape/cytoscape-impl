/**
 * 
 */
package org.cytoscape.app.internal;

/**
 * @author skillcoy
 *
 */
public enum PluginXml {
	// basic tags
	NAME("name"), DESCRIPTION("description"),
	CLASS_NAME("classname"), UNIQUE_ID("uniqueID"),
	CATEGORY("category"), FILE_TYPE("filetype"),
	INSTALL_LOCATION("installLocation"),
	RELEASE_DATE("releaseDate"),
	// versions
	PLUGIN_VERSION("pluginVersion"),
	THEME_VERSION("themeVersion"),
	CYTOSCAPE_VERSIONS("cytoscapeVersions"), 
	VERSION("version"), 
	// url tags
	URL("url"), 
	PROJECT_URL("projectUrl"), 
	DOWNLOAD_URL("downloadUrl"),
	// more specific tags for the lists 
	FILE_LIST("filelist"), FILE("file"),
	THEME_LIST("themes"), THEME("theme"),
	PLUGIN_LIST("pluginlist"), PLUGIN("plugin"), 
	AUTHOR_LIST("authorlist"), AUTHOR("author"), INSTITUTION("institution"),
	// license  
	LICENSE("license"), LICENSE_TEXT("text"), LICENSE_REQUIRED("license_required");

	private String tagName;

	private PluginXml(String Name) {
		tagName = Name;
	}

	public String getTag() {
		return tagName;
	}
	
}
