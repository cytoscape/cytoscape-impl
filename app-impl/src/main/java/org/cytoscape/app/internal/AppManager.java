/*
 File: AppManager.java 
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.internal.action.AppManagerAction;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author skillcoy
 * 
 */
public class AppManager {
	protected AppTracker appTracker;

	private boolean duplicateLoadError;

	private List<String> duplicateClasses;

	private static AppManager appMgr = null;

	private static File tempDir;

	private static List<java.net.URL> appURLs;

	private static List<String> resourceApps;

	private static Set<String> loadedApps;

	private static Set<Throwable> loadingErrors;

	private static HashMap<String, AppInfo> initializedApps;

	private static URLClassLoader classLoader;

	private static boolean usingWebstart;

	private static String cyVersion;// = new CytoscapeVersion().getMajorVersion();

	private static final Logger logger = LoggerFactory.getLogger(AppTracker.class);
	private CyAppAdapter adapter = null;
	
	/**
	/**
	 * Returns list of loading exceptions.
	 */
	public List<Throwable> getLoadingErrors() {
		if (appTracker.hasCorruptedElements()) {
			loadingErrors
					.add(new TrackerException(
							"Corrupted elements removed from the App Tracker.  Some apps may need to be reinstalled."));
		}
		return new ArrayList<Throwable>(loadingErrors);
	}

	/**
	 * Clears the loading error list. Ideally this should be called after
	 * checking the list each time.
	 */
	public void clearErrorList() {
		appTracker.clearCorruptedElements();
		loadingErrors.clear();
	}

	/**
	 * @return URLClassLoader used to load apps at startup.
	 */
	public static URLClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * @return Set<String> of resource apps from startup
	 */
	public static List<String> getResourceApps() {
		return resourceApps;
	}

