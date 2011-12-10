/*
 File: CytoscapeApp.java

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
 * A CytoscapeApp is the new "Global" app. A CytoscapeApp constructor
 * does not have any arguments, since it is Network agnostic. Instead all access
 * to the Cytoscape Data Structures is handled throught the static methods
 * provided by cytoscape.Cytoscape.
 * 
 * It is encouraged, but not mandatory, for apps to override the
 * {@link #describe describe} method to state what the app does and how it
 * should be used.
 */
public abstract class CytoscapeApp implements PropertyChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(CytoscapeApp.class);

	/**
	 * There are no arguments required or allowed in a CytoscapeApp
	 * constructor.
	 */
	public CytoscapeApp() {
		//Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
		//		Cytoscape.SAVE_APP_STATE, this);
		//Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
		//		Cytoscape.RESTORE_APP_STATE, this);
		//Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
		//		Cytoscape.CYTOSCAPE_EXIT, this);
	}

	/**
	 * If true, this app is capable if accepting scripts, and we will find
	 * out what its script name is
	 */
	public boolean isScriptable() {
		return false;
	}

	/**
	 * If this app is scriptable, then this will return a unique script name,
	 * that will come after the colon like: :name
	 */
	public String getScriptName() {
		return "default";
	}

	/**
	 * Take a CyNetwork as input along with some arguments, and return a
	 * CyNetwork, which can be the same, or different, it doesn't really matter,
	 * and is up to the individual app.
	 */
	public CyNetwork interpretScript(String[] args, CyNetwork network) {
		return null;
	}

	/**
	 * If implemented, then this app will be activated after being
	 * initialized
	 */
	public void activate() {
	}

	/**
	 * If implemented then this app can remove itself from the Menu system,
	 * and anything else, when the user decides to deactivate it.
	 */
	public void deactivate() {
	}

	/**
	 * Attempts to instantiate a app of the class argument.
	 * 
	 * @return The object, if it was not successfully constructed object will be
	 *         null
	 * @return true if the app was successfulyl constructed, false otherwise
	 */
	public static Object loadApp(Class appClass)
			//throws InstantiationException, IllegalAccessException,
      throws AppException {
		logger.info(appClass.getName());

    if (appClass == null) {
			return false;
		}

		Object object = null;

		try {
			object = appClass.newInstance();
			// We want to catch everything possible. Errors will cause the
			// entire
			// cytoscape app to crash, which a app should not do.
		} catch (Throwable e) {
			object = null;
			// logger.error("Instantiation has failed for");
			// Here's a bit of Java strangeness: newInstance() throws
			// two exceptions (above) -- however, it also propagates any
			// exception that occurs during the creation of that new instance.
			// Here, we need to catch whatever other exceptions might be thrown --
			// for example, attempting to load an older app that looks
			// for the class cytoscape.CyWindow, which is no longer defined,
			// propagates a ClassNotFoundException (which, if we don't
			// catch causes the application to crash).
			String ErrorMsg = "Unchecked '" + e.getClass().getName() + "'exception while attempting to load app " + appClass.getName() + ".\n"  
					+ "This may happen when loading a app written for a different \n"
					+ "version of Cytoscape than this one, or if the app is dependent \n"
					+ "on another app that isn't available. Consult the documentation \n"
					+ "for the app or contact the app author for more information.";

			//CyLogger.getLogger().warn(ErrorMsg);
			// CyLogger.getLogger().warn(e);
			// e.printStackTrace();
			throw new AppException(ErrorMsg, e);
		} 
		logger.info("App successfully loaded");
		return object;
	}

	private HashMap<String, List<File>> appFileListMap;

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void propertyChange(PropertyChangeEvent e) {
		/*
		String appName = this.getClass().getName();
		int index = appName.lastIndexOf(".");
		appName = appName.substring(index + 1);

		if (e.getPropertyName().equalsIgnoreCase(Cytoscape.SAVE_APP_STATE)) {
			appFileListMap = (HashMap<String, List<File>>) e.getOldValue();

			List<File> newfiles = new ArrayList<File>();
			saveSessionStateFiles(newfiles);

			if (newfiles.size() > 0) {
				appFileListMap.put(appName, newfiles);
			}
		} else if (e.getPropertyName().equalsIgnoreCase(
				Cytoscape.RESTORE_APP_STATE)) {
			appFileListMap = (HashMap<String, List<File>>) e.getOldValue();

			if (appFileListMap.containsKey(appName)) {
				List<File> theFileList = appFileListMap.get(appName);

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
