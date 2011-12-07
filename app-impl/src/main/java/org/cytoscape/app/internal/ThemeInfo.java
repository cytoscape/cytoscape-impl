/*
 File: ThemeInfo.java 
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
package org.cytoscape.app.internal;

import java.util.*;

//import org.cytoscape.plugin.internal.PluginInfo.AuthorInfo;

public class ThemeInfo extends DownloadableInfo {
	private Set<PluginInfo> themePlugins;

	/**
	 * See {@link DownloadableInfo#DownloadableInfo()}
	 * 
	 * Initializes a ThemeInfo object with the following defaults:
	 * setName("Unknown"); setDescription("No description");
	 * setObjectVersion("0.1"); setCytoscapeVersion(
	 * cytoscape.cytoscapeVersion.version ); setCategory("Theme");
	 */
	public ThemeInfo() {
		init();
	}

	/**
	 * See {@link DownloadableInfo#DownloadableInfo(String)}
	 * 
	 * @param UniqueID
	 *            Additionally this sets the unique identifier that will be used
	 *            to find a new version of the theme at the given download url.
	 */
	public ThemeInfo(String ID) {
		super(ID);
		init();
	}

	private void init() {
		setName("Unknown");
		setDescription("No description");
		setObjectVersion("0.1");
		setCategory(Category.THEME);
		themePlugins = new HashSet<PluginInfo>();
	}

	public Installable getInstallable() {
		return null;// new InstallableTheme(this);
	}
	
	/**
	 * See {@link DownloadableInfo#getType()}
	 */
	public DownloadableType getType() {
		return DownloadableType.THEME;
	}

	public void replacePlugin(PluginInfo oldPlugin, PluginInfo newPlugin) {
		themePlugins.remove(oldPlugin);
		themePlugins.add(newPlugin);
	}
	
	/**
	 * @param plugin
	 *            Add a plugin object to this theme.
	 */
	public void addPlugin(PluginInfo plugin) {
		themePlugins.add(plugin);
	}

	/**
	 * @return All plugins that make up this theme.
	 */
	public List<PluginInfo> getPlugins() {
		return new ArrayList<PluginInfo>(themePlugins);
	}

	public boolean containsPlugin(PluginInfo plugin) {
		for (PluginInfo pi: themePlugins) {
			if (pi.equalsDifferentObjectVersion(plugin))
				return true;
		}
		return false;
	}
	
	public void clearPluginList() {
		this.themePlugins.clear();
	}
	
	public String getInstallLocation() {
		 java.io.File Dir = new java.io.File(
				 PluginManager.getPluginManager().getPluginManageDirectory(),
				 this.getName()+"-"+this.getObjectVersion());
		return Dir.getAbsolutePath();
	}
	
	public String htmlOutput() {
		String Html = this.basicHtmlOutput();
		
		Html += "<b>Plugins Included</b>:<br><ul>";
		for (PluginInfo i: getPlugins()) {
			Html += "<li>" + i.toString();
		}
		Html += "</ul>";

		Html += "</font></body></html>";
		return Html;
	}

}
