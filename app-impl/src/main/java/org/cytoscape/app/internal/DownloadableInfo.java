/*
 File: DownloadableInfo.java 
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

import java.net.URL;
import java.io.IOException;

import org.cytoscape.app.internal.action.PluginManagerAction;
import org.cytoscape.app.internal.util.URLUtil;

import static org.cytoscape.app.internal.PluginVersionUtils.getNewerVersion;
import static org.cytoscape.app.internal.PluginVersionUtils.versionOk;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DownloadableInfo {
	private static final Logger logger = LoggerFactory.getLogger(DownloadableInfo.class);


	protected String versionMatch = PluginVersionUtils.versionMatch;

	protected String versionSplit = PluginVersionUtils.versionSplit;

	private String releaseDate;

	private String uniqueID;

	private String name = "";

	private String description;

	private String objVersion;

	private String downloadURL = "";

	private String objURL = "";

	private String category;

	private License license;

	private boolean licenseRequired = false;

	private Set<String> compatibleCyVersions;

	private DownloadableInfo parentObj; 

	public DownloadableInfo() {
		this(null,null);
	}

	public DownloadableInfo(String id) {
		this(id, null);
	}

	public DownloadableInfo(String id, DownloadableInfo parentObj) {
		this.uniqueID = id;
		this.parentObj = parentObj;
		this.compatibleCyVersions = new HashSet<String>();
	}

	/**
	 * Sets the license information for the plugin. Not required.
	 * 
	 * @param url
	 *            object where license can be downloaded from.
	 */
	public void setLicense(URL url) {
		license = new License(url);
	}

	/**
	 * Sets the license information for the plugin. Not required.
	 * 
	 * @param licenseText
	 *            string of license.
	 * @param alwaysRequired
	 *            If the user expects the license to be required for both
	 *            install and update at all times (true) or only at install
	 *            (false)
	 */
	public void setLicense(String licenseText, boolean alwaysRequired) {
		license = new License(licenseText);
		licenseRequired = alwaysRequired;
	}

	/**
	 * @param Category
	 *            Sets the category of the downloadable object.
	 */
	public void setCategory(String Category) {
		this.category = Category;
	}

	public void setCategory(Category cat) {
		this.category = cat.toString();
	}

	public void setParent(DownloadableInfo parentObj) {
		this.parentObj = parentObj;
	}

	/**
	 * @param name
	 *            Sets the name of the downloadable object.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description
	 *            Sets the descriptoin of the downloadable object.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param url
	 *            Sets the URL for the xml file describing all downloadable
	 *            objects from any given project. (ex.
	 *            http://cytoscape.org/plugins/plugin.xml)
	 */
	public void setDownloadableURL(String url) {
		this.downloadURL = url;
	}

	/**
	 * @param url
	 *            Set the URL where this object can be downloaded from.
	 */
	public void setObjectUrl(String url) {
		this.objURL = url;
	}

	/**
	 * Contains a list of all the Cytoscape versions this object is compatible
	 * with.
	 * 
	 * @param cyVersion
	 * @throws NumberFormatException
	 */
	public void addCytoscapeVersion(String cyVersion)
			throws NumberFormatException {
		if (versionOk(cyVersion, false)) {
			compatibleCyVersions.add(cyVersion);
            

    } else {
			throw new NumberFormatException(
					"Cytoscape version numbers must be in the format: \\d+.\\d+  optional to add: .\\d+-[a-z]");
		}
	}

	/**
	 * @param objVersion
	 *            Sets the version of this object.
	 */
	//public void setObjectVersion(double objVersion)
	public void setObjectVersion(String objVersion)
			throws NumberFormatException {
		//String Version = Double.toString(objVersion);
		String Version = objVersion.trim();
		if (versionOk(Version, true)) {
			this.objVersion = Version;
		} else {
			throw new NumberFormatException("Bad version '" + Version + "'."
					+ this
					+ " version numbers must be in the format: \\d+.\\d+");
		}
	}

	/**
	 * TODO - would probably be better to use a date object
	 * 
	 * @param date
	 *            Sets the release date of this object.
	 */
	public void setReleaseDate(String date) {
		this.releaseDate = date;
	}

	/* --- GET --- */

	/**
	 * @return The text of the license for this plugin if available.
	 */
	public String getLicenseText() {
		if (license != null)
			return license.getLicense();
		else
			return null;
	}

	/**
	 * @return If the license is always required to be accepted for installs and
	 *         updates this returns true. If it only is required at install time
	 *         (never at update) returns false.
	 */
	public boolean isLicenseRequired() {
		return licenseRequired;
	}

	public abstract Installable getInstallable();

	/**
	 * Return the downloadable type of this object.
	 */
	public abstract DownloadableType getType();

	/**
	 * @return The parent DownloadableInfo object for this object if it has one.
	 */
	public DownloadableInfo getParent() {
		return this.parentObj;
	}

	/**
	 * @return The URL this object can be downloaded from.
	 */
	public String getObjectUrl() {
		return this.objURL;
	}

	/**
	 * @return Category that describes this downloadable object.
	 */
	public String getCategory() {
		return this.category;
	}

	/**
	 * @return Url that returns the document of available downloadable objects
	 *         this object came from. Example
	 *         http://cytoscape.org/plugins/plugins.xml
	 */
	public String getDownloadableURL() {
		return this.downloadURL;
	}

	/**
	 * @return Version of the downloadable object.
	 */
	public String getObjectVersion() {
		return this.objVersion;
	}

	/**
	 * @return Name of the downloadable object.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return Description of the downloadable object.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return Compatible Cytocape version of this object.
	 *
	 * BUG: THIS IS WRONG, I need to be getting the version that MATCHES the current version
	 * not the one that is newest!!
	 *
	 */
	public String getCytoscapeVersion() {

		//Bug fix, if currentVersion matches one of compatible versions of Cytosape, just return current version
		for (String v : this.compatibleCyVersions) {
			if (isCytoscapeVersionCurrent(v)){
				return v;
			}
		}		

		String currentPluginVersion = null;
		String all = "";
		for (String v : this.compatibleCyVersions) {
			all += v + " ";      

			if (currentPluginVersion != null) {
				currentPluginVersion = getNewerVersion(v, currentPluginVersion);
				// compare to cytoscape version
				if ( isCytoscapeVersionCurrent(currentPluginVersion) )
					return currentPluginVersion; 
			}
			else {
				currentPluginVersion = v;
			}
		}

		//logger.debug(getName() +": Compatible: " + all + " cyvers: " + currentPluginVersion
		//		+ "(cyversion " + CytoscapeVersion.version +")");
		return currentPluginVersion;
	}

  /**
	 * @return All compatible Cytoscape versions.
	 */
	public List<String> getCytoscapeVersions() {
		return new ArrayList<String>(this.compatibleCyVersions);
	}

	protected boolean containsVersion(String cyVersion) {
		return compatibleCyVersions.contains(cyVersion);
	}

	/**
	 * @return Release date for this object.
	 */
	public String getReleaseDate() {
		return this.releaseDate;
	}

	/**
	 * @return Unique identifier for the downloadable object.
	 */
	public String getID() {
		return this.uniqueID;
	}

	/**
	 * Compare the version of the object to the given object.
	 * 
	 * @param New
	 *            Potentially newer DownloadableInfo object
	 * @return true if given version is newer
	 */
	public boolean isNewerObjectVersion(DownloadableInfo New) {
		String[] CurrentVersion = this.getObjectVersion().split(versionSplit);
		String[] NewVersion = New.getObjectVersion().split(versionSplit);

		// make sure it's the same object first
		if (!(this.getID().equals(New.getID()) && this.getDownloadableURL()
				.equals(New.getDownloadableURL()))) {
			return false;
		}

		int CurrentMajor = Integer.valueOf(CurrentVersion[0]).intValue();
		int NewMajor = Integer.valueOf(NewVersion[0]).intValue();

		int CurrentMinor = Integer.valueOf(CurrentVersion[1]).intValue();
		int NewMinor = Integer.valueOf(NewVersion[1]).intValue();

		if ((CurrentMajor > NewMajor || (CurrentMajor == NewMajor && CurrentMinor >= NewMinor))) {
			return false;
		}

		return true;
	}

  /**
   * @param pluginVersion
	 * @return true if the given version is compatible with the current Cytoscape
	 *         version major.minor (bugfix is only checked if the plugin
	 *         specifies a bugfix version)
	 */
  private boolean isCytoscapeVersionCurrent(String pluginVersion) {
	  
  	if (pluginVersion == null )
		return false;

    String[] plVersion = pluginVersion.split(versionSplit);

	if ( plVersion.length <3)
		return false;
	
	int curCyVersionInt = Integer.valueOf(plVersion[0]).intValue()*100+
						Integer.valueOf(plVersion[1]).intValue()*10+
						Integer.valueOf(plVersion[2]).intValue();

	int minCyVersionInt  = PluginManagerAction.cyVersion.getMajorVersion()*100 + 
		PluginManagerAction.cyVersion.getMinorVersion()*10+
		PluginManagerAction.cyVersion.getBugFixVersion();

	if (curCyVersionInt < minCyVersionInt){
		return false;
	}

    return true;
  }


  private boolean compareVersions(String[] v1, String[] v2) {
    if (v1.length != v2.length) return false;
  
    for (int i = 0; i < v1.length; i++) {
      if (Integer.valueOf(v1[i]).intValue() != Integer.valueOf(
          v2[i]).intValue())
        return false;
    }
  return true;
  }

