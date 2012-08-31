package org.cytoscape.app.internal.ui.downloadsites;

/**
 * A class representing an app store, or a download site that can be used to obtain
 * apps and app information.
 */
public class DownloadSite {
	
	/** The name of the site, does not have to be an official name, mainly used for convenient displaying
	 */
	private String siteName;
	
	/** The site's url.
	 */
	private String siteUrl;
	
	public DownloadSite() {
		this.siteName = "";
		this.siteUrl = "";
	}
	
	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	@Override
	public String toString() {
		return this.siteName;
	}
	
	/**
	 * Returns true if the given site has same name and Url.
	 * @param downloadSite Other site
	 * @return <code>true</code> if other site has same name and Url, <code>false</code> otherwise.
	 */
	public boolean sameSiteAs(DownloadSite downloadSite) {
		if (downloadSite.siteName.equals(this.siteName)
				&& downloadSite.siteUrl.equals(this.siteUrl)) {
			return true;
		} else {
			return false;
		}
	}
}
