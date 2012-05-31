package org.cytoscape.app.internal.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.cytoscape.app.internal.exception.AppDownloadException;
import org.cytoscape.app.internal.util.DebugHelper;
import org.cytoscape.io.util.StreamUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is responsible for querying the Cytoscape App Store web service to obtain
 * information about available apps and app updates.
 */
public class WebQuerier {
	
	private static final String APP_STORE_URL = "http://apps.cytoscape.org/";
	
	private static final String REQUEST_JSON_HEADER_KEY = "X-Requested-With";
	private static final String REQUEST_JSON_HEADER_VALUE = "XMLHttpRequest";
	
	private StreamUtil streamUtil;
	
	/** A reference to the result obtained by the last successful query for all available apps. */
	private Set<WebApp> apps;
	
	/** A reference to a map which keeps track of the known set of apps for each known tag */
	private Map<String, Set<WebApp>> appsByTagName;
	
	/** A reference to the result obtained by the last successful query for all available app tags. */
	private Map<String, AppTag> appTags;
	
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
	
	public WebQuerier(StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
		
		apps = null;
		appTags = new HashMap<String, AppTag>();
		appsByTagName = new HashMap<String, Set<WebApp>>();
		
		/*
		Set<WebApp> webApps = getAllApps();
		
		DebugHelper.print("Apps found: " + webApps.size());
		*/
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
	
		HttpURLConnection connection = (HttpURLConnection) streamUtil.getURLConnection(parsedUrl);
		connection.setRequestProperty(REQUEST_JSON_HEADER_KEY, REQUEST_JSON_HEADER_VALUE);
		connection.connect();
		
		InputStream inputStream = connection.getInputStream();
		result = IOUtils.toString(inputStream, "UTF-8");
		
		connection.disconnect();
		
		return result;
	}
	
	public String getAppStoreUrl() {
		return APP_STORE_URL;
	}
	
	/**
	 * Return the set of all tag names found on the app store. 
	 * @return The set of all available tag names
	 */
	public Set<AppTag> getAllTags() {
		// Make a query for all apps if not done so; tag information for each app is returned
		// by the web store and is used to build a set of all available tags
		Set<WebApp> apps = getAllApps();
		
		return new HashSet(appTags.values());
	}
	
