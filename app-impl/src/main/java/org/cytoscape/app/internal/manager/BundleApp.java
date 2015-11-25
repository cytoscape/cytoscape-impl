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

import org.cytoscape.app.internal.exception.AppLoadingException;
import org.cytoscape.app.internal.exception.AppStartupException;
import org.cytoscape.app.internal.exception.AppStoppingException;
import org.cytoscape.app.internal.exception.AppUnloadingException;
import org.cytoscape.app.internal.util.AppHelper;
import org.cytoscape.application.CyVersion;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class BundleApp extends App {
	
	private Bundle bundleInstance;
	
	@Override
	public String getReadableStatus() {
		switch (this.getStatus()) {
		
		case INACTIVE:
			if (bundleInstance != null) {
				return "Inactive on Restart";
			} else {
				return "Inactive";
			}
		case DISABLED:
			if (bundleInstance != null) {
				return "Disable on Restart";
			} else {
				return "Disabled";
			}
		case UNINSTALLED:
			if (bundleInstance != null) {
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
		if(bundleInstance == null) try {
			bundleInstance = appManager.getBundleContext().installBundle(getAppFile().toURI().toString());;
		} catch (BundleException e) {
			throw new AppLoadingException("Bundle install error", e);
		}
	}
	
	@Override
	public void start(AppManager appManager) throws AppStartupException {
		if(bundleInstance != null) try {
			bundleInstance.start();
		} catch (BundleException e) {
			throw new AppStartupException("Bundle start error", e);
		}
	}
	
	@Override
	public void stop(AppManager appManager) throws AppStoppingException {
		if(bundleInstance != null) try {
			bundleInstance.stop();
		}
		catch (BundleException e) {
			throw new AppStoppingException("Bundle stop error", e);
		}
	}

	@Override
	public void unload(AppManager appManager) throws AppUnloadingException {
		if(bundleInstance != null) try {
			bundleInstance.uninstall();
			bundleInstance = null;
		}
		catch (BundleException e) {
			throw new AppUnloadingException("Bundle uninstall error", e);
		}
	}
	
	@Override
	public boolean isCompatible(CyVersion cyVer) {
		try {
			return ParseAppDependencies.checkVersions(getCompatibleVersions(), "org.cytoscape", cyVer.getVersion());
		} catch (Exception e) {
			return false;
		}
	}
}
