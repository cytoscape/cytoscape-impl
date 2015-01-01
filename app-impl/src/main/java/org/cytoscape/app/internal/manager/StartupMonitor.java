package org.cytoscape.app.internal.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.internal.event.AppStatusChangedListener;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.event.CyEventHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

/**
 * Monitors for app state changes and notifies its listeners.
 */
public class StartupMonitor implements SynchronousBundleListener {
	
	private static final int APP_START_LEVEL = 200;
	
	/**
	 * Determines whether StartupMonitor should check whether all installed
	 * apps have started up.  Individual app state changes are monitored
	 * regardless of this setting. 
	 */
	private boolean checkAllApps;
	
	private BundleContext context;
	private PackageAdmin packageAdmin;
	private Map<Long, BundleData> bundleData;
	private List<AppStatusChangedListener> listeners;
	private CyEventHelper eventHelper;
	private StartLevel startLevel;
	
	public StartupMonitor(BundleContext context, PackageAdmin packageAdmin, CyEventHelper eventHelper, StartLevel startLevel) {
		
		this.context = context;
		this.packageAdmin = packageAdmin;
		this.eventHelper = eventHelper;
		this.startLevel = startLevel;
		
		listeners = new CopyOnWriteArrayList<AppStatusChangedListener>();
		bundleData = new HashMap<Long, BundleData>();
	}
	
	public void setActive(boolean isActive) {
		this.checkAllApps = isActive;
		if(isActive) {
			checkAllApps();
		}
	}
	
	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle currentBundle = event.getBundle();
		String bundleName = (String) currentBundle.getHeaders().get("Bundle-Name");
		
		//skip bundles that are uninstalled or aren't apps
		if(currentBundle.getState() == Bundle.UNINSTALLED || 
				startLevel.getBundleStartLevel(currentBundle) != APP_START_LEVEL) return;
		
		long id = currentBundle.getBundleId();
		BundleData data = bundleData.get(id);
		int newEvent = event.getType();
		
		if (data == null) {
			data = new BundleData();
			data.lastState = -1;
			data.lastEvent = -1;
			bundleData.put(id, data);
		}

		if (data.lastEvent == BundleEvent.STARTING && newEvent == BundleEvent.STOPPING) {
			data.failedToStart = true;
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.FAILED_TO_START);
		} else if (newEvent == BundleEvent.STARTED) {
			data.failedToStart = false;
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.INSTALLED);
		} else if (data.lastState == Bundle.ACTIVE && newEvent == BundleEvent.STOPPING) {
			data.failedToStart = false;
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.DISABLED);
		} else if (newEvent == BundleEvent.UNINSTALLED || newEvent == BundleEvent.UNRESOLVED) {
			data.failedToStart = false;
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.UNINSTALLED);
		}
		
		data.lastState = currentBundle.getState();
		data.lastEvent = newEvent;
		
		if (checkAllApps) {
			checkAllApps();
		}
	}
	
	private void checkAllApps() {
		int notFinished = 0;
		for (Bundle bundle : context.getBundles()) {
			//skip bundles that are uninstalled or aren't apps
			if(bundle.getState() == Bundle.UNINSTALLED || 
					startLevel.getBundleStartLevel(bundle) != APP_START_LEVEL) continue;
			
			BundleData data = bundleData.get(bundle.getBundleId());

			boolean isFragment = packageAdmin.getBundleType(bundle) == PackageAdmin.BUNDLE_TYPE_FRAGMENT;
			if (!isFragment && bundle.getState() != Bundle.ACTIVE && (data == null || !data.failedToStart)) {
				notFinished++;
			}
		}
		
		if (notFinished == 0) {
			eventHelper.fireEvent(new AppsFinishedStartingEvent(this));
			checkAllApps = false;
		}
	}
	
	public void addAppStatusChangedListener(AppStatusChangedListener listener) {
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}
	
	public void removeAppStatusChangedListener(AppStatusChangedListener listener) {
		listeners.remove(listener);
	}
	
	void notifyListeners(String symbolicName, String version, AppStatus status) {
		for (AppStatusChangedListener listener : listeners) {
			listener.handleAppStatusChanged(symbolicName, version, status);
		}
	}

	static class BundleData {
		Integer lastState;
		Integer lastEvent;
		boolean failedToStart;
	}
}
