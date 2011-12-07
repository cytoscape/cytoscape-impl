/*
 File: PluginManager.java 
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

import org.cytoscape.app.internal.action.PluginManagerAction;
import org.cytoscape.app.internal.util.FileUtil;
import org.cytoscape.app.internal.util.ZipUtil;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList; // import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author skillcoy
 * 
 */
public class PluginManager {
	protected PluginTracker pluginTracker;

	private boolean duplicateLoadError;

	private List<String> duplicateClasses;

	private static PluginManager pluginMgr = null;

	private static File tempDir;

	private static List<java.net.URL> pluginURLs;

	private static List<String> resourcePlugins;

	private static Set<String> loadedPlugins;

	private static Set<Throwable> loadingErrors;

	private static HashMap<String, PluginInfo> initializedPlugins;

	private static URLClassLoader classLoader;

	private static boolean usingWebstart;

	private static String cyVersion;// = new CytoscapeVersion().getMajorVersion();

	private static final Logger logger = LoggerFactory.getLogger(PluginTracker.class);
	private CyAppAdapter adapter = null;
	
	/**
	/**
	 * Returns list of loading exceptions.
	 */
	public List<Throwable> getLoadingErrors() {
		if (pluginTracker.hasCorruptedElements()) {
			loadingErrors
					.add(new TrackerException(
							"Corrupted elements removed from the Plugin Tracker.  Some plugins may need to be reinstalled."));
		}
		return new ArrayList<Throwable>(loadingErrors);
	}

	/**
	 * Clears the loading error list. Ideally this should be called after
	 * checking the list each time.
	 */
	public void clearErrorList() {
		pluginTracker.clearCorruptedElements();
		loadingErrors.clear();
	}

	/**
	 * @return URLClassLoader used to load plugins at startup.
	 */
	public static URLClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * @return Set<String> of resource plugins from startup
	 */
	public static List<String> getResourcePlugins() {
		return resourcePlugins;
	}

	/**
	 * @return Set<URL> of plugin URL's from startup
	 */
	public static List<java.net.URL> getPluginURLs() {
		return pluginURLs;
	}

	/**
	 * Returns true/false based on the System property. This is what is checked
	 * to find out if install/delete/download methods are permitted.
	 * 
	 * @return true if Cytoscape is in webstart
	 */
	public static boolean usingWebstartManager() {
		return usingWebstart;
	}

	/**
	 * Deletes everything under the webstart install directory. Nothing in here
	 * should stick around.
	 * 
	 * @return True if all files deleted successfully
	 */
	protected boolean removeWebstartInstalls() {
		if (tempDir == null) {
			logger.warn("Directory not yet set up, can't delete");
			return false;
		}
		return recursiveDeleteFiles(tempDir.getParentFile());
	}

	/**
	 * Get the PluginManager object.
	 * 
	 * @return PluginManager
	 */
	public static PluginManager getPluginManager() {
		if (pluginMgr == null) {
			pluginMgr = new PluginManager(null, null);
		}
		return pluginMgr;
	}

	/**
	 * @param loc
	 *            Location of plugin download/install directory. If this method
	 *            is not called the default is .cytoscape/[cytoscape
	 *            version]/plugins
	 */
	public static void setPluginManageDirectory(String loc) {
		tempDir = new File(loc);
		if (!tempDir.getAbsolutePath().contains(cyVersion)) {
			tempDir = new File(tempDir, cyVersion);
		}
	}

	/**
	 * 
	 * @return The current version directory under .cytoscape that includes the
	 *         plugins/ directory.
	 * 
	 * Ex. /<user dir>/.cytoscape/2.6/plugins
	 */
	public File getPluginManageDirectory() {
		return tempDir;
	}

	/*
	 * Just checks the system property 'javawebstart.version' which is only set
	 * when running as a webstart.
	 */
	private static void setWebstart() {
		if (System.getProperty("javawebstart.version") != null
				&& System.getProperty("javawebstart.version").length() > 0) {
			logger.info("USING WEBSTART: "
					+ System.getProperty("javawebstart.version"));
			usingWebstart = true;
		} else {
			usingWebstart = false;
		}
	}

	/**
	 * This should ONLY be used by tests!!
	 * 
	 * @param Tracker
	 * @return
	 */
	protected static PluginManager getPluginManager(PluginTracker Tracker, final CyApplicationConfiguration config) {
		if (pluginMgr == null) {
			pluginMgr = new PluginManager(Tracker, config);
		}
		return pluginMgr;
	}

