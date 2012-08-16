package org.cytoscape.app.internal.manager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.internal.exception.AppDisableException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppInstanceException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.util.DebugHelper;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an app, and contains all needed information about the app such as its name, version, 
 * authors list, description, and file path (if present).
 */
public abstract class App {
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
	private String appName;
	private String version;
	private String authors;
	private String description;
	
	/**
	 * The file containing the app, may be a jar file.
	 */
	private File appFile;

	/**
	 * The temporary file corresponding to the app that is used to load classes from.
	 */
	private File appTemporaryInstallFile;
		
	/**
	 * The fully-qualified name of the app's class that extends {@link AbstractCyApp} to be instantiated when the app is loaded.
	 */
	private String entryClassName;

	/**
	 * The major versions of Cytoscape that the app is known to be compatible for, listed in comma-delimited form. Example: "2, 3"
	 */
	private String compatibleVersions;
	
	private URL appStoreUrl;
	
	/**
	 * A reference to the instance of the app's class that extends {@link AbstractCyApp}.
	 */
	private AbstractCyApp appInstance;
	
	/**
	 * Whether this App object represents an app that has been checked to have valid packaging (such as containing
	 * necessary tags in its manifest file) and contains valid fields, making it loadable by the {@link AppManager} service.
	 */
	private boolean appValidated;
	
	/**
	 * Whether we've found the official name of the app as opposed to using an inferred name.
	 */
	private boolean officialNameObtained;
	
	/**
	 * The SHA-512 checksum of the app file, in format sha512:0a516c..
	 */
	private String sha512Checksum;
	
	private AppStatus status;
	
	/**
	 * An enumeration that indicates the status of a given app, such as whether it is installed or uninstalled.
	 */
	public enum AppStatus{
		INSTALLED("Installed"),
		DISABLED("Disabled"),
		UNINSTALLED("Uninstalled"),
		TO_BE_INSTALLED("Install on Restart"),
		FILE_MOVED("File Moved (Uninstalled)");
		
		String readableStatus;
		
		private AppStatus(String readableStatus) {
			this.readableStatus = readableStatus;
		}
		
		@Override
		public String toString() {
			return readableStatus;
		}
	}
	
	public App() {	
		this.appName = "";
		this.version = "";
		this.authors = "";
		this.description = "";
		this.appFile = null;
		
		appValidated = false;
		officialNameObtained = false;
		this.status = null;
	}
	
	/**
	 * Creates an instance of this app, such as by instancing the app's class that extends AbstractCyApp,
	 * and returns an instance to it.
	 * @param appAdapter A reference to the {@link CyAppAdapter} service used to provide the newly
	 * created app instance with access to the Cytoscape API
	 * @return A reference to the instance of the app's class that extends AbstractCyApp.
	 * @throws AppInstanceException If there was an error while instancing the app, such as not being able to
	 * locate the class to be instanced.
	 */
	public abstract Object createAppInstance(CySwingAppAdapter appAdapter) throws AppInstanceException;
	
	/**
	 * Installs this app by creating an instance of its class that extends AbstractCyApp, copying itself
	 * over to the local Cytoscape app storage directory using the directory path obtained from the given 
	 * {@link AppManager} if needed, and registering it to the {@link AppManager}.
	 * @param appManager The AppManager used to register this app.
	 * @throws AppInstallException If there was an error while installing the app such as being unable to copy
	 * over the app file.
	 */
	public abstract void install(AppManager appManager) throws AppInstallException;
	
	/**
	 * Uninstalls this app by unloading its classes if possible, and copying itself over to
	 * the local Cytoscape app storage directory for uninstalled apps using the path obtained from the
	 * given {@link AppManager}.
	 * @param appManager The AppManager used to register this app.
	 * @throws AppUninstallException If there was an error while uninstalling the app, such as attemping
	 * to uninstall an app that isn't installed, or being unable to move the app file to the uninstalled
	 * apps directory.
	 */
	public abstract void uninstall(AppManager appManager) throws AppUninstallException;
	
