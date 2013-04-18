package org.cytoscape.app.internal.net.server;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.DownloadStatus;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.json.JSONObject;

/**
 * This class is responsible for handling GET requests received by the local HTTP server.
 */
public class AppGetResponder {
    private static final CyHttpResponseFactory responseFactory = new CyHttpResponseFactoryImpl();

	private AppManager appManager;
	
	public AppGetResponder(AppManager appManager) {
		this.appManager = appManager;
	}

    private abstract static class JsonResponder implements CyHttpResponder {
        protected abstract Map<String,String> jsonRespond(CyHttpRequest request, Matcher matchedURI);

        public CyHttpResponse respond(CyHttpRequest request, Matcher matchedURI) {
            final Map<String,String> responseData = jsonRespond(request, matchedURI);
            JSONObject jsonObject = new JSONObject(responseData);
            return responseFactory.createHttpResponse(jsonObject.toString(), "application/json");
        }
    }

    public class StatusResponder extends JsonResponder {
        final Pattern pattern = Pattern.compile("^/status/(.*)$");

        public Pattern getURIPattern() {
            return pattern;
        }

        protected Map<String,String> jsonRespond(CyHttpRequest request, Matcher matchedURI) {
            Map<String, String> responseData = new HashMap<String, String>();
            String appName = matchedURI.group(1);
            if (appName != null && appName.length() != 0) {
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
            return responseData;
        }
    }

    public class InstallResponder extends JsonResponder {
        final Pattern pattern = Pattern.compile("^/install/(.+)/(.+)$");

        public Pattern getURIPattern() {
            return pattern;
        }

        protected Map<String,String> jsonRespond(CyHttpRequest request, Matcher matchedURI) {
            Map<String, String> responseData = new HashMap<String, String>();
			String appName = matchedURI.group(1);
			final String version = matchedURI.group(2);
			
			if (appName != null && appName.length() != 0 && version != null && version.length() != 0) {
				// Use the WebQuerier to obtain the app from the app store using the app name and version
				//responseBody = "Will obtain \"" + appName + "\", version " + version;

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
					final File[] result = new File[1];
					final Semaphore semaphore = new Semaphore(0);
					
					final WebApp webApp = appToDownload;
					TaskManager<?, ?> taskManager = appManager.getSwingAppAdapter().getTaskManager();
					taskManager.execute(new TaskIterator(new AbstractTask() {
						private DownloadStatus status;

						@Override
						public void run(TaskMonitor taskMonitor) throws Exception {
							try {
								taskMonitor.setStatusMessage("Installing app: " + webApp.getFullName());
								status = new DownloadStatus(taskMonitor);
								result[0] = appManager.getWebQuerier().downloadApp(
										webApp, version, new File(appManager.getDownloadedAppsPath()), status);
							} finally {
								semaphore.release();
							}
						}
						
						public void cancel() {
							if (status != null) {
								status.cancel();
							}
						};
					}));
					
					semaphore.acquireUninterruptibly();
					File appFile = result[0];
					
					// Attempt to install app
					if (appFile == null) {
						installStatus = "version-not-found";
						installError = "An entry for the app " + appName + " with version " + version
							+ " was not found in the app store database at: " + appManager.getWebQuerier().getDefaultAppStoreUrl();
					} else {
						installStatus = "success";
						
						try {
							App app = appManager.getAppParser().parseApp(appFile);
							installOrUpdate(app);
						} catch (AppParsingException e) {
							installStatus = "install-failed";
							installError = "The installation could not be completed because there were errors in the app file. "
								+ "Details: " + e.getMessage();
						} catch (AppInstallException e) {
							installStatus = "install-failed";
							installError = "The app file passed checking, but the app manager encountered errors while attempting" 
								+ " to install. Details: " + e.getMessage();
						} catch (AppUninstallException e) {
							installStatus = "install-failed";
							installError = "The app file passed checking, but the app manager encountered errors while attempting" 
								+ " to uninstall old version. Details: " + e.getMessage();
						}
					}
				} else {
					installStatus = "app-not-found";
					installError = "The app " + appName + " is not found in the app store database at "
						+ appManager.getWebQuerier().getDefaultAppStoreUrl();
				}
				
				responseData.put("install_status", installStatus);
				responseData.put("install_error", installError);
            }
            return responseData;
        }

		private void installOrUpdate(App app) throws AppInstallException, AppUninstallException {
			// Check if another version of the app is already installed.
			App installedApp = getInstalledApp(app);
			if (installedApp != null) {
				appManager.uninstallApp(installedApp);
			}
			appManager.installApp(app);
		}
		
		private App getInstalledApp(App referenceApp) {
			App installedApp = null;
			for (App app : appManager.getApps()) {
				if (app.getStatus() == AppStatus.INSTALLED && referenceApp.getAppName().equals(app.getAppName())) {
					// Find the app with the most recent version if multiple versions are found.
					if (installedApp == null || WebQuerier.compareVersions(referenceApp.getVersion(), installedApp.getVersion()) < 0) {
						installedApp = app;
					}
				}
			}
			return installedApp;
		}
    }
}