	/**
	 * This is used in testing to isolate each test case. DO NOT USE THIS IN
	 * CYTOSCAPE RUNTIME CODE
	 */
	protected void resetManager() {
		if (pluginTracker != null && pluginMgr != null) {
			pluginTracker.delete();
			pluginTracker = null;
			recursiveDeleteFiles(tempDir);
			pluginMgr = null;
		}
	}

	
	private PluginManager() {
		
	}
	
	// create plugin manager
	private PluginManager(PluginTracker tracker, final CyApplicationConfiguration config) {
		
		cyVersion = new Integer(PluginManagerAction.cyVersion.getMajorVersion()).toString();
		
		// XXX is this needed anymore?
		loadingErrors = new HashSet<Throwable>();

		setWebstart();
		String trackerFileName = "track_plugins.xml";

		String cyConfigVerDir = PluginManagerAction.cyConfigVerDir;
		
		if (tempDir == null) {
			if (usingWebstartManager()) {
				tempDir = new File(config.getSettingLocation(),
						"webstart" + File.separator
								+ PluginManagerAction.cyVersion.getMajorVersion()
								+ File.separator + "plugins");
				removeWebstartInstalls();
				trackerFileName = "track_webstart_plugins.xml";
			} else {
				tempDir = new File(cyConfigVerDir,"plugins");
			}
		} else if (!tempDir.getAbsolutePath().endsWith("/plugins")) {
			tempDir = new File(tempDir, "plugins");
		}

		if (!tempDir.exists()) {
			logger.info("Creating directories for " + tempDir.getAbsolutePath());
			if (!tempDir.mkdirs()) {
				logger.warn("Failed to create directory --- "+ tempDir.getAbsolutePath());
			}
		}

		if (tracker != null) {
			pluginTracker = tracker;
		} else {
			try {
				pluginTracker = new PluginTracker(tempDir.getParentFile(),
						trackerFileName);
			} catch (IOException ioe) {
				// ioe.printStackTrace();
				loadingErrors.add(ioe);
			} catch (TrackerException te) {
				// te.printStackTrace();
				loadingErrors.add(te);
			} finally { // document should be cleaned out by now
				try {
					pluginTracker = new PluginTracker(tempDir.getParentFile(),
							trackerFileName);
				} catch (Exception e) {
					logger.warn("Unable to read plugin tracking file", e);
					// this could go on forever, surely there's a better way!
				}
			}
		}
		pluginURLs = new ArrayList<java.net.URL>();
		loadedPlugins = new HashSet<String>();
		initializedPlugins = new HashMap<String, PluginInfo>();
		resourcePlugins = new ArrayList<String>();
	}

	/**
	 * Get a list of downloadable objects by status. CURRENT: currently
	 * installed INSTALL: objects to be installed DELETE: objects to be deleted
	 * 
	 * @param status
	 * @return
	 */
	public List<DownloadableInfo> getDownloadables(PluginStatus status) {
		return pluginTracker.getDownloadableListByStatus(status);
	}

	/**
	 * Calls the given url, expects document describing plugins available for
	 * download
	 * 
	 * @param url
	 * @return List of PluginInfo objects
	 */
	public List<DownloadableInfo> inquire(String url) throws IOException,
			org.jdom.JDOMException {	
		
		List<DownloadableInfo> infoObjs = null;
		PluginFileReader reader = new PluginFileReader(url);
		infoObjs = reader.getDownloadables();
		return infoObjs;
	}

	/**
	 * Registers a currently installed plugin with tracking object. Only useful
	 * if the plugin was not installed via the install process.
	 * 
	 * @param plugin
	 * @param JarFileName
	 */
	protected void register(CytoscapePlugin plugin, JarFile jar) {
		logger.info("Registering " + plugin.toString());

		DownloadableInfo infoObj = ManagerUtil.getInfoObject(plugin.getClass());
		if (infoObj != null && infoObj.getType().equals(DownloadableType.THEME)) {
			this.registerTheme(plugin, jar, (ThemeInfo) infoObj);
		} else {
			this.registerPlugin(plugin, jar, (PluginInfo) infoObj, true);
		}
	}

