/*
 File: CytoscapePlugin.java

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
package org.cytoscape.app.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CytoscapePlugin is the new "Global" plugin. A CytoscapePlugin constructor
 * does not have any arguments, since it is Network agnostic. Instead all access
 * to the Cytoscape Data Structures is handled throught the static methods
 * provided by cytoscape.Cytoscape.
 * 
 * It is encouraged, but not mandatory, for plugins to override the
 * {@link #describe describe} method to state what the plugin does and how it
 * should be used.
 */
public abstract class CytoscapePlugin implements PropertyChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(CytoscapePlugin.class);

	/**
	 * There are no arguments required or allowed in a CytoscapePlugin
	 * constructor.
	 */
	public CytoscapePlugin() {
		//Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
		//		Cytoscape.SAVE_PLUGIN_STATE, this);
		//Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
		//		Cytoscape.RESTORE_PLUGIN_STATE, this);
		//Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
		//		Cytoscape.CYTOSCAPE_EXIT, this);
	}

	/**
	 * If true, this plugin is capable if accepting scripts, and we will find
	 * out what its script name is
	 */
	public boolean isScriptable() {
		return false;
	}

	/**
	 * If this plugin is scriptable, then this will return a unique script name,
	 * that will come after the colon like: :name
	 */
	public String getScriptName() {
		return "default";
	}

	/**
	 * Take a CyNetwork as input along with some arguments, and return a
	 * CyNetwork, which can be the same, or different, it doesn't really matter,
	 * and is up to the individual plugin.
	 */
	public CyNetwork interpretScript(String[] args, CyNetwork network) {
		return null;
	}

	/**
	 * If implemented, then this plugin will be activated after being
	 * initialized
	 */
	public void activate() {
	}

	/**
	 * If implemented then this plugin can remove itself from the Menu system,
	 * and anything else, when the user decides to deactivate it.
	 */
	public void deactivate() {
	}

	/**
	 * Attempts to instantiate a plugin of the class argument.
	 * 
	 * @return The object, if it was not successfully constructed object will be
	 *         null
	 * @return true if the plugin was successfulyl constructed, false otherwise
	 */
	public static Object loadPlugin(Class pluginClass)
			//throws InstantiationException, IllegalAccessException,
      throws PluginException {
		logger.info(pluginClass.getName());

    if (pluginClass == null) {
			return false;
		}

		Object object = null;

		try {
			object = pluginClass.newInstance();
			// We want to catch everything possible. Errors will cause the
			// entire
			// cytoscape app to crash, which a plugin should not do.
		} catch (Throwable e) {
			object = null;
			// logger.error("Instantiation has failed for");
			// Here's a bit of Java strangeness: newInstance() throws
			// two exceptions (above) -- however, it also propagates any
			// exception that occurs during the creation of that new instance.
			// Here, we need to catch whatever other exceptions might be thrown --
			// for example, attempting to load an older plugin that looks
			// for the class cytoscape.CyWindow, which is no longer defined,
			// propagates a ClassNotFoundException (which, if we don't
			// catch causes the application to crash).
			String ErrorMsg = "Unchecked '" + e.getClass().getName() + "'exception while attempting to load plugin " + pluginClass.getName() + ".\n"  
					+ "This may happen when loading a plugin written for a different \n"
					+ "version of Cytoscape than this one, or if the plugin is dependent \n"
					+ "on another plugin that isn't available. Consult the documentation \n"
					+ "for the plugin or contact the plugin author for more information.";

			//CyLogger.getLogger().warn(ErrorMsg);
			// CyLogger.getLogger().warn(e);
			// e.printStackTrace();
			throw new PluginException(ErrorMsg, e);
		} 
		logger.info("Plugin successfully loaded");
		return object;
	}

	private HashMap<String, List<File>> pluginFileListMap;

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void propertyChange(PropertyChangeEvent e) {
		/*
		String pluginName = this.getClass().getName();
		int index = pluginName.lastIndexOf(".");
		pluginName = pluginName.substring(index + 1);

		if (e.getPropertyName().equalsIgnoreCase(Cytoscape.SAVE_PLUGIN_STATE)) {
			pluginFileListMap = (HashMap<String, List<File>>) e.getOldValue();

			List<File> newfiles = new ArrayList<File>();
			saveSessionStateFiles(newfiles);

			if (newfiles.size() > 0) {
				pluginFileListMap.put(pluginName, newfiles);
			}
		} else if (e.getPropertyName().equalsIgnoreCase(
				Cytoscape.RESTORE_PLUGIN_STATE)) {
			pluginFileListMap = (HashMap<String, List<File>>) e.getOldValue();

			if (pluginFileListMap.containsKey(pluginName)) {
				List<File> theFileList = pluginFileListMap.get(pluginName);

				if ((theFileList != null) && (theFileList.size() > 0)) {
					restoreSessionState(theFileList);
				}
			}
		} else if (e.getPropertyName().equalsIgnoreCase(
				Cytoscape.CYTOSCAPE_EXIT)) {
			onCytoscapeExit();
		}
		*/
	}

	public void onCytoscapeExit() {

	}

	// override the following two methods to save state.
	/**
	 * DOCUMENT ME!
	 * 
	 * @param pStateFileList
	 *            DOCUMENT ME!
	 */
	public void restoreSessionState(List<File> pStateFileList) {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param pFileList
	 *            DOCUMENT ME!
	 */
	public void saveSessionStateFiles(List<File> pFileList) {
	}
}
