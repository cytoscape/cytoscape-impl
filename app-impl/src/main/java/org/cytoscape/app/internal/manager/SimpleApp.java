package org.cytoscape.app.internal.manager;

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
import org.cytoscape.app.internal.exception.AppDisableException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppInstanceException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.swing.CySwingAppAdapter;

public class SimpleApp extends App {

	@Override
	public String getReadableStatus() {
		switch (this.getStatus()) {
		
		case DISABLED:
			if (this.getAppInstance() != null) {
				return "Disable on Restart";
			} else {
				return "Disabled";
			}
		case UNINSTALLED:
			if (this.getAppInstance() != null) {
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
	public Object createAppInstance(CySwingAppAdapter appAdapter)
			throws AppInstanceException {
		File installFile = this.getAppTemporaryInstallFile();
		if (installFile == null) {
			throw new AppInstanceException("No copy of app jar for instancing was found");
		}
		
		URL appURL = null;
		try {
			appURL = installFile.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new AppInstanceException("Unable to obtain URL for file: " 
					+ installFile + ". Reason: " + e.getMessage());
		}
		
		// TODO: Currently uses the CyAppAdapter's loader to load apps' classes. Is there reason to use a different one?
		ClassLoader appClassLoader = new URLClassLoader(
				new URL[]{appURL}, appAdapter.getClass().getClassLoader());
		
		// Attempt to load the class
		Class<?> appEntryClass = null;
		try {
			 appEntryClass = appClassLoader.loadClass(this.getEntryClassName());
		} catch (ClassNotFoundException e) {
			
			throw new AppInstanceException("Class " + this.getEntryClassName() + " not found in URL: " + appURL);
		}
		
		// Attempt to obtain the constructor
		Constructor<?> constructor = null;
		try {
			try {
				constructor = appEntryClass.getConstructor(CyAppAdapter.class);
			} catch (SecurityException e) {
				throw new AppInstanceException("Access to the constructor for " + appEntryClass + " denied.");
			} catch (NoSuchMethodException e) {
				throw new AppInstanceException("Unable to find a constructor for " + appEntryClass 
						+ " that takes a CyAppAdapter as its argument.");
			}
		} catch (AppInstanceException e) {
			try {
				constructor = appEntryClass.getConstructor(CySwingAppAdapter.class);
			} catch (SecurityException e2) {
				throw new AppInstanceException("Access to the constructor for " + appEntryClass 
						+ " taking a CySwingAppAdapter as its argument denied.");
			} catch (NoSuchMethodException e2) {
				throw new AppInstanceException("Unable to find an accessible constructor that takes either" 
						+ " a CyAppAdapter or a CySwingAppAdapter as its argument.");
			}
		}
		
		// Attempt to instantiate the app's class that extends AbstractCyApp or AbstractCySwingApp.
		Object appInstance = null;
		try {
			appInstance = constructor.newInstance(appAdapter);
		} catch (IllegalArgumentException e) {
			throw new AppInstanceException("Illegal arguments passed to the constructor for the app's entry class: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new AppInstanceException("Error instantiating the class " + appEntryClass + ": " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new AppInstanceException("Access to constructor denied: " + e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new AppInstanceException("App constructor threw exception: " + e.toString());
		}
		
		return appInstance;
	}

	@Override
	public void install(AppManager appManager) throws AppInstallException {
		if (this.getStatus() == AppStatus.INSTALLED) {
			return;
		}
		
		/*
		try {
			moveAppFile(appManager, new File(appManager.getKarafDeployDirectory()));
		} catch (IOException e) {
			throw new AppInstallException("Failed to move app file, " + e.getMessage());
		}
		*/
		
		// Make a copy used to create app instance
		LinkedList<String> uniqueNameDirectory = new LinkedList<String>();
		uniqueNameDirectory.add(appManager.getTemporaryInstallPath());
		
		try {
			if (this.getAppTemporaryInstallFile() == null) {
				File targetFile = new File(appManager.getTemporaryInstallPath() + File.separator 
						+ suggestFileName(uniqueNameDirectory, this.getAppFile().getName()));
				FileUtils.copyFile(this.getAppFile(), targetFile);
				this.setAppTemporaryInstallFile(targetFile);
			}
		} catch (IOException e) {
			throw new AppInstallException("Unable to make copy of app jar for instancing, " + e.getMessage());
		}
		
		try {
			// Only create new instance if none created
			if (this.getAppInstance() == null) {
				Object appInstance = this.createAppInstance(appManager.getSwingAppAdapter());
				this.setAppInstance((AbstractCyApp) appInstance);
			}
		} catch (AppInstanceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.setStatus(AppStatus.INSTALLED);
	}

	@Override
	public void uninstall(AppManager appManager) throws AppUninstallException {
		this.setStatus(AppStatus.UNINSTALLED);
	}

	@Override
	public void disable(AppManager appManager) throws AppDisableException {
		this.setStatus(AppStatus.DISABLED);
	}

}