	public Set<WebApp> getAllApps() {
		// If we have a cached result from the previous query, use that one
		if (apps != null) {
			return apps;
		}
		
		DebugHelper.print("Obtaining apps from app store..");
		String iconUrlPrefix = APP_STORE_URL.substring(0, APP_STORE_URL.length() - 1);
		
		Set<WebApp> result = new HashSet<WebApp>();
		
		String jsonResult = null;
		try {
			// Obtain information about the app from the website
			jsonResult = query(APP_STORE_URL + "backend/all_apps");
			
			// Parse the JSON result
			JSONArray jsonArray = new JSONArray(jsonResult);
			JSONObject jsonObject = null;
			
			for (int index = 0; index < jsonArray.length(); index++) {
				jsonObject = jsonArray.getJSONObject(index);
				
				WebApp webApp = new WebApp();
				webApp.setName(jsonObject.get("fullname").toString());
				webApp.setFullName(jsonObject.get("fullname").toString());
				webApp.setIconUrl(jsonObject.get("icon_url").toString());
				
				webApp.setPageUrl(APP_STORE_URL.substring(0, APP_STORE_URL.length() - 1) 
						+ jsonObject.get("page_url").toString());
				webApp.setDescription(jsonObject.get("description").toString());
				webApp.setDownloadCount(jsonObject.getInt("downloads"));
				
				try {
					webApp.setStarsPercentage(Integer.parseInt(jsonObject.get("stars_percentage").toString()));
					webApp.setVotes(Integer.parseInt(jsonObject.get("votes").toString()));
				} catch (NumberFormatException e) {
				}
				
				// DebugHelper.print("Obtaining ImageIcon: " + iconUrlPrefix + webApp.getIconUrl());
				// webApp.setImageIcon(new ImageIcon(new URL(iconUrlPrefix + webApp.getIconUrl())));

				// Obtain tags associated with this app
				processAppTags(webApp, jsonObject);

				result.add(webApp);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			DebugHelper.print("Error parsing JSON: " + e.getMessage());
			e.printStackTrace();
		}
	
		
		DebugHelper.print(result.size() + " apps found from web store.");
		
		// Cache the result of this query
		this.apps = result;
		return result;
	}
	
	private void processAppTags(WebApp webApp, JSONObject jsonObject) throws JSONException {
		// Obtain tags associated with this app from the JSONObject representing the app data in JSON format obtained
		// from the web store
		
		JSONArray appTagObjects = jsonObject.getJSONArray("tags");
		
		for (int index = 0; index < appTagObjects.length(); index++) {
			/*
			JSONObject appTagObject = appTagObjects.getJSONObject(index);
			
			String appTagName = appTagObject.get("fullname").toString();
			*/
			
			String appTagName = appTagObjects.get(index).toString();
			
			AppTag appTag = appTags.get(appTagName);
			
			if (appTag == null) {
				appTag = new AppTag();
				appTag.setName(appTagName);
				// appTag.setFullName(appTagObject.get("fullname").toString());
				appTag.setFullName(appTagName);
			
				appTag.setCount(0);
				appTags.put(appTagName, appTag);
			}
			
			
			webApp.getAppTags().add(appTag);
			
			// Add the app information for this tag to the map which keeps apps categorized by tag
			if (appsByTagName.get(appTagName) == null) {
				appsByTagName.put(appTagName, new HashSet<WebApp>());
			}
			
			appsByTagName.get(appTagName).add(webApp);
			appTag.setCount(appTag.getCount() + 1);
		}
	}
	
	public String getAppDescription(String appName) {
		// Obtain information about the app from the website
		String jsonResult = null;
		JSONObject jsonObject = null;
		
		try {
			jsonResult = query(APP_STORE_URL + "apps/" + appName);
			
			// Parse the JSON result
			jsonObject = new JSONObject(jsonResult);
			return jsonObject.get("description").toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			DebugHelper.print("Error parsing JSON: " + e.getMessage());
			e.printStackTrace();
		}
		
		return "";
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
	 */
	public File downloadApp(String appName, String version, File directory) throws AppDownloadException {
		Set<WebApp> apps = getAllApps();
		
		// Check that the app was found in the app store's current list of apps
		boolean appFound = false;
		for (WebApp webApp : apps) {
			if (webApp.getName().equalsIgnoreCase(appName)) {
				appFound = true;
				break;
			}
		}
		
		if (appFound) {
			try {

				// Find the download url
				String releaseUrl = getReleaseUrl(appName, version);
				
				if (releaseUrl != null) {
					URL downloadUrl = new URL(releaseUrl);
					
					// Prepare to download
					ReadableByteChannel readableByteChannel = Channels.newChannel(downloadUrl.openStream());
					
					File outputFile = new File(directory.getCanonicalPath() + File.separator + appName + ".jar");
					
					if (outputFile.exists()) {
						outputFile.delete();
					}
					
					outputFile.createNewFile();
					
				    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
				    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, 1 << 24);
				    
				    return outputFile;
				}
			    
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			DebugHelper.print("No app with name " + appName + " found.");
		}
		
		return null;
	}
	
	
	/**
	 * Obtain the release URL for a given app and version by querying the app store.
	 * 
	 * This method assumes that the appName parameter corresponds to the name of an
	 * app available on the web store.
	 * 
	 * @param appName The name of the app whose release URL is to be looked for
	 * @param version The desired version of the app, or <code>null</code> to obtain
	 * the the latest release 
	 * @return The download URL of the app, or <code>null</code> if no release was
	 * available, or a release with the given version was not found.
	 */
	private String getReleaseUrl(String appName, String version) {
		
		try {
			String jsonResult = query(APP_STORE_URL + "apps/" + appName);
			
			JSONObject jsonObject = new JSONObject(jsonResult);
		
			JSONArray releases = jsonObject.getJSONArray("releases");
			
			DebugHelper.print("Releases for " + appName + ": " + releases.length());
			
			String latestReleaseUrl = null;
			String latestReleaseDate = null;
			String releaseDate;
			
			for (int index = 0; index < releases.length(); index++) {
				JSONObject release = releases.getJSONObject(index);
			
				if (version != null) {
					if (release.getString("version").equalsIgnoreCase(version)) {
						latestReleaseUrl = APP_STORE_URL + release.getString("release_file_url");
					}
				} else {
					// If version was null, look for the latest release
					releaseDate = release.getString("created_iso");
					if (latestReleaseDate == null || releaseDate.compareToIgnoreCase(latestReleaseDate) >= 0) {
						latestReleaseUrl = APP_STORE_URL + release.getString("release_file_url");
						latestReleaseDate = releaseDate;
					}
				}
			}
			
			return latestReleaseUrl;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
		
	}
	
	
	public Set<WebApp> getAppsByTag(String tagName) {
		// Query for apps (which includes tag information) if not done so
		Set<WebApp> webApps = getAllApps();
		
		return appsByTagName.get(tagName);
		
		/*
		 
		// Construct a map used to quickly obtain references to WebApp objects given the app's name.
		// The app's name is guaranteed to be unique by the app store website.
		Map<String, WebApp> appMap = new HashMap<String, WebApp>();
		for (WebApp webApp : webApps) {
			appMap.put(webApp.getName(), webApp);
		}
		
		Set<WebApp> result = new HashSet<WebApp>();
		
		// Query for apps that match the given tag
		String jsonResult = null;
		try {
			// Obtain information about the app from the website
			jsonResult = query(APP_STORE_URL + "apps/with_tag/" + tagName);
			
			// Parse the JSON result
			JSONArray jsonArray = new JSONArray(jsonResult);
			JSONObject jsonObject = null;
			
			for (int index = 0; index < jsonArray.length(); index++) {
				jsonObject = jsonArray.getJSONObject(index);
				
				String appName = jsonObject.get("name").toString();

				// Assume any app obtained by querying with the tag was
				// already obtained by the query for all available apps
				result.add(appMap.get(appName));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			DebugHelper.print("Error parsing JSON: " + e.getMessage());
			e.printStackTrace();
		}
		
		return result;
		
		*/
	}
}
