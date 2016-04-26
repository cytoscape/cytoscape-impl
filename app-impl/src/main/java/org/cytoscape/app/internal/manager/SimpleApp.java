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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.internal.exception.AppLoadingException;
import org.cytoscape.app.internal.exception.AppStartupException;
import org.cytoscape.app.internal.exception.AppStoppingException;
import org.cytoscape.app.internal.exception.AppUnloadingException;
import org.cytoscape.app.internal.util.AppHelper;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyVersion;

public class SimpleApp extends App {
	
	/**
	 * The temporary file corresponding to the app that is used to load classes from.
	 */
	private File appTemporaryInstallFile;
	
	/**
	 * A reference to the app's constructor.
	 */
	private Constructor<?> appConstructor;
	
	/**
	 * The app's entry class. 
	 */
	private Class<?> appEntryClass;
	
	/**
	 * A reference to the instance of the app's class that extends {@link AbstractCyApp}.
	 */
	private AbstractCyApp app;
	
	@Override
	public boolean isHidden() {
		return(super.isHidden() && app == null);
	}

	@Override
	public String getReadableStatus() {
		switch (this.getStatus()) {
		
		case INACTIVE:
			if (app != null) {
				return "Inactive on Restart";
			} else {
				return "Inactive";
			}
		case DISABLED:
			if (app != null) {
				return "Disable on Restart";
			} else {
				return "Disabled";
			}
		case UNINSTALLED:
			if (app != null) {
				return "Uninstall on Restart";
			} else {
				return "Uninstalled";
			}
		case FILE_MOVED:
			return "File Moved (Needs restart to uninstall)";
		default:
			return super.getReadableStatus();
		
		}
	}


	@Override
	public void load(AppManager appManager) throws AppLoadingException {
		if (appConstructor != null) return;
			// Make a copy used to create app instance
			LinkedList<String> uniqueNameDirectory = new LinkedList<>();
			uniqueNameDirectory.add(appManager.getTemporaryInstallPath());
			
			try {
				if (appTemporaryInstallFile == null) {
					File targetFile = new File(appManager.getTemporaryInstallPath() + File.separator 
							+ suggestFileName(uniqueNameDirectory, this.getAppFile().getName()));
					FileUtils.copyFile(this.getAppFile(), targetFile);
					appTemporaryInstallFile = targetFile;
				}
			} catch (IOException e) {
				throw new AppLoadingException("Unable to make copy of app jar for instancing", e);
			}
			
			File installFile = appTemporaryInstallFile;
			if (installFile == null) {
				throw new AppLoadingException("No copy of app jar for instancing was found");
			}
			
			URL appURL = null;
			try {
				appURL = installFile.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new AppLoadingException("Unable to obtain URL for file: " 
						+ installFile, e );
			}
			
			// TODO: Currently uses the CyAppAdapter's loader to load apps' classes. Is there reason to use a different one?
			ClassLoader appClassLoader = new URLClassLoader(
					new URL[]{appURL}, appManager.getSwingAppAdapter().getClass().getClassLoader());
			
			// Attempt to load the class
			try {
				 appEntryClass = appClassLoader.loadClass(this.getEntryClassName());
			} catch (ClassNotFoundException e) {
				throw new AppLoadingException("Class " + this.getEntryClassName() + " not found in URL: " + appURL, e);
			}
			
			// Attempt to obtain the constructor
			try {
				try {
					appConstructor = appEntryClass.getConstructor(CyAppAdapter.class);
				} catch (SecurityException e) {
					throw new AppLoadingException("Access to the constructor for " + appEntryClass + " denied.", e);
				} catch (NoSuchMethodException e) {
					throw new AppLoadingException("Unable to find a constructor for " + appEntryClass 
							+ " that takes a CyAppAdapter as its argument.", e);
				}
			} catch (AppLoadingException e) {
				try {
					appConstructor = appEntryClass.getConstructor(CySwingAppAdapter.class);
				} catch (SecurityException e2) {
					throw new AppLoadingException("Access to the constructor for " + appEntryClass 
							+ " taking a CySwingAppAdapter as its argument denied.", e2);
				} catch (NoSuchMethodException e2) {
					throw new AppLoadingException("Unable to find an accessible constructor that takes either" 
							+ " a CyAppAdapter or a CySwingAppAdapter as its argument.", e2);
				}
			}
	}
	
	@Override
	public void start(AppManager appManager) throws AppStartupException {
		// Only create new instance if none created
		if (app != null) return;
		try {
			app = (AbstractCyApp) appConstructor.newInstance(appManager.getSwingAppAdapter());
		} 
		catch (IllegalArgumentException e) {
			throw new AppStartupException("Illegal arguments passed to the constructor for the app's entry class", e);
		} 
		catch (InstantiationException e) {
			throw new AppStartupException("Error instantiating the class " + appEntryClass, e);
		} 
		catch (IllegalAccessException e) {
			throw new AppStartupException("Access to constructor denied", e);
		} 
		catch (InvocationTargetException e) {
			throw new AppStartupException("App constructor threw exception", e);
		}
	}

	@Override
	public void unload(AppManager appManager) throws AppUnloadingException {
	}

	@Override
	public void stop(AppManager appManager) throws AppStoppingException {
	}
	
	@Override
	public boolean isCompatible(CyVersion cyVer) {
		return AppHelper.isCompatible(cyVer, getCompatibleVersions());
	}

}
