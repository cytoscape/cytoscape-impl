/*
 File: PluginInfo.java 
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
package org.cytoscape.plugin.internal;

//import cytoscape.util.URLUtil;

import java.util.ArrayList;
import java.util.List;

import java.net.URL;

/**
 * Object describes a plugin
 */
public class PluginInfo extends DownloadableInfo {
	/**
	 * Jar and Zip files currently supported
	 * 
	 * @author skillcoy
	 * 
	 */
	public enum FileType {
		JAR("jar"), ZIP("zip");

		private String typeText;

		private FileType(String type) {
			typeText = type;
		}

		public String toString() {
			return typeText;
		}
	}

	private FileType fileType;

	private String pluginClassName;

	private List<AuthorInfo> authors;

	private String projectUrl;

	private List<String> pluginFiles;

	protected String enclosingJar;
	
	protected String installLocation;
	
	
	/**
	 * See {@link DownloadableInfo#DownloadableInfo()}
	 * 
	 * Initializes a PluginInfo object with the following defaults:
	 * setName("Unknown"); setDescription("No description");
	 * setObjectVersion("0.1"); setCytoscapeVersion(
	 * cytoscape.cytoscapeVersion.version ); setCategory("Uncategorized");
	 * 
	 */
	public PluginInfo() {
		init();
	}

	/**
	 * See {@link DownloadableInfo#DownloadableInfo(String)}
	 * 
	 * @param UniqueID
	 *            Additionally this sets the unique identifier that will be used
	 *            to find a new version of the plugin at the given download url.
	 */
	public PluginInfo(String UniqueID) {
		super(UniqueID);
		init();
	}

	public PluginInfo(String UniqueID, String Name) {
		super(UniqueID);
		init();
		this.setName(Name);
	}
	
	/**
	 * See {@link DownloadableInfo#DownloadableInfo(String)}
	 * 
	 * @param UniqueID
	 * @param ParentObj
	 *            Additionally this sets the unique identifier that will be used
	 *            to find a new version of the plugin at the given download url and
	 *            sets the parent downloadable object.
	 */
	public PluginInfo(String UniqueID, DownloadableInfo ParentObj) {
		super(UniqueID, ParentObj);
		init();
	}
		
	/*
	 * Sets all the fields that are required to a default value in case it is
	 * not called
	 */
	private void init() {
		pluginFiles = new ArrayList<String>();
		authors = new ArrayList<AuthorInfo>();
		setName("Unknown");
		setDescription("No description");
		setObjectVersion(0.1);
		setCategory(Category.NONE);
		setPluginClassName("");
	}

	/**
	 * Sets the plugin class name. Used for tracking plugins.
	 * 
	 * @param className
	 */
	public void setPluginClassName(String className) {
		pluginClassName = className;
	}


	/**
	 * Sets the url of a site describing this plugin project
	 * @param url
	 */
	public void setProjectUrl(String url) {
		projectUrl = url;
	}

	/**
	 * Jar or Zip are currently supported. Use PluginInfo.JAR or PluginInfo.ZIP.
	 * This will only be set by the PluginManager generally and can only be set once 
	 * as an object's file type will not change.
	 * @param type
	 */
	protected void setFiletype(FileType type) {
		if (fileType == null)
			fileType = type;
	}

	/**
	 * Sets a list of files (prefer full paths) installed with this plugin.
	 * Includes the jar file.
	 * @param list
	 */
	protected void setFileList(List<String> list) {
		pluginFiles = list;
	}

	/**
	 * Adds a file to the list of installed files.
	 * 
	 * @param fileName
	 */
	protected void addFileName(String fileName) {
		if (!pluginFiles.contains(fileName))
			pluginFiles.add(fileName);
	}

	/**
	 * Adds an author to the list of authors.
	 * 
	 * @param authorName
	 * @param institution
	 */
	public void addAuthor(String authorName, String institution) {
		authors.add(new AuthorInfo(authorName, institution));
	}

	/**
	 * Clears author list.
	 */
	public void clearAuthorList() {
		authors.clear();
	}
	

	/**
	 * This is meant to only get set by the PluginManager.  It can only
	 * be set once as the install location can't move.
	 * 
	 * @param Loc
	 */
	protected void setInstallLocation(String Loc) {
		if (installLocation == null)
			installLocation = Loc;
	}
	
	/* GET */
	/**
	 * Gets the full install path for this plugin.  Should look like
	 * <HOME_DIR>/.cytoscape/<cytoscape_version>/plugins/<pluginName-pluginVersion>
	 */
	public java.io.File getPluginDirectory() {
		 java.io.File PluginDir = new java.io.File(
				 PluginManager.getPluginManager().getPluginManageDirectory(),
				 this.getName()+"-"+this.getObjectVersion());
		return PluginDir;
	}

	/**
	 * @return String of the installation location for the plugin and all of it's files.
	 * 		Generally this is .cytoscape/[cytoscape version]/plugins/PluginName-version
	 */
	public String getInstallLocation() {
		return installLocation;
	}
	
	/**
	 * @return FileType of file type for plugin. PluginInfo.JAR or
	 *         PluginInfo.ZIP
	 */
	public FileType getFileType() {
		return fileType;
	}

	/**
	 * @return Java class name
	 */
	public String getPluginClassName() {
		return pluginClassName;
	}

	/**
	 * @return List of authors.
	 */
	public List<AuthorInfo> getAuthors() {
		return authors;
	}

	/**
	 * 
	 * @return Url that points to a site describing this plugin project
	 */
	public String getProjectUrl() {
		return projectUrl;
	}

	public Installable getInstallable() {
		return new InstallablePlugin(this);
	}
	
	/**
	 * {@link DownloadableInfo#getType()}
	 */
	public DownloadableType getType() {
		return DownloadableType.PLUGIN;
	}
	
	/**
	 * @return List of files installed with this plugin (includes plugin jar
	 *         file).
	 */
	public List<String> getFileList() {
		return pluginFiles;
	}

	public String htmlOutput() {
		
		String Html = this.basicHtmlOutput();
		Html += "<b>Released By:</b><br><ul>";
		for (AuthorInfo ai : getAuthors()) {
			Html += "<li>" + ai.getAuthor() + ", " + ai.getInstitution()
					+ "<br>";
		}
		Html += "</ul>";

		Html += "</font></body></html>";
		return Html;
	}

	/**
	 * Describes an author for a given plugin.
	 * 
	 */
	public class AuthorInfo {
		private String authorName;

		private String institutionName;

		public AuthorInfo(String Name, String Institution) {
			authorName = Name;
			institutionName = Institution;
		}

		public String getAuthor() {
			return authorName;
		}

		public String getInstitution() {
			return institutionName;
		}
	}


}