	private PluginInfo registerPlugin(CytoscapePlugin plugin, JarFile jar,
			PluginInfo pluginObj, boolean addToTracker) {
		// try to get it from the file
		// XXX PROBLEM: what to do about a plugin that attempts to register
		// itself and is not compatible with the current version?
		logger.info("     Registering " + plugin.getClass().getName());
		try {
			PluginProperties pp = new PluginProperties(plugin);
			pluginObj = pp.fillPluginInfoObject(pluginObj);

		} catch (IOException ioe) {
			logger.warn("ERROR registering plugin: " + ioe.getMessage(), ioe);
			logger.warn(plugin.getClass().getName()
							+ " loaded but not registered, this will not affect the operation of the plugin");
		} catch (Exception e) {
			logger.warn("ERROR registering plugin: ", e);
		} finally {
			if (pluginObj == null) { // still null, create a default one
				pluginObj = new PluginInfo();
				pluginObj.addCytoscapeVersion(cyVersion);
				pluginObj.setName(plugin.getClass().getName());
				pluginObj.setObjectVersion("0.1");
			}

			pluginObj.setPluginClassName(plugin.getClass().getName());
			if (!usingWebstart && jar != null) {
				pluginObj.setInstallLocation(jar.getName());
				pluginObj.addFileName(jar.getName());
			}
			pluginObj.setFiletype(PluginInfo.FileType.JAR);

			initializedPlugins.put(pluginObj.getPluginClassName(), pluginObj);
			// TODO This causes a bug where theme plugins essentially get added
			// to the current list twice
			logger.info("Track plugin: " + addToTracker);
			if (addToTracker) {
				pluginTracker.addDownloadable(pluginObj, PluginStatus.CURRENT);
			}
		}
		return pluginObj;
	}

	private void registerTheme(CytoscapePlugin inputPlugin, JarFile jar,
			ThemeInfo themeObj) {
		logger.info("--- Registering THEME " + themeObj.getName());
		for (PluginInfo plugin : themeObj.getPlugins()) {
			if (plugin.getPluginClassName().equals(inputPlugin.getClass().getName())) {
				logger.info(plugin.getName());
				PluginInfo updatedPlugin = registerPlugin(inputPlugin, jar, plugin, false);
				themeObj.replacePlugin(plugin, updatedPlugin);
			}
		}
		pluginTracker.addDownloadable(themeObj, PluginStatus.CURRENT);
	}

	// TODO would be better to fix how initializedPlugins are tracked...
	private void cleanCurrentList() {
		List<DownloadableInfo> currentList = getDownloadables(PluginStatus.CURRENT);
		for (DownloadableInfo info : currentList) {
			if (info.getType().equals(DownloadableType.PLUGIN)) {
				PluginInfo pInfo = (PluginInfo) info;
				if (!initializedPlugins.containsKey(pInfo.getPluginClassName())) {
					pluginTracker.removeDownloadable(info, PluginStatus.CURRENT);
				}
			}
		}
	}

	/**
	 * Sets all plugins on the "install" list to "current"
	 */
	public void install() {
		for (DownloadableInfo info : getDownloadables(PluginStatus.INSTALL)) {
			install(info);
		}
	}

	/**
	 * Change the given downloadable object from "install" to "current" status
	 * 
	 * @param obj
	 */
	public void install(DownloadableInfo obj) {
		pluginTracker.removeDownloadable(obj, PluginStatus.INSTALL);
		pluginTracker.addDownloadable(obj, PluginStatus.CURRENT);

		// mark all webstart-installed plugins for deletion
		if (usingWebstartManager()) {
			pluginTracker.addDownloadable(obj, PluginStatus.DELETE);
		}
	}

	/**
	 * Marks the given object for deletion the next time Cytoscape is restarted.
	 * 
	 * @param Obj
	 */
	public void delete(DownloadableInfo obj) throws WebstartException {
		checkWebstart();
		pluginTracker.addDownloadable(obj, PluginStatus.DELETE);
	}

	/**
	 * Takes all objects on the "to-delete" list and deletes them. This can only
	 * occur at start up.
	 * 
	 * @throws ManagerException
	 *             If all files fail to delete
	 * @throws WebstartException
	 *             If this method is called from a webstart instance
	 */
	public void delete() throws ManagerException {
		List<DownloadableInfo> toDelete = pluginTracker.getDownloadableListByStatus(PluginStatus.DELETE);

		for (DownloadableInfo infoObj : toDelete) {
			Installable ins = infoObj.getInstallable();

			try {
				if (ins.uninstall()) {
					pluginTracker.removeDownloadable(infoObj, PluginStatus.DELETE);
					pluginTracker.removeDownloadable(infoObj, PluginStatus.CURRENT);
				} // TODO um.....XXXX
			} catch (Exception me) {
				throw new ManagerException( 
				          "Failed to completely delete the following installed components:\n" + 
						  infoObj.getName() + " v" + infoObj.getObjectVersion() + "\n", me);
			}
		}
	}

