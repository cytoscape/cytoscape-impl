package org.cytoscape.app.internal.manager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.internal.exception.AppLoadingException;
import org.cytoscape.app.internal.exception.AppStartupException;
import org.cytoscape.app.internal.exception.AppStoppingException;
import org.cytoscape.app.internal.exception.AppUnloadingException;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.util.AppHelper;
import org.cytoscape.application.CyVersion;

/**
 * This class represents an app, and contains all needed information about the app such as its name, version, 
 * authors list, description, and file path (if present).
 */
public abstract class App {
		
	private String appName;
	private String version;
	private String authors;
	private String description;
	
	/**
	 * The file containing the app, may be a jar file.
	 */
	private File appFile;
	
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
	 * Whether this App object represents an app that has been checked to have valid packaging (such as containing
	 * necessary tags in its manifest file) and contains valid fields, making it loadable by the {@link AppManager} service.
	 */
	private boolean appValidated;
	
	/**
	 * Whether we've found the official name of the app as opposed to using an inferred name.
	 */
	private boolean officialNameObtained;
	
	/**
	 * Whether this App object represents an app bundled with Cytoscape.
	 */
	private boolean bundledApp;
	
	/**
	 * The SHA-512 checksum of the app file, in format sha512:0a516c..
	 */
	private String sha512Checksum;

	public static class Dependency {
		final String name;
		final String version;

		public Dependency(final String name, final String version) {
			this.name = name;
			this.version = version;
		}

		public String getName() {
			return name;
		}

		public String getVersion() {
			return version;
		}

		public String toString() {
			return name + " " + version;
		}
	}

	private List<Dependency> dependencies = null;
	
	private AppStatus status;
	
	/**
	 * An enumeration that indicates the status of a given app, such as whether it is installed or uninstalled.
	 */
	public enum AppStatus{
		INACTIVE("Inactive"),
		INSTALLED("Installed"),
		DISABLED("Disabled"),
		UNINSTALLED("Uninstalled"),
		TO_BE_INSTALLED("Install on Restart"),
		FILE_MOVED("File Moved (Uninstalled)"),
		FAILED_TO_LOAD("Failed to Load"),
		FAILED_TO_START("Failed to Start");
		
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
		this.description = null;
		this.appFile = null;
		
		appValidated = false;
		officialNameObtained = false;
		this.status = AppStatus.INACTIVE;
	}
	
	/**
	 * This is a useful method for knowing which apps not to display in an "all apps" GUI listing.
	 */
	public boolean isHidden() {
		if (bundledApp || status == AppStatus.INACTIVE || 
				status == AppStatus.UNINSTALLED || status == AppStatus.FILE_MOVED)
			return true;
		else return false;
	}
	
	/**
	 * Loosely, a detached app is no longer associated with the main program.
	 */
	public boolean isDetached() {
		if (status == AppStatus.UNINSTALLED || status == AppStatus.FILE_MOVED || 
				status == AppStatus.DISABLED || status == AppStatus.TO_BE_INSTALLED)
			return true;
		else return false;
	}
	

	public abstract void load(AppManager appManager) throws AppLoadingException;
	
	public abstract void start(AppManager appManager) throws AppStartupException;
	
	public abstract void stop(AppManager appManager) throws AppStoppingException;
	
	public abstract void unload(AppManager appManager) throws AppUnloadingException;
	
	public abstract boolean isCompatible(CyVersion cyVer);
	
	
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
		if (bundledApp == other.bundledApp
				&& appName.equalsIgnoreCase(other.appName)
				&& WebQuerier.compareVersions(version, other.version) == 0) {

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
	
	public String getEntryClassName() {
		return entryClassName;
	}
	
	public String getCompatibleVersions() {
		return compatibleVersions;
	}
	
	public String getSha512Checksum() {
		return sha512Checksum;
	}
	
	public URL getAppStoreUrl() {
		return appStoreUrl;
	}

	public boolean isAppValidated() {
		return appValidated;
	}
	
	public boolean isOfficialNameObtained() {
		return officialNameObtained;
	}
	
	public boolean isBundledApp() {
		return bundledApp;
	}

	public AppStatus getStatus() {
		return status;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
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

	public void setAppValidated(boolean appValidated) {
		this.appValidated = appValidated;
	}

	public void setOfficialNameObtained(boolean officialNameObtained) {
		this.officialNameObtained = officialNameObtained;
	}
	
	public void setBundledApp(boolean bundledApp) {
		this.bundledApp = bundledApp;
	}
	
	public void setStatus(AppStatus status) {
		this.status = status;
	}

	public void setDependencies(List<Dependency> deps) {
		this.dependencies = deps;
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
		LinkedList<String> uniqueNameDirectories = new LinkedList<>();
		
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
		LinkedList<File> moveDirectories = new LinkedList<>();
		moveDirectories.add(installDirectoryPath);
		moveDirectories.add(disabledDirectoryPath);
		moveDirectories.add(uninstallDirectoryPath);
		
		String targetFilePath = targetDirectory.getCanonicalPath() + File.separator 
				+ suggestFileName(uniqueNameDirectories, this.getAppFile().getName());
		File tempFile = new File(targetFilePath + ".tmp");
		File targetFile = new File(targetFilePath);
		
		if (!targetDirectory.equals(parentPath)) {
			if (moveDirectories.contains(parentPath)) {
				FileUtils.moveFile(this.getAppFile(), tempFile);
				//System.out.println("Moving: " + this.getAppFile() + " -> " + targetFile);
				
				// ** Disabled to let directory observers assign file reference
				// this.setAppFile(targetFile);
			} else {
				FileUtils.copyFile(this.getAppFile(), tempFile);
				//System.out.println("Copying: " + this.getAppFile() + " -> " + targetFile);
				
				// ** Disabled to let directory observers assign file reference
				// this.setAppFile(targetFile);
			}
			tempFile.renameTo(targetFile);
		}
	}
}
