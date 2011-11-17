/**
 * 
 */
package org.cytoscape.plugin.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class reads the plugin.props file that is expected to be in 
 * each plugin jar file and turns it into a PluginInfo object for
 * the PluginManager
 */
public class PluginProperties extends Properties {
	private String configFileName = "plugin.props";
	private String packageName;
	private String errorMsg;
	private static final Logger logger = LoggerFactory.getLogger(CytoscapePlugin.class);

	/**
	 * Properties in the plugin.props file
	 */
	public enum PluginProperty {
		NAME("pluginName", true), DESCRIPTION("pluginDescription", true),
		VERSION("pluginVersion", true), CYTOSCAPE_VERSION("cytoscapeVersion", true),
		CATEGORY("pluginCategory", true),
		PROJECT_URL("projectURL", false), AUTHORS("pluginAuthorsInstitutions", false),
		RELEASE_DATE("releaseDate", false), UNIQUE_ID("uniqueID", false),
		DOWNLOAD_URL("downloadURL", false);
		
		private String propText;
		private boolean requiredProp;
	
		private PluginProperty(String prop, boolean required) {
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
	PluginProperties(String fileName) throws IOException {
		FileInputStream fis = null;
        try {
			fis = new FileInputStream(new File(fileName));
            readPluginProperties(fis);
        }
        finally {
            if (fis != null) {
                fis.close();
            }
        }
	}
	
	/**
	 * The plugin.props file is expected to be in the jar file under the package directory.  
	 * It will not be found if it is anywhere else.
	 * @param Plugin
	 * @throws IOException
	 */
	public PluginProperties(CytoscapePlugin Plugin) throws IOException {
		if (Plugin.getClass().getPackage() == null) {
			throw new IOException(Plugin.getClass().getName() + " is not part of a package, cannot read " + configFileName);
		}
		packageName = Plugin.getClass().getPackage().getName();
		packageName = packageName.replace('.', '/'); // the package name has to be in the directory structure form with unix slashes
		readPluginProperties(Plugin.getClass().getClassLoader().getResourceAsStream(packageName + "/" +  configFileName));
	}
	
	private void readPluginProperties(InputStream is) throws IOException {
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
	 * Takes a PluginInfo object (can be null) and fills it in with information that would not come from
	 * a properties file like the unique identifier and download url.
	 * @param info
	 * @return
	 * @throws ManagerException
	 */
	public PluginInfo fillPluginInfoObject(DownloadableInfo info) throws ManagerException {
		if (!expectedPropertiesPresent()) {
			throw new ManagerException("Required properties are missing from plugins.props file: " + errorMsg);
		}
		
		
		PluginInfo pi;
		if (containsKey(PluginProperty.UNIQUE_ID.getPropertyKey())) {
			pi = new PluginInfo(getProperty(PluginProperty.UNIQUE_ID.getPropertyKey()));
			if (info != null) {
				pi.setObjectUrl(info.getObjectUrl());
				pi.setDownloadableURL(info.getDownloadableURL());
			}
		} else if (info != null) { // ????
				logger.info(info.toString());
				pi = (PluginInfo) info;
				pi.clearAuthorList();
		} else {
			pi = new PluginInfo();
		}
		
		// required parameters
		pi.setName(getProperty(PluginProperty.NAME.getPropertyKey()));

		try {
			pi.setObjectVersion( getProperty(PluginProperty.VERSION.getPropertyKey()));
		} catch (java.lang.NumberFormatException ne) { // skip it or set it to a default value??
			logger.warn(pi.getName() + " version is incorrectly formatted, format is: \\d+.\\d+. Version set to 0.1 to allow plugin to load");
			// ne.printStackTrace();
			pi.setObjectVersion("0.1");
		}
		
		pi.setDescription(getProperty(PluginProperty.DESCRIPTION.getPropertyKey()));
		pi.setCategory(getProperty(PluginProperty.CATEGORY.getPropertyKey()));
		
		
		// optional parameters
		if (containsKey(PluginProperty.PROJECT_URL.getPropertyKey())) {
			pi.setProjectUrl(getProperty(PluginProperty.PROJECT_URL.getPropertyKey()));
		}

		if (containsKey(PluginProperty.DOWNLOAD_URL.getPropertyKey())) {
			pi.setDownloadableURL(getProperty(PluginProperty.DOWNLOAD_URL.getPropertyKey()));
		}

		if (containsKey(PluginProperty.AUTHORS.getPropertyKey()) || containsKey("pluginAuthorsIntsitutions")) {
			// split up the value and add each
			String AuthorProp = getProperty(PluginProperty.AUTHORS.getPropertyKey());

			// bug fix, misspelled the property file key but need to be sure anyone who used the
			// misspelling is taken care of for now
			if (AuthorProp == null) 
				AuthorProp = getProperty("pluginAuthorsIntsitutions");

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

		if (containsKey(PluginProperty.RELEASE_DATE.getPropertyKey())) {
			pi.setReleaseDate(getProperty(PluginProperty.RELEASE_DATE.getPropertyKey()));
		}
		
		// on the off chance that someone did not install this via the PM this should be null if the version is not current
		String [] AllCytoscapeVersions = getProperty(PluginProperty.CYTOSCAPE_VERSION.getPropertyKey()).split(","); 
		
		for (String v: AllCytoscapeVersions) {
			v = v.trim();
			pi.addCytoscapeVersion(v);
		}
		/*
		 * The only current usage of this method is in the PluginManager.register() method.  By
		 * the time it gets to that point a plugin is already loaded (or has failed to load) and
		 * we can't unload it.  Instead we'll add notes and change the category to make it clear
		 * this may not be a good plugin.		
		 */
		
		return pi;
	}


	private boolean expectedPropertiesPresent() {
		for (PluginProperty pp : PluginProperty.values()) {
			if (pp.isRequired() && !containsKey(pp.getPropertyKey())) {
				errorMsg = pp.getPropertyKey();
				return false;
			}
		}
	return true;
	}
	
	
}
