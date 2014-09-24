package org.cytoscape.group.internal;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
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

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		
		CyGroupManagerImpl cyGroupManager = new CyGroupManagerImpl(cyEventHelperServiceRef);
		CyGroupFactoryImpl cyGroupFactory = new CyGroupFactoryImpl(cyEventHelperServiceRef, 
		                                                           cyGroupManager, 
		                                                           cyServiceRegistrarServiceRef);
		registerService(bc,cyGroupManager,CyGroupManager.class, new Properties());
		registerService(bc,cyGroupFactory,CyGroupFactory.class, new Properties());

/*
		// Move this to a separate module
		SessionEventsListener sessListener = new SessionEventsListener(cyGroupFactory,
				                                                       cyGroupManager,
				                                                       cyNetworkManagerServiceRef,
				                                                       cyRootNetworkManagerServiceRef);
		
		registerService(bc, sessListener, SessionLoadedListener.class, new Properties());
		registerService(bc, sessListener, SessionAboutToBeSavedListener.class, new Properties());
*/
	}
}

