package org.cytoscape.app.internal.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.cytoscape.app.internal.exception.AppDownloadException;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp.Release;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSite;
import org.cytoscape.app.internal.util.DebugHelper;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.CyVersion;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2019 The Cytoscape Consortium
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

/**
 * This class is responsible for querying the Cytoscape App Store web service to obtain
 * information about available apps and app updates.
 */
public class WebQuerier {
	
	public static final List<DownloadSite> DEFAULT_DOWNLOAD_SITES = new LinkedList<>();
	
	public static final String DEFAULT_APP_STORE_URL = "https://apps.cytoscape.org/";
	
	private static final String REQUEST_JSON_HEADER_KEY = "X-Requested-With";
	private static final String REQUEST_JSON_HEADER_VALUE = "XMLHttpRequest";
	
	static {
		DownloadSite site = new DownloadSite("Cytoscape App Store", DEFAULT_APP_STORE_URL);
		DEFAULT_DOWNLOAD_SITES.add(site);
	}
	
	/**
	 * A regular expression for version lists that are compatible with the current version of Cytoscape.
	 */
	private static final String COMPATIBLE_RELEASE_REGEX = "(^\\s*|.*,)\\s*3(\\..*)?\\s*(\\s*$|,.*)";
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	/** A reference to the result obtained by the last successful query for all available apps. */
	// private Set<WebApp> apps;
	
	/** A reference to a map which keeps track of the known set of apps for each known tag */
	// private Map<String, Set<WebApp>> appsByTagName;
	
	/** A reference to the result obtained by the last successful query for all available app tags. */
	// private Map<String, AppTag> appTags;
	
	/**
	 * A reference to the result obtained by the last successful query for all available apps
	 * to this app store URL.
	 */
	private Map<String, Set<WebApp>> appsByUrl;
	
	/** 
	 * A reference to a map of maps keeping track, for each app store URL, the known set of 
	 * apps for each known tag from that URL.
	 */
	private Map<String, Map<String, Set<WebApp>>> appsByTagNameByUrl;
	
	/**
	 * A reference to the set of all known tags for a given app store URL. The tags are stored
	 * in a map that maps the tag's string name to a tag object containing more information
	 * about the tag, such as the number of apps associated with it.
	 */
	private Map<String, Map<String, AppTag>> appTagsByUrl;
	
