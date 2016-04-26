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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.task.InstallAppsFromWebAppTask;
import org.cytoscape.application.CyVersion;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;
import org.json.JSONObject;

/**
 * This class is responsible for handling GET requests received by the local HTTP server.
 */
public class AppGetResponder {
    private static final CyHttpResponseFactory responseFactory = new CyHttpResponseFactoryImpl();

	private AppManager appManager;
  private CyVersion cyVersion;
	
	public AppGetResponder(AppManager appManager, CyVersion cyVersion) {
		this.appManager = appManager;
    this.cyVersion = cyVersion;
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
            Map<String, String> responseData = new HashMap<>();
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

    public class InstallResponder extends JsonResponder implements TaskObserver {
        final Pattern pattern = Pattern.compile("^/install/(.+)/(.+)$");
		final Semaphore semaphore = new Semaphore(0);
        String installStatus = "app-not-found";
		String installError = "";

        public Pattern getURIPattern() {
            return pattern;
        }

        protected Map<String,String> jsonRespond(CyHttpRequest request, Matcher matchedURI) {
            Map<String, String> responseData = new HashMap<>();
			String appName = matchedURI.group(1);
			final String version = matchedURI.group(2);
			
			if (appName != null && appName.length() != 0 && version != null && version.length() != 0) {
				// Use the WebQuerier to obtain the app from the app store using the app name and version
				//responseBody = "Will obtain \"" + appName + "\", version " + version;

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
					final WebApp webApp = appToDownload;
					TaskManager<?, ?> taskManager = appManager.getSwingAppAdapter().getTaskManager();
					taskManager.execute(new TaskIterator(new InstallAppsFromWebAppTask(Collections.singletonList(webApp), appManager, true)), this);
					semaphore.acquireUninterruptibly();
				} else {
					installStatus = "install failed: this app is incompatible with your version of Cytoscape";
					installError = "The app " + appName + " is not found in the app store database at "
						+ appManager.getWebQuerier().getDefaultAppStoreUrl();
				}
				
				responseData.put("install_status", installStatus);
				responseData.put("install_error", installError);
            }
            return responseData;
        }
        
        @Override
		public void taskFinished(ObservableTask task) {
			// TODO Auto-generated method stub
		}

		@Override
		public void allFinished(FinishStatus finishStatus) {
			if (finishStatus.getType() == FinishStatus.Type.SUCCEEDED) {
				installStatus = "success";
			}
			else if ((finishStatus.getType() == FinishStatus.Type.CANCELLED)) {
				installStatus = "install-failed";
				installError = "Install cancelled by user.";
			}
			else {
				installStatus = "install-failed";
				installError = finishStatus.getException().getMessage();
			}
			semaphore.release();
		}
    }
}
