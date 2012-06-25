package org.cytoscape.app.internal.manager;

import java.net.MalformedURLException;
import java.util.List;

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
	
	private List<KarafFeature> featuresList = null;
	
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
		
		
	}

	@Override
	public void uninstall(AppManager appManager) throws AppUninstallException {
		
		// Use the default uninstallation procedure consisting of moving the app file
		// to the uninstalled apps directory
		// defaultUninstall(appManager);
		
		FeaturesService featuresService = appManager.getFeaturesService();
		
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
	}

	/**
	 * Return the list of karaf features.
	 * @return The list of karaf features
	 */
	public List<KarafFeature> getFeaturesList() {
		return featuresList;
	}
	
	public void setFeaturesList(List<KarafFeature> featuresList) {
		this.featuresList = featuresList;
	}

	

}