	private boolean showMultipleWarnings = false;
	
	
	private String currentAppStoreUrl = DEFAULT_APP_STORE_URL;
	private String currentSiteName = null;

	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)([.](\\d+)([.](\\d+)([.]([-_a-zA-Z0-9]+))?)?)?");
	
	public static final Pattern OUTPUT_FILENAME_DISALLOWED_CHARACTERS = Pattern.compile("[^a-zA-Z0-9.-]");

	private AppManager appManager;
	private final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * A class that represents a tag used for apps, containing information about the tag
	 * such as its unique name used on the app store website as well as its human-readable name.
	 */
	public class AppTag {
		
		/** A unique name of the tag used by the app store website as a tag identifier */
		private String name;
		
		/** The name of the tag that is shown to the user */
		private String fullName;
		
		/** The number of apps associated with this tag */
		private int count;
		
		public AppTag() {
		}
		
		/** Obtain the name of the tag, which is a unique name used by the app store website as an identifier */
		public String getName() {
			return name;
		}
		
		/** Obtain the name of the tag that is shown to the user */
		public String getFullName() {
			return fullName;
		}
		
		/** Obtain the number of apps known by the web store to be associated with this tag */
		public int getCount() {
			return count;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}
		
		public void setCount(int count) {
			this.count = count;
		}
		
		@Override
		public String toString() {
			return fullName + " (" + count + ")";
		}
	}
	
	public WebQuerier(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		/*
		// *** Older initialization for previous implementation supporting a single app store page
		apps = null;
		appTags = new HashMap<>();
		appsByTagName = new HashMap<>();
		*/
		
		appsByUrl = new HashMap<>();
		appTagsByUrl = new HashMap<>();
		appsByTagNameByUrl = new HashMap<>();
		
		appsByUrl.put(currentAppStoreUrl, null);
		appTagsByUrl.put(currentAppStoreUrl, new HashMap<>());
		appsByTagNameByUrl.put(currentAppStoreUrl, new HashMap<>());
		
		/*
		Set<WebApp> webApps = getAllApps();
		DebugHelper.print("Apps found: " + webApps.size());
		*/
	}
	
	
	public void setShowMultipleWarnings(boolean show) {
		this.showMultipleWarnings = show;
	}
	
	/**
	 * Makes a HTTP query using the given URL and returns the response as a string.
	 * @param url The URL used to make the HTTP request
	 * @return The response, as a string
	 * @throws IOException If there was an error while attempting to make a connection
	 * to the given URL
	 */
	private String query(String url) throws IOException {
		// Convert the string url to a URL object
		URL parsedUrl = null;
		
		try {
			parsedUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new IOException("Malformed url, " + e.getMessage());
		}
		
		String result = null;
		
		StreamUtil streamUtil = serviceRegistrar.getService(StreamUtil.class);
		HttpURLConnection connection = (HttpURLConnection) streamUtil.getURLConnection(parsedUrl);
		connection.setRequestProperty(REQUEST_JSON_HEADER_KEY, REQUEST_JSON_HEADER_VALUE);
		//Set the read timeout to 10 seconds.
		//TODO: This is likely way too long and should be set to something more reasonable.
		connection.setReadTimeout(10000);
		connection.connect();
		
		InputStream inputStream = connection.getInputStream();
		result = IOUtils.toString(inputStream, "UTF-8");
		
		connection.disconnect();
		
		return result;
	}

	public void setAppManager(AppManager appManager) {
		this.appManager = appManager;
	}
	
	public String getDefaultAppStoreUrl() {
		return DEFAULT_APP_STORE_URL;
	}
	
	public String getCurrentAppStoreUrl() {
		return currentAppStoreUrl;
	}
	
	/**
	 * Sets the current base url used for app store queries. If the url is malformed,
	 * no change is made.
	 * 
	 * @param url The base url of the app store, e.g. https://apps.cytoscape.org/
	 */
	public void setCurrentAppStoreUrl(String url) {
		boolean malformed = false;
		
		try {
			URL checkMalformed = new URL(url);
		} catch (MalformedURLException e) {
			malformed = true;
			logger.warn("Malformed URL: " + url + ", " + e.getMessage());
		}
		
		if (!malformed) {
			if (!url.trim().endsWith("/")) {
				currentAppStoreUrl = url + "/";
			} else {
				currentAppStoreUrl = url;
			}
			
			if (appsByUrl.get(currentAppStoreUrl) == null) {
				appsByUrl.put(currentAppStoreUrl, null);
			}
			
			if (appTagsByUrl.get(currentAppStoreUrl) == null) {
				appTagsByUrl.put(currentAppStoreUrl, new HashMap<>());
			}
			
			if (appsByTagNameByUrl.get(currentAppStoreUrl) == null) {
				appsByTagNameByUrl.put(currentAppStoreUrl, new HashMap<>());
			}
		}
	}
	
	
	public void setCurrentSiteName(String site) {
		this.currentSiteName = site;
	}
	
	/**
	 * Return the set of all tag names found on the app store. 
	 * @return The set of all available tag names
	 */
	public Set<AppTag> getAllTags() {
		// Make a query for all apps if not done so; tag information for each app is returned
		// by the web store and is used to build a set of all available tags
		Set<WebApp> apps = getAllApps();
		
		return new HashSet<>(appTagsByUrl.get(currentAppStoreUrl).values());
	}

	public boolean appsHaveBeenLoaded() {
		return this.appsByUrl.get(currentAppStoreUrl) != null;
	}
	
	public Set<WebApp> getAllApps() {
		return getAllApps(false);
	}
	
	public Set<WebApp> getAllApps(boolean forceRefresh) {
		if (!showMultipleWarnings && appManager != null && appManager.getAppManagerDialog() != null) {
			appManager.getAppManagerDialog().hideNetworkError();
		}
		
		// If we have a cached result from the previous query, use that one
		if (!forceRefresh && this.appsByUrl.get(currentAppStoreUrl) != null) {
			return this.appsByUrl.get(currentAppStoreUrl);
		}
		
		DebugHelper.print("Obtaining apps from app store..");

		Set<WebApp> result = new HashSet<>();
		
		String jsonResult = null;
		try {
			// Obtain information about the app from the website
			jsonResult = query(currentAppStoreUrl + "backend/all_apps");

			// Parse the JSON result
			JSONArray jsonArray = new JSONArray(jsonResult);
			JSONObject jsonObject = null;
			String keyName;
			
			for (int index = 0; index < jsonArray.length(); index++) {
				jsonObject = jsonArray.getJSONObject(index);
				
				WebApp webApp = new WebApp();
				
				keyName = "fullname";
				if (jsonObject.has(keyName)) {
					webApp.setName(jsonObject.get(keyName).toString());
					webApp.setFullName(jsonObject.get(keyName).toString());
				} else {
					continue;
				}
				
				keyName = "icon_url";
				if (jsonObject.has(keyName)) {
					webApp.setIconUrl(jsonObject.get(keyName).toString());
				}
				
				keyName = "page_url";
				if (jsonObject.has(keyName)) {
					webApp.setPageUrl(currentAppStoreUrl.substring(0, currentAppStoreUrl.length() - 1) + jsonObject.get(keyName).toString());
				}
				
				keyName = "description";
				if (jsonObject.has(keyName)) {
					webApp.setDescription(jsonObject.get(keyName).toString());
				}
				
				keyName = "downloads";
				if (jsonObject.has(keyName)) {
					try {
						webApp.setDownloadCount(Integer.parseInt(jsonObject.get(keyName).toString()));
					} catch (NumberFormatException e) { }
				}
				
				keyName = "stars_percentage";
				if (jsonObject.has(keyName)) {
					try {
						webApp.setStarsPercentage(Integer.parseInt(jsonObject.get(keyName).toString()));
					} catch (NumberFormatException e) { }
				}
				
				keyName = "votes";
				if (jsonObject.has(keyName)) {
					try {
						webApp.setVotes(Integer.parseInt(jsonObject.get(keyName).toString()));
					} catch (NumberFormatException e) { }
				}
				
				keyName = "citation";
				if (jsonObject.has(keyName)) {
					webApp.setCitation(jsonObject.get(keyName).toString());
				}
				
				try {
					List<WebApp.Release> releases = new LinkedList<>();
					
					if (jsonObject.has("releases")) {
						JSONArray jsonReleases = jsonObject.getJSONArray("releases");
						JSONObject jsonRelease;
						boolean isCompatible = true;
						
						for (int releaseIndex = 0; releaseIndex < jsonReleases.length(); releaseIndex++) {
							jsonRelease = jsonReleases.getJSONObject(releaseIndex);
							
							WebApp.Release release = webApp.new Release();
							
							release.setBaseUrl(currentAppStoreUrl);
							release.setRelativeUrl(jsonRelease.optString("release_download_url"));
							release.setReleaseDate(jsonRelease.optString("created_iso"));
							release.setReleaseVersion(jsonRelease.optString("version"));							
							//release.setSha512Checksum(jsonRelease.optString("hexchecksum"));
							
							keyName = "works_with";
							if (jsonRelease.has(keyName)) {
								release.setCompatibleCytoscapeVersions(jsonRelease.get(keyName).toString());
								isCompatible = release.isCompatible(serviceRegistrar.getService(CyVersion.class));
							}
							
							if (isCompatible)
								releases.add(release);
						}
	
						// Sort releases by version number
						Collections.sort(releases, Comparator.comparing(Release::getReleaseVersion, ( a, b ) -> {	
							return -compareVersions(a,b);
						}));
					}
					
					webApp.setReleases(releases);
				} catch (JSONException e) {
					logger.warn("Error obtaining releases for app: " + webApp.getFullName() + ", " + e.getMessage());
				}
				
				// DebugHelper.print("Obtaining ImageIcon: " + iconUrlPrefix + webApp.getIconUrl());
				// webApp.setImageIcon(new ImageIcon(new URL(iconUrlPrefix + webApp.getIconUrl())));

				
				// Check the app for compatible releases
				List<WebApp.Release> compatibleReleases = getCompatibleReleases(webApp);
				
				// Only add this app if it has compatible releases
				if (compatibleReleases.size() > 0) {
					// Obtain tags associated with this app
					processAppTags(webApp, jsonObject);
					result.add(webApp);
				}
			}
			
		} catch (final IOException e) {
			if (appManager != null && appManager.getAppManagerDialog() != null) {
				var dialog = appManager.getAppManagerDialog();
				if(currentSiteName == null)
					dialog.showNetworkError("Cannot access the App Store at: " + currentAppStoreUrl);
				else
					dialog.showNetworkError("Cannot access " + currentSiteName + " at: " + currentAppStoreUrl);
			}
			e.printStackTrace();
			result = null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			DebugHelper.print("Error parsing JSON: " + e.getMessage());
			e.printStackTrace();
		}
	
		
		//DebugHelper.print(result.size() + " apps found from web store.");
		
		// Cache the result of this query
		this.appsByUrl.put(currentAppStoreUrl, result);
		return result;
	}
	
	private void processAppTags(WebApp webApp, JSONObject jsonObject) throws JSONException {
		// Obtain tags associated with this app from the JSONObject representing the app data in JSON format obtained
		// from the web store
		
		JSONArray appTagObjects = jsonObject.optJSONArray("tags");
		if (appTagObjects == null) {
			return;
		}
		
		for (int index = 0; index < appTagObjects.length(); index++) {
			/*
			JSONObject appTagObject = appTagObjects.getJSONObject(index);
			
			String appTagName = appTagObject.get("fullname").toString();
			*/
			
			String appTagName = appTagObjects.get(index).toString();
			
			AppTag appTag = appTagsByUrl.get(currentAppStoreUrl).get(appTagName);
			
			if (appTag == null) {
				appTag = new AppTag();
				appTag.setName(appTagName);
				// appTag.setFullName(appTagObject.get("fullname").toString());
				appTag.setFullName(appTagName);
			
				appTag.setCount(0);
				appTagsByUrl.get(currentAppStoreUrl).put(appTagName, appTag);
			}
			
			
			webApp.getAppTags().add(appTag);
			
			// Add the app information for this tag to the map which keeps apps categorized by tag
			if (appsByTagNameByUrl.get(currentAppStoreUrl).get(appTagName) == null) {
				appsByTagNameByUrl.get(currentAppStoreUrl).put(appTagName, new HashSet<WebApp>());
			}
			
			appsByTagNameByUrl.get(currentAppStoreUrl).get(appTagName).add(webApp);
			appTag.setCount(appTag.getCount() + 1);
		}
	}
	
	/**
	 * Given the unique app name used by the app store, query the app store for the 
	 * download URL and download the app to the given directory.
	 * 
	 * If a file with the same name exists in the directory, it is overwritten.
	 * 
	 * @param appName The unique app name used by the app store
	 * @param version The desired version, or <code>null</code> to obtain the latest release
	 * @param directory The directory used to store the downloaded file
	 * @param taskMonitor 
	 */
	public File downloadApp(WebApp webApp, String version, File directory, DownloadStatus status)
			throws AppDownloadException {
		List<WebApp.Release> compatibleReleases = getCompatibleReleases(webApp);

		if (compatibleReleases.size() > 0) {
			WebApp.Release releaseToDownload = null;
			
			if (version != null) {
				for (WebApp.Release compatibleRelease : compatibleReleases) {
					
					// Check if the desired version is found in the list of available versions
					if (compatibleRelease.getReleaseVersion().matches(
							"(^\\s*|.*,)\\s*" + version + "\\s*(\\s*$|,.*)")) {
						releaseToDownload = compatibleRelease;
					}
				}
				
				if (releaseToDownload == null) {
					throw new AppDownloadException("No release with the requested version " + version
							+ " was found for the requested app " + webApp.getFullName());
				}
			} else {
				releaseToDownload = compatibleReleases.get(compatibleReleases.size() - 1);
			}
			
			URL downloadUrl = null;
			try {
				downloadUrl = new URL(currentAppStoreUrl + releaseToDownload.getRelativeUrl());
			} catch (MalformedURLException e) {
				throw new AppDownloadException("Unable to obtain URL for version " + version 
						+ " of the release for " + webApp.getFullName());
			}
			
			if (downloadUrl != null) {
				try {
					// Prepare to download
					StreamUtil streamUtil = serviceRegistrar.getService(StreamUtil.class);
					URLConnection connection = streamUtil.getURLConnection(downloadUrl);
					InputStream inputStream = connection.getInputStream();
					long contentLength = connection.getContentLength();
					ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
					
					File outputFile;
					try {
						// Replace spaces with underscores
						String outputFileBasename = webApp.getName().replaceAll("\\s", "_");
						
						// Append version information
						outputFileBasename += "-v" + releaseToDownload.getReleaseVersion();
						
						// Strip disallowed characters
						outputFileBasename = OUTPUT_FILENAME_DISALLOWED_CHARACTERS.matcher(outputFileBasename).replaceAll("");
						
						// Append extension
						outputFileBasename += ".jar";
						
						// Output file has same name as app, but spaces and slashes are replaced with hyphens
						outputFile = new File(directory.getCanonicalPath() + File.separator 
								+ outputFileBasename);
						
						if (outputFile.exists()) {
							outputFile.delete();
						}
						
						outputFile.createNewFile();
						
					    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
					    try {
						    FileChannel fileChannel = fileOutputStream.getChannel();
						    
						    long currentDownloadPosition = 0;
						    long bytesTransferred;
						    
						    TaskMonitor taskMonitor = status.getTaskMonitor();
						    do {
						    	bytesTransferred = fileChannel.transferFrom(readableByteChannel, currentDownloadPosition, 1 << 14);
						    	if (status.isCanceled()) {
						    		outputFile.delete();
						    		return null;
						    	}
						    	currentDownloadPosition += bytesTransferred;
						    	if (contentLength > 0) {
						    		double progress = (double) currentDownloadPosition / contentLength;
						    		taskMonitor.setProgress(progress);
						    	}
						    } while (bytesTransferred > 0);
					    } finally {
					    	fileOutputStream.close();
					    }
					} finally {
				    	readableByteChannel.close();
				    }
				    return outputFile;
				} catch (IOException e) {
					throw new AppDownloadException("Error while downloading app " + webApp.getFullName()
							+ ", " + e.getMessage());
				}
			}
		} else {
			throw new AppDownloadException("No available releases were found for the app " 
					+ webApp.getFullName() + ".");
		}
		
		return null;
	}

	/**
	 * Returns the list of compatible releases, in chronological order starting with the earliest.
	 * @param webApp
	 * @return
	 */
	private List<WebApp.Release> getCompatibleReleases(WebApp webApp) {
		List<WebApp.Release> compatibleReleases = new LinkedList<WebApp.Release>();
		
		for (WebApp.Release release : webApp.getReleases()) {
			
			// Get releases that are compatible with the current version of Cytoscape (version 3)
			if (release.getCompatibleCytoscapeVersions().matches(COMPATIBLE_RELEASE_REGEX)) {
				compatibleReleases.add(release);
			}
		}
		
		return compatibleReleases;
	}
	
	public Set<WebApp> getAppsByTag(String tagName) {
		// Query for apps (which includes tag information) if not done so
		Set<WebApp> webApps = getAllApps();
		
		return appsByTagNameByUrl.get(currentAppStoreUrl).get(tagName);
	}
	
	public Set<Update> checkForUpdates(Set<App> apps, AppManager appManager) {
		Set<Update> updates = new HashSet<>();
		
		for (App app : apps) {
			for (String url : appsByUrl.keySet()) {
				Update update = checkForUpdate(app, url, appManager);
				
				if (update != null) {
					updates.add(update);
					break;
				}
			}
		}
		
		return updates;
	}
	
	public void checkWebAppInstallStatus(Set<WebApp> webApps, AppManager appManager) {
		
		// This method contains a nest structure with 3 for loops. However,
		// there isn't much other way to check every hash of every WebApp with every
		// installed app. Runtime performance is
		// (num_installed_apps * num_web_apps * releases_per_web_app)
		
		for (App app : appManager.getInstalledApps()) {
			for (WebApp webApp : webApps) {
				for (Release release : webApp.getReleases()) {
					if (releaseEquals(app, release)) {
						if(webApp.getCorrespondingApp() == null || webApp.getCorrespondingApp().isDetached() || compareVersions(webApp.getCorrespondingApp().getVersion(), app.getVersion()) > 0) {
							webApp.setCorrespondingApp(app);
							// For convenience, set the app's description field
							if (app.getDescription() == null) {
								app.setDescription(webApp.getDescription());
							}
						}
					}
				}
			}
		}
	}
	
	
	private static boolean releaseEquals(App app, Release release) {
		return app.getAppName().equals(release.getWebApp().getName()) && 
				WebQuerier.compareVersions(app.getVersion(), release.getReleaseVersion()) == 0;
	}
	
	
	private Update checkForUpdate(App app, String url, AppManager appManager) {
		Set<WebApp> urlApps = appsByUrl.get(url);
		
		if (urlApps != null) {
			
			// Look for an app with same name
			for (WebApp webApp : urlApps) {
				
				if (webApp.getName().equalsIgnoreCase(app.getAppName())) {
					
					WebApp.Release highestVersionRelease = null;
					for (WebApp.Release release : webApp.getReleases()) {
						
						if (highestVersionRelease == null
								|| compareVersions(highestVersionRelease.getReleaseVersion(), 
										release.getReleaseVersion()) > 0) {
							
							highestVersionRelease = release;
						}
					}
					
					if (highestVersionRelease != null
							&& compareVersions(highestVersionRelease.getReleaseVersion(), app.getVersion()) < 0) {
						
						Update update = new Update();
						update.setUpdateVersion(highestVersionRelease.getReleaseVersion());
						update.setApp(app);
						update.setWebApp(webApp);
						update.setRelease(highestVersionRelease);
						return update;
					}
				}
			}
		}
		return null;
	}
	
	// Find which version is more recent, assuming versions are in format x.y[.z[.qualifier]]
	
	/**
	 * Compares 2 versions, assuming they are in format x.y[.z[.qualifier]], returning a negative
	 * number if the first is more recent, a positive number if the second is more recent,
	 * or 0 if the versions were the same or unable to determine which is more recent.
	 * 
	 * @param version1 The first version
	 * @param version2 The second version
	 * @return A negative integer if first more recent, a positive integer if second more recent,
	 * or 0 if the versions were the same or unable to determine which is more recent.
	 */
	public static int compareVersions(String version1, String version2) {
		Matcher matcher1 = VERSION_PATTERN.matcher(version1);
		Matcher matcher2 = VERSION_PATTERN.matcher(version2);
		
		if (!matcher1.matches()) {
			throw new IllegalArgumentException("Incorrectly-formatted version string: " + version1);
		}
		if (!matcher2.matches()) {
			throw new IllegalArgumentException("Incorrectly-formatted version string: " + version2);
		}

		// major = 1, minor = 3, micro = 5, qualifier = 7
		for (int i = 1; i < 8; i += 2) {
			String part1 = matcher1.group(i);
			String part2 = matcher2.group(i);
			
			if (part1 == null && part2 == null) {
				return 0;
			}
			if (i < 7) {
				// major/minor/micro
				if (part1 != null && part2 == null) {
					return Integer.parseInt(part1) == 0 ? 0 : -1;
				}
				if (part1 == null && part2 != null) {
					return Integer.parseInt(part2) == 0 ? 0 : 1;
				}
				int result = Integer.compare(Integer.parseInt(part1), Integer.parseInt(part2));
				
				if (result != 0) {
					return -result;
				}
			} else {
				// qualifier
				if (part1 != null && part2 == null) {
					return -1;
				}
				if (part1 == null && part2 != null) {
					return 1;
				}
				int result = part1.compareTo(part2);
				
				if (result != 0) {
					return -result;
				}
			}
			
		}
		return 0;
	}
	
	public void findAppDescriptions(Set<App> apps) {
		// TODO: Perhaps modify this method to do the check with a given app store
		// as a parameter, instead of all app stores
		
		if (appsByUrl.get(DEFAULT_APP_STORE_URL) == null) {
			return;
		}
		
		// Find the set of all available apps
		Set<WebApp> allWebApps = new HashSet<>(appsByUrl.get(DEFAULT_APP_STORE_URL).size());
		
		for (String url : appsByUrl.keySet()) {
			Set<WebApp> urlApps = appsByUrl.get(url);
			for (WebApp webApp : urlApps) {
				allWebApps.add(webApp);
			}
		}
		
		for (WebApp webApp : allWebApps) {
			for (Release release : webApp.getReleases()) {
				for (App app : apps) {
					
					// TODO: Currently, this will give the app the description from the
					// first app store providing the matching hash. Perhaps
					// we could give the default app store the priority in providing the description,
					// in cases where multiple stores give the same hash.
					
					if (app.getDescription() == null && releaseEquals(app, release)) {
	
						// WebQuerier obtains app information from app store because no description metadata is required
						// in the app zip file itself. This was to allow better App-Bundle interchangeability, not
						// imposing unneeded restrictions on OSGi bundles (from past discussion on mailing list, some time in 2012)
						
						app.setDescription(release.getWebApp().getDescription());
					}
				}
			}
		}
	}
	
}
