package org.cytoscape.application.internal;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class ShutdownHandler implements CyShutdown {

	private static final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);
	
	private final Bundle rootBundle;
	private final CyServiceRegistrar serviceRegistrar;

	public ShutdownHandler(final Bundle rootBundle, final CyServiceRegistrar serviceRegistrar) {
		this.rootBundle = rootBundle;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void exit(int retVal) {
		exit(retVal, false);
	}

	@Override
	public void exit(int retVal, boolean force) {
		CyShutdownEvent ev = new CyShutdownEvent(ShutdownHandler.this, force);
		serviceRegistrar.getService(CyEventHelper.class).fireEvent(ev);

		if (ev.actuallyShutdown()) {
			try {
				rootBundle.stop();
			} catch (BundleException e) {
				logger.error("Error while shutting down", e);
			}
		} else {
			logger.info("NOT shutting down, per listener instruction: " + ev.abortShutdownReason());
		}
	}
}
