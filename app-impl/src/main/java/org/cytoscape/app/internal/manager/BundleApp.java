package org.cytoscape.app.internal.manager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppInstanceException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class BundleApp extends App {

	/**
	 * Karaf feature information used to install/uninstall a bundle app, 
	 * which consists of Karaf features.
	 */
	public static class KarafFeature {
		public String featureName;
		public String featureVersion;
	}
	
	private Map<String, KarafFeature> featuresSet;
	
	public BundleApp() {
		super();
		
		this.featuresSet = new HashMap<String, KarafFeature>();
	}
	
	@Override
	public Object createAppInstance(CySwingAppAdapter appAdapter)
			throws AppInstanceException {
		
		BundleContext bundleContext = null;
		Bundle bundle = null;
		try {
			bundle = bundleContext.installBundle(this.getAppTemporaryInstallFile().toURI().toURL().toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bundle;
	}

	@Override
	public void install(AppManager appManager) throws AppInstallException {
		// Use the default installation procedure consisting of copying over
		// the file, creating an instance, and registering with the app manager.
		// defaultInstall(appManager);
		
		// Copy to Karaf deploy directory
		String karafDeployDirectory = appManager.getKarafDeployDirectory();
		File appFile = this.getAppFile();
		
		try {
			if (appFile != null 
					&& !appFile.getParentFile().getCanonicalPath().equals(appManager.getInstalledAppsPath())) {
				
				FileUtils.copyFileToDirectory(appFile, new File(appManager.getInstalledAppsPath()));
				
				if (appFile.getParentFile().getCanonicalPath().equalsIgnoreCase(
						appManager.getUninstalledAppsPath())) {
					appFile.delete();
				}
				
				this.setAppFile(new File(appManager.getInstalledAppsPath() + File.separator + appFile.getName()));
			}
		} catch (IOException e) {
			throw new AppInstallException("Failed to copy bundle app to local storage directory for installed apps");
		}
		
		try {
			FileUtils.copyFileToDirectory(this.getAppFile(), new File(karafDeployDirectory));
			this.setAppTemporaryInstallFile(new File(karafDeployDirectory + File.separator + this.getAppFile().getName()));
		} catch (IOException e) {
			throw new AppInstallException("Failed to copy bundle app to Karaf deploy directory");
		}
		
		// Check if the features were installed
		FeaturesService featuresService = appManager.getFeaturesService();
		List<Feature> installedFeatures = getCorrespondingFeatures(featuresService); 
		
		//System.out.println("features from app: " + featuresSet.size());
		//System.out.println("available features: " + featuresService.listFeatures().length);
		
		/*
		if (installedFeatures.size() == featuresSet.size()) {
			
		} else {
			this.getAppTemporaryInstallFile().delete();
			throw new AppInstallException("Not all Karaf features were successfully installed from the bundle app.");
		}
		*/
		
		if (!appManager.getApps().contains(this)) {
			appManager.getApps().add(this);
		}
		
		this.setStatus(AppStatus.INSTALLED);
	}

	private List<Feature> getCorrespondingFeatures(FeaturesService featuresService) {
		List<Feature> correspondingFeatures = new LinkedList<Feature>();
		
		Feature[] availableFeatures = new Feature[]{};
		try {
			availableFeatures = featuresService.listFeatures();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < availableFeatures.length; i++) {
			Feature availableFeature = availableFeatures[i];
			
			BundleApp.KarafFeature appFeature = featuresSet.get(availableFeature.getName());
			
			// System.out.println("available feature: " + availableFeature.getName() + ", " + availableFeature.getVersion());
			
			if (appFeature != null
					&& appFeature.featureVersion.equalsIgnoreCase(availableFeature.getVersion())) {
				correspondingFeatures.add(availableFeature);
				
				// System.out.println("feature match: " + availableFeature.getName() + " vs " + appFeature.featureName);
			}
		}
		
		return correspondingFeatures;
	}
	
	@Override
	public void uninstall(AppManager appManager) throws AppUninstallException {
		
		// Use the default uninstallation procedure consisting of moving the app file
		// to the uninstalled apps directory
		// defaultUninstall(appManager);
		
		FeaturesService featuresService = appManager.getFeaturesService();
		List<Feature> installedFeatures = getCorrespondingFeatures(featuresService);
		
		this.getAppTemporaryInstallFile().delete();
		
		try {
			FileUtils.moveFileToDirectory(getAppFile(), new File(appManager.getUninstalledAppsPath()), false);
			this.setAppFile(new File(appManager.getUninstalledAppsPath() + File.separator + this.getAppFile().getName()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw new AppUninstallException("Unable to move app file to uninstalled apps directory: " + e.getMessage());
		}
		
		this.setStatus(AppStatus.UNINSTALLED);
		
		/*
		for (Feature installedFeature : installedFeatures) {
			featuresService.uninstallFeature(arg0)
		}
		*/
		
		/*
		try {
			Feature[] availableFeatures = featuresService.listFeatures();
			List<BundleApp.KarafFeature> appFeatures = this.getFeaturesList();
			
			Feature availableFeature;
			for (int i = 0; i < availableFeatures.length; i++) {
				availableFeature = availableFeatures[i];
				
				for (BundleApp.KarafFeature appFeature : appFeatures) {
					if (appFeature.featureName.equalsIgnoreCase(availableFeature.getName())
							&& appFeature.featureVersion.equalsIgnoreCase(availableFeature.getVersion())) {
						
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		*/
	}

	/**
	 * Return the list of karaf features.
	 * @return The list of karaf features
	 */
	public Map<String, KarafFeature> getFeaturesList() {
		return featuresSet;
	}
	
	public void setFeaturesList(Map<String, KarafFeature> featuresSet) {
		this.featuresSet = featuresSet;
	}
}