/**
 * @return true if the plugin is compatible with the current version of Cytoscape.
 *    NOTE: It is assumed that if a plugin is listed as being compatible with the minor version number
 *      it is compatible with all bug fix versions.
 */
  public boolean isPluginCompatibleWithCurrent() {

	  if (this.getCategory() != null && this.getCategory().equalsIgnoreCase("Core")){
		  // core plugins already compatible with current version
		  return true;
	  }
	  
	boolean compatible = false;
    for (String pluginVersion: compatibleCyVersions) {
      String[] cyVersion = PluginManagerAction.cyVersion.getVersion().split(versionSplit);
      String[] plVersion = pluginVersion.split(versionSplit);
      if ( PluginVersionUtils.isVersion(pluginVersion, PluginVersionUtils.MINOR) ) {
          cyVersion = new String[]{cyVersion[0], cyVersion[1]};
        }
     // logger.debug("Comparing versions: " + Arrays.toString(cyVersion) + " : " + Arrays.toString(plVersion));

      // TODO: we do not compae version for now
      //if (compareVersions(cyVersion, plVersion)) {
        compatible = true;
        break;
      //}
    }
    return compatible;
  }

	/**
	 * Compare the two info objects. If the ID, downloadable url and object
	 * version are the same they are considered to be the same object.
   *
   * Careful, this overwrites the Object.equals method
	 */
	public boolean equals(Object Obj) {
		DownloadableInfo obj = (DownloadableInfo) Obj;

		if ( this.getType().equals(obj.getType()) ) {
			if ( (this.getID() != null && obj.getID() != null) ) {
				if (this.getID().equals(obj.getID()) &&
					this.getDownloadableURL().equals(obj.getDownloadableURL()) &&
					this.getObjectVersion().equals(obj.getObjectVersion()))
					return true;
			} else if (this.getDownloadableURL().equals(obj.getDownloadableURL()) &&
					   this.getObjectVersion().equals(obj.getObjectVersion())) {
				// should I do this?? Without an id there is no other good way to
				// tell I suppose
				return true;
			}
		}
		return false;
	}

	/**
	 * Compares the ID and download URL of the two objects. If they are the same
	 * the objects are considered to be equal regardless of version.
	 * 
	 * @param Obj
	 * @return
	 */
	public boolean equalsDifferentObjectVersion(Object Obj) {
		DownloadableInfo obj = (DownloadableInfo) Obj;
		if (this.getID().equals(obj.getID())
				&& this.getType().equals(obj.getType())
				&& this.getDownloadableURL().equals(obj.getDownloadableURL()))
			return true;

		return false;
	}

  /**
	 * @return Returns String of downloadable name and version ex. MyPlugin
	 *         v.1.0
	 */
	public String toString() {
		return getName() + " v." + getObjectVersion();
	}

	public abstract String htmlOutput();

	// yea, it's ugly...styles taken from cytoscape website
	protected String basicHtmlOutput() {
		String Html = "<html><style type='text/css'>";
		Html += "body,th,td,div,p,h1,h2,li,dt,dd ";
		Html += "{ font-family: Tahoma, \"Gill Sans\", Arial, sans-serif; }";
		Html += "body { margin: 0px; color: #333333; background-color: #ffffff; }";
		Html += "#indent { padding-left: 30px; }";
		Html += "ul {list-style-type: none}";
		Html += "</style><body>";

		Html += "<b>" + getName() + "</b><p>";
		Html += "<b>Version:</b>&nbsp;" + getObjectVersion() + "<p>";
		Html += "<b>Category:</b>&nbsp;" + getCategory() + "<p>";
		Html += "<b>Description:</b><br>" + getDescription();

		if (!isPluginCompatibleWithCurrent()) {
			Html += "<br><b>Verified with the following Cytoscape versions:</b> "
					+ getCytoscapeVersions().toString() + "<br>";
			Html += "<font color='red'><i>" + toString()
					+ " is not verfied to work in the current version ("
					+ PluginManagerAction.cyVersion.getVersion()
					+ ") of Cytoscape.</i></font>";
		}
		Html += "<p>";

		if (getReleaseDate() != null && getReleaseDate().length() > 0) {
			Html += "<b>Release Date:</b>&nbsp;" + getReleaseDate() + "<p>";
		}

		return Html;
	}


	/**
	 * Fetches and keeps a plugin license if one is available.
	 */
	protected class License {
		private URL url;
		private String text;

		public License(URL url) {
			this.url = url;
		}

		public License(String licenseText) {
			text = licenseText;
		}

		/**
		 * Get the license text as a string. Will download from url if License
		 * was not initialized with text string.
		 * 
		 * @return String
		 */
		public String getLicense() {
			if (text == null) {
				try {
					text = URLUtil.download(url);
				} catch (Exception e) {
					DownloadableInfo.logger.warn("Unable to get license: "+e.toString());
					text = "No license found"; 
				}
			}
			return text;
		}
	}
}