	public abstract void disable(AppManager appManager) throws AppDisableException;
	
	/**
	 * Default app installation method that can be used by classes extending this class.
	 * 
	 * Attempts to install an app by copying it to the installed apps directory,
	 * creating an instance of the app's class that extends the {@link AbstractCyApp} class,
	 * and registering it with the given {@link AppManager} object. The app is instanced by
	 * calling its createAppInstance() method.
	 * 
	 * @param appManager The AppManager used to register this app with.
	 * @throws AppInstallException If there was an error while attempting to install the app such
	 * as improper app packaging, failure to copy the file to the installed apps directory, 
	 * or failure to create an instance of the app.
	 */
	protected void defaultInstall(AppManager appManager) throws AppInstallException {
		// Check if the app has been verified to contain proper packaging.
		if (!this.isAppValidated()) {
			
			// If the app is not packaged properly or is missing fields in its manifest file, do not install the app
			// as the install operation will fail.
			throw new AppInstallException("Cannot install app; app file has not been checked to have proper metadata");
		}
		
		// Check if the app has already been installed.
		if (this.getStatus() == AppStatus.INSTALLED) {
			
			// Do nothing if it is already installed
			throw new AppInstallException("This app has already been installed.");
		}
		
		for (App app : appManager.getApps()) {
			if (this.heuristicEquals(app) && this != app) {
				
				// If we already have an App object registered to the app manager
				// that represents this app, re-use that app object
				app.setAppFile(this.appFile);
				app.install(appManager);
				//appManager.installApp(app);
				
				return;
			}
		}
		
		// Obtain the paths to the local storage directories for holding installed and uninstalled apps.
		String installedAppsPath = appManager.getInstalledAppsPath();
		String uninstalledAppsPath = appManager.getUninstalledAppsPath();
		
		// Attempt to copy the app to the directory for installed apps.
		try {
			File appFile = this.getAppFile();
			
			if (!appFile.exists()) {
				DebugHelper.print("Install aborted: file " + appFile.getCanonicalPath() + " does not exist");
				return;
			}
			
			// Make sure no app with the same filename and app name is already installed
			File installedDirectoryTargetFile = new File(installedAppsPath + File.separator + appFile.getName());
			File uninstalledDirectoryTargetFile = new File(uninstalledAppsPath + File.separator + appFile.getName());
		
			String copyDestinationFileName = appFile.getName();
			
			// Check for filename collisions in both the installed apps directory as well as the 
			// uninstalled apps directory
			if (installedDirectoryTargetFile.exists() || uninstalledDirectoryTargetFile.exists()) {
				Set<App> registeredApps = appManager.getApps();
				
				// The app registered to the app manager that happens to have the same filename
				App conflictingApp = null;
				File registeredAppFile;
				
				for (App registeredApp : registeredApps) {
					registeredAppFile = registeredApp.getAppFile();
					
					if (registeredAppFile != null && registeredAppFile.getName().equalsIgnoreCase(appFile.getName())) {
						conflictingApp = registeredApp;
					}
				}
				
				// Only prevent the overwrite if the filename conflict is with an app registered
				// to the app manager
				if (conflictingApp != null) {
					
					// Check if the apps have the same name
					if (this.getAppName().equalsIgnoreCase(conflictingApp.getAppName())) {
						
						// Same filename, same app name found
						
						// Forgive the collision if we are copying from the uninstalled apps directory
						if (appFile.getParentFile().getCanonicalPath().equals(uninstalledAppsPath)) {
							
						// Forgive collision if other app is not installed
						} else if (conflictingApp.getStatus() != AppStatus.INSTALLED) {
						
						// Ignore collisions with self
						// } else if (conflictingApp.getAppFile().equals(appFile)) {
							
						} else {
							/*
							for (App app : appManager.getApps()) {
								if (this.heuristicEquals(app) && this != app) {
									DebugHelper.print("Install aborted: heuristic finds app already installed");
									DebugHelper.print("conflict app status: " + app.getStatus());
									DebugHelper.print("conflict app name: " + app.getAppName());
									
									appManager.installApp(app);
									
									return;
								}
							}
							*/
							
							// Skip installation, suspected that a copy of the app is already installed
							
							// return;
						}
						
					} else {
						
						// Same filename, different app name found
						// Rename file
						Collection<String> directoryPaths = new LinkedList<String>();
						directoryPaths.add(installedAppsPath);
						directoryPaths.add(uninstalledAppsPath);
						
						copyDestinationFileName = suggestFileName(directoryPaths, appFile.getName());
					}
					
				}
			}
			
			// Only perform the copy if the app was not already in the target directory
			if (!appFile.getParentFile().getCanonicalPath().equals(installedAppsPath)) {
				
				// Uses Apache Commons library; overwrites files with the same name.
				// FileUtils.copyFileToDirectory(appFile, new File(installedAppsPath));
				
				// If we copied it from the uninstalled apps directory, remove it from that directory
				File targetFile = new File(installedAppsPath + File.separator + copyDestinationFileName);
				if (appFile.getParentFile().getCanonicalPath().equals(uninstalledAppsPath)) {
					FileUtils.moveFile(appFile, targetFile);
				} else {
					FileUtils.copyFile(appFile, targetFile);				
				}
				
				// Update the app's path
				this.setAppFile(new File(installedAppsPath + File.separator + copyDestinationFileName));
			}
		} catch (IOException e) {
			throw new AppInstallException("Unable to copy app file to installed apps directory: " + e.getMessage());
		}
		
		// Make a second copy to be used to load the actual classes
		// This is used to prevent errors associated with moving jar files that have classes loaded from them.
		if (this.getAppTemporaryInstallFile() == null) {
			String temporaryInstallPath = appManager.getTemporaryInstallPath();
			List<String> temporaryInstallPathCollection = new LinkedList<String>();
			temporaryInstallPathCollection.add(temporaryInstallPath);
			
			// Rename the file if necessary to avoid overwrites
			File temporaryInstallTargetFile = new File(temporaryInstallPath + File.separator 
					+ suggestFileName(temporaryInstallPathCollection, appFile.getName()));
			try {
				FileUtils.copyFile(appFile, temporaryInstallTargetFile);
				
				this.setAppTemporaryInstallFile(temporaryInstallTargetFile);
			} catch (IOException e) {
				logger.warn("Failed to make copy of app file to be used for loading classes. The problem was: " + e.getMessage());
			}
		}
	
		// Create an app instance only if one was not already created
		if (this.getAppInstance() == null) {
			Object appInstance;
			try {
				appInstance = createAppInstance(appManager.getSwingAppAdapter());
			} catch (AppInstanceException e) {
				throw new AppInstallException("Unable to create app instance: " + e.getMessage());
			}
			
			// Keep a reference to the newly created instance
			this.setAppInstance((AbstractCyApp) appInstance);
		}
		
		this.setStatus(AppStatus.INSTALLED);
		appManager.addApp(this);
	}
	
