package org.cytoscape.app.internal.net.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.app.internal.exception.AppDownloadException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.server.LocalHttpServer.Response;
import org.cytoscape.app.internal.util.DebugHelper;
import org.json.JSONObject;

/**
 * This class is responsible for handling GET requests received by the local HTTP server.
 */
public class AppGetResponder implements LocalHttpServer.GetResponder{

	private static final String STATUS_QUERY_URL = "status";
	private static final String STATUS_QUERY_APP_NAME = "appname";
	
	private static final String INSTALL_QUERY_URL = "install";
	private static final String INSTALL_QUERY_APP_NAME = "appname";
	private static final String INSTALL_QUERY_APP_VERSION = "version";
	
	private AppManager appManager;
	
	@Override
	public boolean canRespondTo(String url) throws Exception {
		return true;
	}

	@Override
	public Response respond(String url) throws Exception {
		Map<String, String> parsed = parseEncodedUrl(url);
		
		DebugHelper.print("Request received. Url: " + url);
		DebugHelper.print("Url request prefix: " + getUrlRequestPrefix(url));
		DebugHelper.print("Parsed result of encoded url: " + parsed);

		String responseBody = "Empty";
		
		String urlRequestPrefix = getUrlRequestPrefix(url);
		
		Map<String, String> responseData = new HashMap<String, String>();
		if (urlRequestPrefix.equalsIgnoreCase(STATUS_QUERY_URL)) {
			
			String appName = parsed.get(STATUS_QUERY_APP_NAME);
			boolean appFound = false;
			
			if (appName != null) {
				
				String status = "not-found";
				String version = "not-found";
				
				// Searches web apps first. If not found, searches other apps using manifest name field.
				for (App app : appManager.getApps()) {
					if (app.getAppName().equalsIgnoreCase(appName)) {
						if (app.getStatus() != null) {
							status = app.getStatus().toString().toLowerCase();
						}
						
						if (app.getVersion() != null) {
							version = app.getVersion();
						}
					}
				}
				
				responseData.put("request_name", appName); // web unique identifier
				responseData.put("status", status);
				responseData.put("version", version);
			}
		// Are we being told to install an app remotely?
		} else if (urlRequestPrefix.equalsIgnoreCase(INSTALL_QUERY_URL)) {
			
			String appName = parsed.get(INSTALL_QUERY_APP_NAME);
			String version = parsed.get(INSTALL_QUERY_APP_VERSION);
			
			if (appName != null && version != null) {
				// Use the WebQuerier to obtain the app from the app store using the app name and version
				responseBody = "Will obtain \"" + appName + "\", version " + version;

				String installStatus = "app-not-found";
				String installError = "";
				boolean appFoundInStore = false;
				WebApp appToDownload = null;
				
				// Check if the app is available on the app store
				// TODO: Use a web query to do this?
				
				for (WebApp webApp : appManager.getWebQuerier().getAllApps()) {
					if (webApp.getName().equals(appName)) {
						appFoundInStore = true;
						appToDownload = webApp;
						break;
					}
				}
				responseData.put("name", appName);
				
				if (appFoundInStore) {
					
					// Download app
					File appFile = null;
					try {
						appFile = appManager.getWebQuerier().downloadApp(
							appToDownload, version, new File(appManager.getDownloadedAppsPath()));
					} catch (AppDownloadException e) {
					}
					
					// Attempt to install app
					if (appFile == null) {
						installStatus = "version-not-found";
						installError = "An entry for the app " + appName + " with version " + version
							+ " was not found in the app store database at: " + appManager.getWebQuerier().getDefaultAppStoreUrl();
					} else {
						installStatus = "success";
						
						try {
							App app = appManager.getAppParser().parseApp(appFile);
							
							appManager.installApp(app);
						} catch (AppParsingException e) {
							installStatus = "install-failed";
							installError = "The installation could not be completed because there were errors in the app file. "
								+ "Details: " + e.getMessage();
						} catch (AppInstallException e) {
							installStatus = "install-failed";
							installError = "The app file passed checking, but the app manager encountered errors while attempting" 
								+ "install. Details: " + e.getMessage();
						}
					}
				} else {
					installStatus = "app-not-found";
					installError = "The app " + appName + " is not found in the app store database at "
						+ appManager.getWebQuerier().getDefaultAppStoreUrl();
				}
				
				responseData.put("install_status", installStatus);
				responseData.put("install_error", installError);
				
			} else if (version == null) {
				
				responseData.put("install_status", "version-not-found");
				responseData.put("install_error", "The query to the app manager did not specify a desired app version.");
			}
		}
		
		JSONObject jsonObject = new JSONObject(responseData);

		responseBody = jsonObject.toString();
		responseBody += "\n";
		LocalHttpServer.Response response = new LocalHttpServer.Response(responseBody, "application/json");

		return response;
	}
	
	public AppGetResponder(AppManager appManager) {
		this.appManager = appManager;

	}
	
	/**
	 * Parses the parameters from an URL encoded in the application/x-www-form-urlencoded form, 
	 * which is the default form. This method uses a simple parsing method, only scanning text after the last '?' symbol
	 * and splitting with the '=' symbol. A more comprehensive (and possibly securer) parser can be found in the URLEncodedUtils 
	 * class of the Apache HttpClient library.
	 * 
	 * @param url The encoded URL in String form
	 * @return A map containing the encoded keys as keys and the corresponding encoded values as values.
	 */
	private Map<String, String> parseEncodedUrl(String url) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		int lastIndex = url.lastIndexOf("?");
		
		if (lastIndex != -1) {
			String paramSubstring = url.substring(lastIndex + 1);
			
			String[] splitParameters = paramSubstring.split("&");
			
			
			String key, value;
			int equalSignIndex;
			for (int i = 0; i < splitParameters.length; i++) {
				equalSignIndex = splitParameters[i].indexOf('=');
				
				if (equalSignIndex != -1) {
					key = splitParameters[i].substring(0, equalSignIndex);
					value = splitParameters[i].substring(equalSignIndex + 1);
					
					parameters.put(key, value);
				}
			}
		}
		
		return parameters;
	}
	
	/**
	 * Returns the request prefix for a URL encoded in the application/x-www-form-urlencoded form, ie.
	 * returns "installed" for "http://127.0.0.1:2608/installed?appname=3d&version=1.0.0"
	 */
	private String getUrlRequestPrefix(String url) {
		
		// Use the last occurrence of the question mark
		int lastIndex = url.lastIndexOf("?");
		
		if (lastIndex == -1) {
			return url.substring(1);
		} else { 
			return url.substring(url.indexOf("/") + 1, lastIndex);
		}
	}
}