	/**
	 * @return Set<URL> of app URL's from startup
	 */
	public static List<java.net.URL> getAppURLs() {
		return appURLs;
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
	 * Get the AppManager object.
	 * 
	 * @return AppManager
	 */
	public static AppManager getAppManager() {
		if (appMgr == null) {
			appMgr = new AppManager(null, null);
		}
		return appMgr;
	}

	/**
	 * @param loc
	 *            Location of app download/install directory. If this method
	 *            is not called the default is .cytoscape/[cytoscape
	 *            version]/apps
	 */
	public static void setAppManageDirectory(String loc) {
		tempDir = new File(loc);
		if (!tempDir.getAbsolutePath().contains(cyVersion)) {
			tempDir = new File(tempDir, cyVersion);
		}
	}

	/**
	 * 
	 * @return The current version directory under .cytoscape that includes the
	 *         apps/ directory.
	 * 
	 * Ex. /<user dir>/.cytoscape/2.6/apps
	 */
	public File getAppManageDirectory() {
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
	protected static AppManager getAppManager(AppTracker Tracker, final CyApplicationConfiguration config) {
		if (appMgr == null) {
			appMgr = new AppManager(Tracker, config);
		}
		return appMgr;
	}

	/**
	 * This is used in testing to isolate each test case. DO NOT USE THIS IN
	 * CYTOSCAPE RUNTIME CODE
	 */
	protected void resetManager() {
		if (appTracker != null && appMgr != null) {
			appTracker.delete();
			appTracker = null;
			recursiveDeleteFiles(tempDir);
			appMgr = null;
		}
	}

	
	private AppManager() {
		
	}
	
	// create app manager
	private AppManager(AppTracker tracker, final CyApplicationConfiguration config) {
		
		cyVersion = new Integer(AppManagerAction.cyVersion.getMajorVersion()).toString();
		
		// XXX is this needed anymore?
		loadingErrors = new HashSet<Throwable>();

		setWebstart();
		String trackerFileName = "track_apps.xml";

		String cyConfigVerDir = AppManagerAction.cyConfigVerDir;
		
		if (tempDir == null) {
			if (usingWebstartManager()) {
				tempDir = new File(config.getConfigurationDirectoryLocation(),
						"webstart" + File.separator
								+ AppManagerAction.cyVersion.getMajorVersion()
								+ File.separator + "apps");
				removeWebstartInstalls();
				trackerFileName = "track_webstart_apps.xml";
			} else {
				tempDir = new File(cyConfigVerDir,"apps");
			}
		} else if (!tempDir.getAbsolutePath().endsWith("/apps")) {
			tempDir = new File(tempDir, "apps");
		}

		if (!tempDir.exists()) {
			logger.info("Creating directories for " + tempDir.getAbsolutePath());
			if (!tempDir.mkdirs()) {
				logger.warn("Failed to create directory --- "+ tempDir.getAbsolutePath());
			}
		}

		if (tracker != null) {
			appTracker = tracker;
		} else {
			try {
				appTracker = new AppTracker(tempDir.getParentFile(),
						trackerFileName);
			} catch (IOException ioe) {
				// ioe.printStackTrace();
				loadingErrors.add(ioe);
			} catch (TrackerException te) {
				// te.printStackTrace();
				loadingErrors.add(te);
			} finally { // document should be cleaned out by now
				try {
					appTracker = new AppTracker(tempDir.getParentFile(),
							trackerFileName);
				} catch (Exception e) {
					logger.warn("Unable to read app tracking file", e);
					// this could go on forever, surely there's a better way!
				}
			}
		}
		appURLs = new ArrayList<java.net.URL>();
		loadedApps = new HashSet<String>();
		initializedApps = new HashMap<String, AppInfo>();
		resourceApps = new ArrayList<String>();
	}

	/**
	 * Get a list of downloadable objects by status. CURRENT: currently
	 * installed INSTALL: objects to be installed DELETE: objects to be deleted
	 * 
	 * @param status
	 * @return
	 */
	public List<DownloadableInfo> getDownloadables(AppStatus status) {
		return appTracker.getDownloadableListByStatus(status);
	}

	/**
	 * Calls the given url, expects document describing apps available for
	 * download
	 * 
	 * @param url
	 * @return List of AppInfo objects
	 */
	public List<DownloadableInfo> inquire(String url) throws IOException,
			org.jdom.JDOMException {	
		
		List<DownloadableInfo> infoObjs = null;
		AppFileReader reader = new AppFileReader(url);
		infoObjs = reader.getDownloadables();
		return infoObjs;
	}

	/**
	 * Registers a currently installed app with tracking object. Only useful
	 * if the app was not installed via the install process.
	 * 
	 * @param app
	 * @param JarFileName
	 */
	protected void register(CytoscapeApp app, JarFile jar) {
		logger.info("Registering " + app.toString());

		DownloadableInfo infoObj = ManagerUtil.getInfoObject(app.getClass());
		if (infoObj != null && infoObj.getType().equals(DownloadableType.THEME)) {
			this.registerTheme(app, jar, (ThemeInfo) infoObj);
		} else {
			this.registerApp(app, jar, (AppInfo) infoObj, true);
		}
	}

	private AppInfo registerApp(CytoscapeApp app, JarFile jar,
			AppInfo appObj, boolean addToTracker) {
		// try to get it from the file
		// XXX PROBLEM: what to do about a app that attempts to register
		// itself and is not compatible with the current version?
		logger.info("     Registering " + app.getClass().getName());
		try {
			AppProperties pp = new AppProperties(app);
			appObj = pp.fillAppInfoObject(appObj);

		} catch (IOException ioe) {
			logger.warn("ERROR registering app: " + ioe.getMessage(), ioe);
			logger.warn(app.getClass().getName()
							+ " loaded but not registered, this will not affect the operation of the app");
		} catch (Exception e) {
			logger.warn("ERROR registering app: ", e);
		} finally {
			if (appObj == null) { // still null, create a default one
				appObj = new AppInfo();
				appObj.addCytoscapeVersion(cyVersion);
				appObj.setName(app.getClass().getName());
				appObj.setObjectVersion("0.1");
			}

			appObj.setAppClassName(app.getClass().getName());
			if (!usingWebstart && jar != null) {
				appObj.setInstallLocation(jar.getName());
				appObj.addFileName(jar.getName());
			}
			appObj.setFiletype(AppInfo.FileType.JAR);

			initializedApps.put(appObj.getAppClassName(), appObj);
			// TODO This causes a bug where theme apps essentially get added
			// to the current list twice
			logger.info("Track app: " + addToTracker);
			if (addToTracker) {
				appTracker.addDownloadable(appObj, AppStatus.CURRENT);
			}
		}
		return appObj;
	}

	private void registerTheme(CytoscapeApp inputApp, JarFile jar,
			ThemeInfo themeObj) {
		logger.info("--- Registering THEME " + themeObj.getName());
		for (AppInfo app : themeObj.getApps()) {
			if (app.getAppClassName().equals(inputApp.getClass().getName())) {
				logger.info(app.getName());
				AppInfo updatedApp = registerApp(inputApp, jar, app, false);
				themeObj.replaceApp(app, updatedApp);
			}
		}
		appTracker.addDownloadable(themeObj, AppStatus.CURRENT);
	}

	// TODO would be better to fix how initializedApps are tracked...
	private void cleanCurrentList() {
		List<DownloadableInfo> currentList = getDownloadables(AppStatus.CURRENT);
		for (DownloadableInfo info : currentList) {
			if (info.getType().equals(DownloadableType.APP)) {
				AppInfo pInfo = (AppInfo) info;
				if (!initializedApps.containsKey(pInfo.getAppClassName())) {
					appTracker.removeDownloadable(info, AppStatus.CURRENT);
				}
			}
		}
	}

	/**
	 * Sets all apps on the "install" list to "current"
	 */
	public void install() {
		for (DownloadableInfo info : getDownloadables(AppStatus.INSTALL)) {
			install(info);
		}
	}

	/**
	 * Change the given downloadable object from "install" to "current" status
	 * 
	 * @param obj
	 */
	public void install(DownloadableInfo obj) {
		appTracker.removeDownloadable(obj, AppStatus.INSTALL);
		appTracker.addDownloadable(obj, AppStatus.CURRENT);

		// mark all webstart-installed apps for deletion
		if (usingWebstartManager()) {
			appTracker.addDownloadable(obj, AppStatus.DELETE);
		}
	}

	/**
	 * Marks the given object for deletion the next time Cytoscape is restarted.
	 * 
	 * @param Obj
	 */
	public void delete(DownloadableInfo obj) throws WebstartException {
		checkWebstart();
		appTracker.addDownloadable(obj, AppStatus.DELETE);
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
		List<DownloadableInfo> toDelete = appTracker.getDownloadableListByStatus(AppStatus.DELETE);

		for (DownloadableInfo infoObj : toDelete) {
			Installable ins = infoObj.getInstallable();

			try {
				if (ins.uninstall()) {
					appTracker.removeDownloadable(infoObj, AppStatus.DELETE);
					appTracker.removeDownloadable(infoObj, AppStatus.CURRENT);
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
	 * Get list of apps that would update the given app.
	 * 
	 * @param info
	 * @return List<AppInfo>
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
	 *            AppInfo object currently installed
	 * @param New
	 *            AppInfo object to install
	 * @param taskMonitor
	 *            TaskMonitor for downloads
	 * @throws IOException
	 *             Fails to download the file.
	 * @throws ManagerException
	 *             If the apps don't match or the new one is not a newer
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

		appTracker.addDownloadable(currentObj, AppStatus.DELETE);
		appTracker.addDownloadable(newObj, AppStatus.INSTALL);
	}

	/**
	 * Downloads given object to the temporary directory.
	 * 
	 * @param obj
	 *            AppInfo object to be downloaded
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
	 *            AppInfo object to be downloaded
	 * @param taskMonitor
	 *            TaskMonitor
	 * @param tempDirectory
	 *            Download to a different temporary directory. Default is
	 *            .cytoscape/apps/[cytoscape version number]
	 * @return File downloaded
	 */
	public DownloadableInfo download(DownloadableInfo obj,
			TaskMonitor taskMonitor) throws IOException, ManagerException {
		// run a check for apps 
		List<DownloadableInfo> currentAndInstalled = new ArrayList<DownloadableInfo>();
		currentAndInstalled.addAll(this.getDownloadables(AppStatus.CURRENT));
		currentAndInstalled.addAll(this.getDownloadables(AppStatus.INSTALL));
		
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
				for (AppInfo themeApp: ((ThemeInfo) obj).getApps()) {
					if (themeApp.equalsDifferentObjectVersion(currentlyInstalled)) {
						throw new ManagerException(obj.toString() + " cannot be installed a app contained within the theme is already present: " 
								+ xCurrentlyInstalled.toString());
					}
				}
			}
		}
		Installable installable = obj.getInstallable();
		installable.install(taskMonitor);
		appTracker.addDownloadable(obj, AppStatus.INSTALL);
		return installable.getInfoObj();
	}

	private List<DownloadableInfo> flattenDownloadableList(List<DownloadableInfo> list) {
		List<DownloadableInfo> flattenedList = new ArrayList<DownloadableInfo>();
		for (DownloadableInfo info: list) {
			switch (info.getType()) {
			case THEME:
				flattenedList.addAll(((ThemeInfo) info).getApps());
			case APP:
				flattenedList.add(info);
			}
		}
		return flattenedList;
	}
	
	/*
	 * Methods for loading apps when Cytoscape starts up.
	 */
	public void loadApp(DownloadableInfo i) throws MalformedURLException,
			IOException, ClassNotFoundException, AppException {
		switch (i.getType()) {
		case APP:
			loadApp((AppInfo) i);
			break;
		case THEME:
			ThemeInfo Info = (ThemeInfo) i;
			for (AppInfo p : Info.getApps())
				loadApp(p);
			break;
		case FILE: // currently there is no FileInfo type
			break;
		}
	}

	/**
	 * Load a single app based on the AppInfo object given
	 * 
	 * @param AppInfo
	 *            The app to load
	 * @throws ManagerException
	 */
	public void loadApp(AppInfo p) throws ManagerException {
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
//					String name = jar.getManifest().getMainAttributes().getValue("Cytoscape-App");
//					URL jarurl = filename.toURI().toURL(); 
//					
//					URLClassLoader ucl = URLClassLoader.newInstance( new URL[]{jarurl}, 
//					                                      AppLoaderTask.class.getClassLoader() );
//					Class c = ucl.loadClass(name);
//					Constructor<CyApp> con = c.getConstructor(CyAppAdapter.class);
//					o = con.newInstance(adapter);
//				}
//				catch (Exception e){
//					e.printStackTrace();
//				}
				
				AppLoaderTask2 task = new AppLoaderTask2(adapter);
				task.setFile(new File(FileName));
				
				this.guiTaskManagerServiceRef.execute(new TaskIterator(task));
			}
		}
		// don't need to register if we have the info object
		InstallableApp insp = new InstallableApp(p);

		if (duplicateLoadError) {
			insp.uninstall();
			appTracker.removeDownloadable(p, AppStatus.CURRENT);
			addDuplicateError();
		}

	}

	private DialogTaskManager guiTaskManagerServiceRef;
	
	public void setTaskManager(DialogTaskManager guiTaskManagerServiceRef){
		this.guiTaskManagerServiceRef = guiTaskManagerServiceRef;
	}

	
	
	private void addDuplicateError() {
		String Msg = "The following apps were not loaded due to duplicate class definitions:\n";
		for (String dup : duplicateClasses)
			Msg += "\t" + dup + "\n";
		logger.warn(Msg);
		loadingErrors.add(new DuplicateAppClassException(Msg));
	}

	
	public void setCyAppAdapter(CyAppAdapter adapter){
		this.adapter = adapter;
	}

	public CyAppAdapter getCyAppAdapter(){
		return adapter;
	}
	
}
