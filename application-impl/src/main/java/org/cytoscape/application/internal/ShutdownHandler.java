package org.cytoscape.application.internal;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.event.CyEventHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ShutdownHandler implements CyShutdown {

	private static final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);
	
	private final CyEventHelper eh;

	private Bundle rootBundle;

	public ShutdownHandler(CyEventHelper eh, Bundle rootBundle) {
		this.eh = eh;
		this.rootBundle = rootBundle;
	}

	public void exit(int retVal) {
		CyShutdownEvent ev =  new CyShutdownEvent(ShutdownHandler.this);
		eh.fireEvent( ev );

		if ( ev.actuallyShutdown() )
			try {
				rootBundle.stop();
			} catch (BundleException e) {
				logger.error("Error while shutting down", e);
			}
		else
			logger.info("NOT shutting down, per listener instruction: " + ev.abortShutdownReason() );
	}
}