	/**
	 * Given a set of canonical directory paths, find a name for a given file that does not 
	 * collide with names of files in any of the given directories.
	 * 
	 * For example, if the name file.txt is taken, this method will return file-2.txt. If the
	 * latter is taken, it will return file-3.txt, and so on.
	 * 
	 * @param directoryPaths A collection of canonical directory paths used to check for files
	 * that have colliding names
	 * @param desiredFileName The desired name for the given file, used as a base to which the
	 * number tag is added.
	 * @return A new name of the file that does not collide with any non-directory file in the given
	 * paths. If the given filename had no collisions, then an identical filename is returned.
	 */
	protected String suggestFileName(Collection<String> directoryPaths, String desiredFileName) {
		
		int postfixNumber = 1;
		boolean nameCollision = false;
		File file;
		
		for (String directoryPath : directoryPaths) {
			file = new File(directoryPath + File.separator + desiredFileName);
			
			nameCollision = nameCollision || (file.exists() && !file.isDirectory());
		}
		
		String fileBaseName = desiredFileName;
		String fileFullExtension = "";
		int lastPeriodIndex = desiredFileName.lastIndexOf(".");
		
		if (lastPeriodIndex != -1) {
			fileBaseName = desiredFileName.substring(0, lastPeriodIndex);
			fileFullExtension = desiredFileName.substring(lastPeriodIndex, desiredFileName.length());
		}
		
		String newFileName = desiredFileName;
		
		while(nameCollision) {
			postfixNumber++;
			nameCollision = false;
			
			for (String directoryPath : directoryPaths) {
				// If the old name is basename.extension, then the new name is basename-postfixNumber.extension
				newFileName = fileBaseName + "-" + postfixNumber + fileFullExtension;
				file = new File(directoryPath + File.separator + newFileName);
				
				nameCollision = nameCollision || (file.exists() && !file.isDirectory());
			}
		}
		
		return newFileName;
	}
	
