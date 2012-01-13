/**
 * 
 */
package org.cytoscape.app.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class reads the app.props file that is expected to be in 
 * each app jar file and turns it into a AppInfo object for
 * the AppManager
 */
public class AppProperties extends Properties {
	private String configFileName = "app.props";
	private String packageName;
	private String errorMsg;
	private static final Logger logger = LoggerFactory.getLogger(CytoscapeApp.class);

	/**
	 * Properties in the app.props file
	 */
	public enum AppProperty {
		NAME("appName", true), DESCRIPTION("appDescription", true),
		VERSION("appVersion", true), CYTOSCAPE_VERSION("cytoscapeVersion", true),
		CATEGORY("appCategory", true),
		PROJECT_URL("projectURL", false), AUTHORS("appAuthorsInstitutions", false),
		RELEASE_DATE("releaseDate", false), UNIQUE_ID("uniqueID", false),
		DOWNLOAD_URL("downloadURL", false);
		
		private String propText;
		private boolean requiredProp;
	
		private AppProperty(String prop, boolean required) {
			propText = prop;
			requiredProp = required;
		}

		public String toString() {
			return propText + ":" + requiredProp;
		}
		
		public String getPropertyKey() {
			return propText;
		}
		
		public boolean isRequired() {
			return requiredProp;
		}
		
	}

	/**
	 * Used in testing only.
	 * @param fileName
	 */
	AppProperties(String fileName) throws IOException {
		FileInputStream fis = null;
        try {
			fis = new FileInputStream(new File(fileName));
            readAppProperties(fis);
        }
        finally {
            if (fis != null) {
                fis.close();
            }
        }
	}
	
	/**
	 * The app.props file is expected to be in the jar file under the package directory.  
	 * It will not be found if it is anywhere else.
	 * @param App
	 * @throws IOException
	 */
	public AppProperties(CytoscapeApp App) throws IOException {
		if (App.getClass().getPackage() == null) {
			throw new IOException(App.getClass().getName() + " is not part of a package, cannot read " + configFileName);
		}
		packageName = App.getClass().getPackage().getName();
		packageName = packageName.replace('.', '/'); // the package name has to be in the directory structure form with unix slashes
		readAppProperties(App.getClass().getClassLoader().getResourceAsStream(packageName + "/" +  configFileName));
	}
	
	private void readAppProperties(InputStream is) throws IOException {
		if (is == null || is.available() == 0) {
			// throw an error!
			String Msg = "";
			if (is == null) {
				Msg = "File is not in the expected location: " + packageName;
			} else if (is.available() == 0) {
				Msg = "0 bytes in input stream";
			}
	
			IOException Error = new IOException("Unable to load " + packageName 
					+ "/" + configFileName + ". " + Msg);
			throw Error;
		} else {
			load(is);
		}
	}
	
	/**
	 * Takes a AppInfo object (can be null) and fills it in with information that would not come from
	 * a properties file like the unique identifier and download url.
	 * @param info
	 * @return
	 * @throws ManagerException
	 */
	public AppInfo fillAppInfoObject(DownloadableInfo info) throws ManagerException {
		if (!expectedPropertiesPresent()) {
			throw new ManagerException("Required properties are missing from apps.props file: " + errorMsg);
		}
		
		
		AppInfo pi;
		String uniqueId = getProperty(AppProperty.UNIQUE_ID.getPropertyKey());
		if (uniqueId != null) {
			pi = new AppInfo(uniqueId);
			if (info != null) {
				pi.setObjectUrl(info.getObjectUrl());
				pi.setDownloadableURL(info.getDownloadableURL());
			}
		} else if (info != null) { // ????
				logger.info(info.toString());
				pi = (AppInfo) info;
				pi.clearAuthorList();
		} else {
			pi = new AppInfo();
		}
		
		// required parameters
		pi.setName(getProperty(AppProperty.NAME.getPropertyKey()));

		try {
			pi.setObjectVersion( getProperty(AppProperty.VERSION.getPropertyKey()));
		} catch (java.lang.NumberFormatException ne) { // skip it or set it to a default value??
			logger.warn(pi.getName() + " version is incorrectly formatted, format is: \\d+.\\d+. Version set to 0.1 to allow app to load");
			// ne.printStackTrace();
			pi.setObjectVersion("0.1");
		}
		
		pi.setDescription(getProperty(AppProperty.DESCRIPTION.getPropertyKey()));
		pi.setCategory(getProperty(AppProperty.CATEGORY.getPropertyKey()));
		
		
		// optional parameters
		String projectUrl = getProperty(AppProperty.PROJECT_URL.getPropertyKey());
		if (projectUrl != null) {
			pi.setProjectUrl(projectUrl);
		}

		String downloadUrl = getProperty(AppProperty.DOWNLOAD_URL.getPropertyKey());
		if (downloadUrl != null) {
			pi.setDownloadableURL(downloadUrl);
		}

		String AuthorProp = getProperty(AppProperty.AUTHORS.getPropertyKey());
		String AuthorProp2 = getProperty("appAuthorsIntsitutions");
		if (AuthorProp != null || AuthorProp2 != null) {
			// split up the value and add each

			// bug fix, misspelled the property file key but need to be sure anyone who used the
			// misspelling is taken care of for now
			if (AuthorProp == null) 
				AuthorProp = AuthorProp2;

			String[] AuthInst = AuthorProp.split(";");

			for (String ai: AuthInst) {
				String[] CurrentAI = ai.split(":");
				if (CurrentAI.length != 2) {
					logger.warn("Author line '" + ai + "' incorrectly formatted. Please enter authors as 'Name1, Name2 and Name3: Institution");
					continue;
				}
				pi.addAuthor(CurrentAI[0], CurrentAI[1]);
			}
		}

		String releaseDate = getProperty(AppProperty.RELEASE_DATE.getPropertyKey());
		if (releaseDate != null) {
			pi.setReleaseDate(releaseDate);
		}
		
		// on the off chance that someone did not install this via the PM this should be null if the version is not current
		String [] AllCytoscapeVersions = getProperty(AppProperty.CYTOSCAPE_VERSION.getPropertyKey()).split(","); 
		
		for (String v: AllCytoscapeVersions) {
			v = v.trim();
			pi.addCytoscapeVersion(v);
		}
		/*
		 * The only current usage of this method is in the AppManager.register() method.  By
		 * the time it gets to that point a app is already loaded (or has failed to load) and
		 * we can't unload it.  Instead we'll add notes and change the category to make it clear
		 * this may not be a good app.		
		 */
		
		return pi;
	}


	private boolean expectedPropertiesPresent() {
		for (AppProperty pp : AppProperty.values()) {
			String key = pp.getPropertyKey();
			if (pp.isRequired() && !containsKey(key)) {
				errorMsg = key;
				return false;
			}
		}
	return true;
	}
	
	
}
