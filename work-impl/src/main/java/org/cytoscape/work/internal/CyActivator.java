package org.cytoscape.work.internal;

/*
 * #%L
 * org.cytoscape.work-impl
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

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.internal.properties.BasicTypePropertyHandler;
import org.cytoscape.work.internal.properties.BoundedPropertyHandler;
import org.cytoscape.work.internal.properties.ListMultiplePropertyHandler;
import org.cytoscape.work.internal.properties.ListSinglePropertyHandler;
import org.cytoscape.work.internal.properties.SimpleTunablePropertyHandlerFactory;
import org.cytoscape.work.internal.properties.TunablePropertySerializerFactoryImpl;
import org.cytoscape.work.internal.sync.SyncTaskManager;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutatorFactory;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.cytoscape.work.properties.TunablePropertyHandlerFactory;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	public void start(BundleContext bc) {
		
		SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();
		Properties syncFactoryProp = new Properties();
		registerService(bc,syncTunableHandlerFactory, TunableHandlerFactory.class, syncFactoryProp);
		
		SyncTunableMutatorFactory mutatorFactory = new SyncTunableMutatorFactory(syncTunableHandlerFactory);
		
		SyncTaskManager syncTaskManager = new SyncTaskManager(mutatorFactory.createMutator());
		registerService(bc,syncTaskManager,SynchronousTaskManager.class, syncFactoryProp);
		registerServiceListener(bc,syncTaskManager,"addTunableRecorder","removeTunableRecorder",TunableRecorder.class);
		
		TunableRecorderManager trm = new TunableRecorderManager();
		registerServiceListener(bc,trm,"addTunableRecorder","removeTunableRecorder",TunableRecorder.class);

		TunableSetterImpl tsi = new TunableSetterImpl(mutatorFactory,trm);
		registerService(bc,tsi,TunableSetter.class);		
		
		TunablePropertySerializerFactoryImpl tpsf = new TunablePropertySerializerFactoryImpl();
		registerService(bc, tpsf, TunablePropertySerializerFactory.class);
		registerServiceListener(bc, tpsf, "addTunableHandlerFactory", "removeTunableHandlerFactory", TunablePropertyHandlerFactory.class);
		
		TunablePropertyHandlerFactory<BasicTypePropertyHandler> simpleHandler = new SimpleTunablePropertyHandlerFactory<>(BasicTypePropertyHandler.class, BasicTypePropertyHandler.supportedTypes());
		TunablePropertyHandlerFactory<ListSinglePropertyHandler> listSingleHandler = new SimpleTunablePropertyHandlerFactory<>(ListSinglePropertyHandler.class, ListSingleSelection.class);
		TunablePropertyHandlerFactory<ListMultiplePropertyHandler> listMultipleHandler = new SimpleTunablePropertyHandlerFactory<>(ListMultiplePropertyHandler.class, ListMultipleSelection.class);
		TunablePropertyHandlerFactory<BoundedPropertyHandler> boundedHandler = new SimpleTunablePropertyHandlerFactory<>(BoundedPropertyHandler.class, BoundedPropertyHandler.supportedTypes());
		
		registerService(bc, simpleHandler,       TunablePropertyHandlerFactory.class);
		registerService(bc, listMultipleHandler, TunablePropertyHandlerFactory.class);
		registerService(bc, listSingleHandler,   TunablePropertyHandlerFactory.class);
		registerService(bc, boundedHandler,      TunablePropertyHandlerFactory.class);
	}
}