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

/**
 * Monitors for app state changes and notifies its listeners.
 */
public class StartupMonitor implements SynchronousBundleListener {
	/**
	 * The number of times a bundle can fail to start before we consider it
	 * permanent.
	 */
	private static final int FAILED_START_THRESHOLD = 5;
	
	/**
	 * Determines whether StartupMonitor should check whether all installed
	 * apps have started up.  Individual app state changes are monitored
	 * regardless of this setting. 
	 */
	private boolean checkAllApps;
	
	private PackageAdmin packageAdmin;
	private Map<Long, BundleData> bundleData;
	private List<AppStatusChangedListener> listeners;
	private CyEventHelper eventHelper;
	
	public StartupMonitor(BundleContext context, PackageAdmin packageAdmin, CyEventHelper eventHelper) {
		this.packageAdmin = packageAdmin;
		this.eventHelper = eventHelper;
		
		listeners = new CopyOnWriteArrayList<AppStatusChangedListener>();
		bundleData = new HashMap<Long, BundleData>();
		
		for (Bundle bundle : context.getBundles()) {
			BundleData data = new BundleData();
			data.lastState = bundle.getState();
			switch (data.lastState) {
			case Bundle.ACTIVE:
				data.lastEvent = BundleEvent.STARTED;
				break;
			case Bundle.INSTALLED:
				data.lastEvent = BundleEvent.INSTALLED;
				break;
			case Bundle.RESOLVED:
				data.lastEvent = BundleEvent.RESOLVED;
				break;
			case Bundle.STARTING:
				data.lastEvent = BundleEvent.STARTING;
				break;
			case Bundle.STOPPING:
				data.lastEvent = Bundle.STOPPING;
				break;
			}
		}
	}
	
	public void setActive(boolean isActive) {
		this.checkAllApps = isActive;
	}
	
	@Override
	public void bundleChanged(BundleEvent event) {
		long id = event.getBundle().getBundleId();
		
		BundleData data = bundleData.get(id);
		int newEvent = event.getType();
		
		if (data == null) {
			data = new BundleData();
			data.lastState = -1;
			data.lastEvent = -1;
			bundleData.put(id, data);
		}

		Bundle currentBundle = event.getBundle();
		String bundleName = (String) currentBundle.getHeaders().get("Bundle-Name");
		if (data.lastEvent == BundleEvent.STARTING && newEvent == BundleEvent.STOPPING) {
			data.failedStarts++;
			
			// Karaf uses an insane policy in which it attempts to restart a
			// bundle continuously if it fails to start.  For bundles that
			// actually work, this ends up resolving their dependencies
			// eventually if they're started in the wrong order.  For broken
			// bundles, this is an endless loop.
			if (data.failedStarts == FAILED_START_THRESHOLD) {
				// Fire this event only once per string of restart attempts.
				notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.FAILED_TO_START);
			}
		} else if (newEvent == BundleEvent.STARTED) {
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.INSTALLED);
			data.failedStarts = 0;
		} else if (data.lastState == Bundle.ACTIVE && newEvent == BundleEvent.STOPPING) {
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.DISABLED);
			data.failedStarts = 0;
		} else if (newEvent == BundleEvent.UNINSTALLED) {
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.UNINSTALLED);
			data.failedStarts = 0;
		} else if (newEvent == BundleEvent.UNRESOLVED) {
			notifyListeners(bundleName, currentBundle.getVersion().toString(), AppStatus.UNINSTALLED);
			data.failedStarts = 0;
		}
		
		data.lastState = currentBundle.getState();
		data.lastEvent = newEvent;
		
		if (!checkAllApps) {
			return;
		}
		
		BundleContext context = currentBundle.getBundleContext();
		if (context == null) {
			return;
		}
		
		int notStarted = 0;
		for (Bundle bundle : context.getBundles()) {
			boolean isFragment = packageAdmin.getBundleType(bundle) == PackageAdmin.BUNDLE_TYPE_FRAGMENT;
			if (!isFragment && bundle.getState() != Bundle.ACTIVE) {
				notStarted++;
			}
		}
		
		if (notStarted == 0) {
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
		int failedStarts; 
	}
}