	/**
	 * Default app uninstallation method that can be used by classes extending this class.
	 * 
	 * The default app uninstallation procedure consists of simply moving the app to the uninstalled apps
	 * directory.
	 * 
	 * @param appManager The app manager responsible for managing apps, which is used to obtain
	 * the path of the storage directories containing the installed and uninstalled apps
	 * @throws AppUninstallException If there was an error while uninstalling the app, such as
	 * attempting to uninstall an app that is not installed, or failure to move the app to
	 * the uninstalled apps directory
	 */
	protected void defaultUninstall(AppManager appManager) throws AppUninstallException {
		// Check if the app is installed before attempting to uninstall.
		if (this.getStatus() != AppStatus.INSTALLED) {
			// If it is not installed, do not attempt to uninstall it.
			throw new AppUninstallException("App is not installed; cannot uninstall.");
		}
		
		// For an installed app whose file has been moved or is no longer available, do not
		// perform file moving. Instead, attempt to try to complete the uninstallation without 
		// regard to app's file.
		if (this.getAppFile() != null) {
			
			if (!this.getAppFile().exists()) {
				
				// Skip file moving if the file has been moved
				return;
			}
			
			// Check if the app is inside the directory containing currently installed apps.
			// If so, prepare to move it to the uninstalled directory.
			File appParentDirectory = this.getAppFile().getParentFile();
			try {
				// Obtain the path of the "uninstalled apps" subdirectory.
				String uninstalledAppsPath = appManager.getUninstalledAppsPath();
				
				if (appParentDirectory.getCanonicalPath().equals(
						appManager.getInstalledAppsPath())) {
					
					// Use the Apache commons library to copy over the file, overwriting existing files.
					try {
						FileUtils.moveFileToDirectory(this.getAppFile(), new File(uninstalledAppsPath), true);
					} catch (IOException e) {
						throw new AppUninstallException("Unable to move file: " + e.getMessage());
					}
					
					// Delete the source file after the copy operation
					String fileName = this.getAppFile().getName();
					
					//System.gc();
					//System.out.println("Deleting " + this.getAppFile().getPath() + ": " + App.delete(this.getAppFile()));
					this.setAppFile(new File(uninstalledAppsPath + File.separator + fileName));
				}
			} catch (IOException e) {
				throw new AppUninstallException("Unable to obtain path: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Checks if this app is known to be compatible with a given version of Cytoscape.
	 * @param cytoscapeVersion The version of Cytoscape to be checked, in the form major.minor.patch[-tag].
	 * @return <code>true</code> if this app is known to be compatible with the given version, or
	 * <code>false</code> otherwise.
	 */
	public boolean isCompatible(String cytoscapeVersion) {
		// Get the major version of Cytoscape
		String majorVersion = cytoscapeVersion.substring(0, cytoscapeVersion.indexOf(".")).trim();
		
		if (compatibleVersions.matches("(.*,|^)\\s*(" + majorVersion + ")\\s*(,.*|$)")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Uses heuristics to check if another App represents the same Cytoscape app as this App, 
	 * ignoring filename differences. Specifically, it returns true only if the app names
	 * and app versions are equal.
	 * 
	 * @param other The app to compare against.
	 * @return <code>true</code> if the apps are suspected to be the same Cytoscape app,
	 * <code>false</code> otherwise.
	 */
	public boolean heuristicEquals(App other) {
		
		// Return false if different app names
		if (appName.equalsIgnoreCase(other.appName)
				&& version.equalsIgnoreCase(other.version)) {

			if (sha512Checksum != null && other.sha512Checksum != null) {
				return (sha512Checksum.equalsIgnoreCase(other.sha512Checksum));
			}
			
			return true;
		}
		
		return false;
	}
	
	/*
	/**
	 * Returns true only if the argument is an {@link App} with the same app name and version.
	 */
	/*
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		
		if (other instanceof App) {
			return (this.heuristicEquals((App) other));
		}
		
		return false;
	}
	*/
	
	public String getReadableStatus() {
		return this.getStatus().toString();
	}
	
	public String getAppName() {
		return appName;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getAuthors() {
		return authors;
	}
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * Return the file containing the app.
	 * @return The file containing the app, such as a jar, zip, or kar file.
	 */
	public File getAppFile() {
		return appFile;
	}
	
	/**
	 * Return the temporary file associated with the app that is used to load classes from.
	 * @return The temporary file corresponding to the app used to load classes from
	 */
	public File getAppTemporaryInstallFile() {
		return appTemporaryInstallFile;
	}
	
	public String getEntryClassName() {
		return entryClassName;
	}
	
	/**
	 * Return the major versions of Cytoscape the app is known to be compatible with, in comma-delimited form, eg. "2, 3"
	 * @return The major versions of Cytoscape that the app is known to be compatible with
	 */
	public String getCompatibleVersions() {
		return compatibleVersions;
	}
	
	public String getSha512Checksum() {
		return sha512Checksum;
	}
	
	public URL getAppStoreUrl() {
		return appStoreUrl;
	}
	
	public AbstractCyApp getAppInstance() {
		return appInstance;
	}

	public boolean isAppValidated() {
		return appValidated;
	}
	
	public boolean isOfficialNameObtained() {
		return officialNameObtained;
	}
	
	public AppStatus getStatus() {
		return status;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setAppFile(File appFile) {
		this.appFile = appFile;
	}
	
	public void setAppTemporaryInstallFile(File appTemporaryInstallFile) {
		this.appTemporaryInstallFile = appTemporaryInstallFile;
	}
	
	public void setEntryClassName(String entryClassName) {
		this.entryClassName = entryClassName;
	}
	
	public void setCompatibleVersions(String compatibleVersions) {
		this.compatibleVersions = compatibleVersions;
	}
	
	public void setSha512Checksum(String sha512Checksum) {
		this.sha512Checksum = sha512Checksum;
	}
	
	public void setAppStoreUrl(URL appStoreURL) {
		this.appStoreUrl = appStoreURL;
	}
	
	public void setAppInstance(AbstractCyApp appInstance) {
		this.appInstance = appInstance;
	}

	public void setAppValidated(boolean appValidated) {
		this.appValidated = appValidated;
	}

	public void setOfficialNameObtained(boolean officialNameObtained) {
		this.officialNameObtained = officialNameObtained;
	}
	
	public void setStatus(AppStatus status) {
		this.status = status;
	}
	
	public static boolean delete( File f )  
    {  
        if( ! f.exists() )  
        {  
            System.err.println( "Cannot delete, file does not exist: " + f.getPath() );  
            return false;  
        }  
        f.setReadable( true );  
        f.setWritable( true );  
        if( ! f.canWrite() )  
        {  
            System.err.println( "Cannot delete, file is read-only: " + f.getPath() );  
            return false;  
        }  
  
        // Hack attempt  
        File    parent = f.getParentFile();  
        parent.setReadable( true );  
        parent.setWritable( true );  
        if( ! parent.canWrite() )  
        {  
            System.err.println( "Cannot delete, parent folder read-only: " + parent.getPath() );  
            return false;  
        }  
  
        try  
        {  
            (new SecurityManager()).checkDelete( f.getPath() );  
        }  
        catch( Exception ex )  
        {  
            System.err.println( "Cannot delete file, " + ex.getMessage() );  
            return false;  
        }  
  
        boolean ret = f.delete();  
        if( ! ret )  
            System.err.println( "Delete failed: " + f.getPath() );  
        return ret;  
    }

	/**
	 * Moves an app file to the given directory, copying the app if it is outside one of the local app storage directories
	 * and moving if it is not. Also assigns filename that does not colliide with any from the local app storage directories.
	 * 
	 * Will also add postfix to filename if desired filename already exists in target directory when
	 * moving app to a directory other than the 3 local app storage directories.
	 * 
	 * @param appManager A reference to the app manager
	 * @param targetDirectory The local storage directory to move to, such as the local sotrage directory
	 * containing installed apps obtained via the app manager
	 * @throws IOException If there was an error while moving/copying the file
	 */
	public void moveAppFile(AppManager appManager, File targetDirectory) throws IOException {
		File parentPath = this.getAppFile().getParentFile();
		File installDirectoryPath = new File(appManager.getInstalledAppsPath());
		File disabledDirectoryPath = new File(appManager.getDisabledAppsPath());
		File uninstallDirectoryPath = new File(appManager.getUninstalledAppsPath());

		// Want to make sure the app file's name does not collide with another name in these directories
		LinkedList<String> uniqueNameDirectories = new LinkedList<String>();
		
		if (!parentPath.equals(installDirectoryPath))
			uniqueNameDirectories.add(installDirectoryPath.getCanonicalPath());
		
		if (!parentPath.equals(disabledDirectoryPath))	
			uniqueNameDirectories.add(disabledDirectoryPath.getCanonicalPath());
	
		if (!parentPath.equals(uninstallDirectoryPath))	
			uniqueNameDirectories.add(uninstallDirectoryPath.getCanonicalPath());
		
		if (!parentPath.equals(targetDirectory)
				&& !installDirectoryPath.equals(targetDirectory)
				&& !disabledDirectoryPath.equals(targetDirectory)
				&& !uninstallDirectoryPath.equals(targetDirectory))
			uniqueNameDirectories.add(targetDirectory.getCanonicalPath());
		
		// If the app file is in one of these directories, do a move instead of a copy
		LinkedList<File> moveDirectories = new LinkedList<File>();
		moveDirectories.add(installDirectoryPath);
		moveDirectories.add(disabledDirectoryPath);
		moveDirectories.add(uninstallDirectoryPath);

		File targetFile = new File(targetDirectory.getCanonicalPath() + File.separator 
				+ suggestFileName(uniqueNameDirectories, this.getAppFile().getName()));
		
		if (!targetDirectory.equals(parentPath)) {
			if (moveDirectories.contains(parentPath)) {
				FileUtils.moveFile(this.getAppFile(), targetFile);
				//System.out.println("Moving: " + this.getAppFile() + " -> " + targetFile);
				
				// ** Disabled to let directory observers assign file reference
				// this.setAppFile(targetFile);
			} else {
				FileUtils.copyFile(this.getAppFile(), targetFile);
				//System.out.println("Copying: " + this.getAppFile() + " -> " + targetFile);
				
				// ** Disabled to let directory observers assign file reference
				// this.setAppFile(targetFile);
			}
		}
	}
	
	protected boolean checkAppAlreadyInstalled(AppManager appManager) {
		boolean appAlreadyInstalled = false;
		for (App app : appManager.getApps()) {
			if (app.heuristicEquals(this)) {
				appAlreadyInstalled = true;
			}
		}
		
		return appAlreadyInstalled;
	}
}