	protected static boolean recursiveDeleteFiles(File file) {
		if (file.isDirectory())
			for (File f : file.listFiles())
				recursiveDeleteFiles(f);

		boolean del = file.delete();
		// Utterly f*#king retarded, but apparently necessary since sometimes
		// directories don't realize they're empty...
		if (!del) {
			for (int i = 0; i < 1000 && file.exists(); i++) {
				System.gc();
				del = file.delete();
			}
		}

		return del;
	}

	private void checkWebstart() throws WebstartException {
		if (usingWebstart) {
			throw new WebstartException();
		}
	}

	/**
	 * Get list of plugins that would update the given plugin.
	 * 
	 * @param info
	 * @return List<PluginInfo>
	 * @throws ManagerException
	 */
	public List<DownloadableInfo> findUpdates(DownloadableInfo info)
			throws IOException, org.jdom.JDOMException {
		return info.getInstallable().findUpdates();
	}

	/**
	 * Finds the given version of the new object, sets the old object for
	 * deletion and downloads new object to temporary directory
	 * 
	 * @param current
	 *            DownloadableInfo object currently installed
	 * @param newDownload
	 *            DownloadableInfo object to install
	 * @throws IOException
	 *             Fails to download the file.
	 * @throws ManagerException
	 *             If the objects don't match or the new one is not a newer
	 *             version.
	 */
	public void update(DownloadableInfo current, DownloadableInfo newDownload)
			throws IOException, ManagerException, WebstartException {
		update(current, newDownload, null);
	}

	/**
	 * Finds the given version of the new object, sets the old object for
	 * deletion and downloads new object to temporary directory
	 * 
	 * @param Current
	 *            PluginInfo object currently installed
	 * @param New
	 *            PluginInfo object to install
	 * @param taskMonitor
	 *            TaskMonitor for downloads
	 * @throws IOException
	 *             Fails to download the file.
	 * @throws ManagerException
	 *             If the plugins don't match or the new one is not a newer
	 *             version.
	 */
	public void update(DownloadableInfo currentObj, DownloadableInfo newObj,
			TaskMonitor taskMonitor) throws IOException,
			ManagerException, WebstartException {

		if (!currentObj.getType().equals(newObj.getType())) {
			throw new ManagerException(
					"Cannot update an object of one download type to an object of a different download type");
		}
		currentObj.getInstallable().update(newObj, taskMonitor);

		pluginTracker.addDownloadable(currentObj, PluginStatus.DELETE);
		pluginTracker.addDownloadable(newObj, PluginStatus.INSTALL);
	}

	/**
	 * Downloads given object to the temporary directory.
	 * 
	 * @param obj
	 *            PluginInfo object to be downloaded
	 * @return File downloaded
	 */
	public DownloadableInfo download(DownloadableInfo obj) throws IOException,
			ManagerException {
		return this.download(obj, null);
	}

	/**
	 * Downloads given object to the temporary directory. Uses a task monitor if
	 * available.
	 * 
	 * @param obj
	 *            PluginInfo object to be downloaded
	 * @param taskMonitor
	 *            TaskMonitor
	 * @param tempDirectory
	 *            Download to a different temporary directory. Default is
	 *            .cytoscape/plugins/[cytoscape version number]
	 * @return File downloaded
	 */
	public DownloadableInfo download(DownloadableInfo obj,
			TaskMonitor taskMonitor) throws IOException, ManagerException {
		// run a check for plugins 
		List<DownloadableInfo> currentAndInstalled = new ArrayList<DownloadableInfo>();
		currentAndInstalled.addAll(this.getDownloadables(PluginStatus.CURRENT));
		currentAndInstalled.addAll(this.getDownloadables(PluginStatus.INSTALL));
		
		List<DownloadableInfo> flattenedList = this.flattenDownloadableList(currentAndInstalled);
		
		for (DownloadableInfo currentlyInstalled : flattenedList) {
			DownloadableInfo xCurrentlyInstalled = null;
			if (currentlyInstalled.getParent() != null) {
				xCurrentlyInstalled = currentlyInstalled.getParent();
			} else {
				xCurrentlyInstalled = currentlyInstalled;
			}
				
			if (obj.equals(currentlyInstalled) || obj.equalsDifferentObjectVersion(currentlyInstalled)) {
				throw new ManagerException(obj.toString() + " cannot be installed, it is already loaded in: " + xCurrentlyInstalled.toString());
			}
				
			if (obj.getType().equals(DownloadableType.THEME)) {
				for (PluginInfo themePlugin: ((ThemeInfo) obj).getPlugins()) {
					if (themePlugin.equalsDifferentObjectVersion(currentlyInstalled)) {
						throw new ManagerException(obj.toString() + " cannot be installed a plugin contained within the theme is already present: " 
								+ xCurrentlyInstalled.toString());
					}
				}
			}
		}
		Installable installable = obj.getInstallable();
		installable.install(taskMonitor);
		pluginTracker.addDownloadable(obj, PluginStatus.INSTALL);
		return installable.getInfoObj();
	}

	private List<DownloadableInfo> flattenDownloadableList(List<DownloadableInfo> list) {
		List<DownloadableInfo> flattenedList = new ArrayList<DownloadableInfo>();
		for (DownloadableInfo info: list) {
			switch (info.getType()) {
			case THEME:
				flattenedList.addAll(((ThemeInfo) info).getPlugins());
			case PLUGIN:
				flattenedList.add(info);
			}
		}
		return flattenedList;
	}
	
	/*
	 * Methods for loading plugins when Cytoscape starts up.
	 */
	public void loadPlugin(DownloadableInfo i) throws MalformedURLException,
			IOException, ClassNotFoundException, PluginException {
		switch (i.getType()) {
		case PLUGIN:
			loadPlugin((PluginInfo) i);
			break;
		case THEME:
			ThemeInfo Info = (ThemeInfo) i;
			for (PluginInfo p : Info.getPlugins())
				loadPlugin(p);
			break;
		case FILE: // currently there is no FileInfo type
			break;
		}
	}

	/**
	 * Load a single plugin based on the PluginInfo object given
	 * 
	 * @param PluginInfo
	 *            The plugin to load
	 * @throws ManagerException
	 */
	public void loadPlugin(PluginInfo p) throws ManagerException {
		List<URL> toLoad = new ArrayList<URL>();

		for (String FileName : p.getFileList()) {
			
			if (FileName.endsWith(".jar")) {
//								
//				Object o = null; 
//				JarFile jar = null; 
//				try {
//
//					File filename = new File(FileName);
//					
//					jar = new JarFile(filename);
//					String name = jar.getManifest().getMainAttributes().getValue("Cytoscape-Plugin");
//					URL jarurl = filename.toURI().toURL(); 
//					
//					URLClassLoader ucl = URLClassLoader.newInstance( new URL[]{jarurl}, 
//					                                      PluginLoaderTask.class.getClassLoader() );
//					Class c = ucl.loadClass(name);
//					Constructor<CyPlugin> con = c.getConstructor(CyPluginAdapter.class);
//					o = con.newInstance(adapter);
//				}
//				catch (Exception e){
//					e.printStackTrace();
//				}
				
				PluginLoaderTask2 task = new PluginLoaderTask2(adapter);
				task.setFile(new File(FileName));
				
				PluginLoaderTaskFactory2 factory = new PluginLoaderTaskFactory2();
				factory.setTask(task);
				this.guiTaskManagerServiceRef.execute(factory);				
			}
		}
		// don't need to register if we have the info object
		InstallablePlugin insp = new InstallablePlugin(p);

		if (duplicateLoadError) {
			insp.uninstall();
			pluginTracker.removeDownloadable(p, PluginStatus.CURRENT);
			addDuplicateError();
		}

	}

	private DialogTaskManager guiTaskManagerServiceRef;
	
	public void setTaskManager(DialogTaskManager guiTaskManagerServiceRef){
		this.guiTaskManagerServiceRef = guiTaskManagerServiceRef;
	}

	
	
	private void addDuplicateError() {
		String Msg = "The following plugins were not loaded due to duplicate class definitions:\n";
		for (String dup : duplicateClasses)
			Msg += "\t" + dup + "\n";
		logger.warn(Msg);
		loadingErrors.add(new DuplicatePluginClassException(Msg));
	}

	
	public void setCyPluginAdapter(CyAppAdapter adapter){
		this.adapter = adapter;
	}

	public CyAppAdapter getCyPluginAdapter(){
		return adapter;
	}
	
}
