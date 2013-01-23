package org.cytoscape.work.internal;

/*
 * #%L
 * org.cytoscape.work-headless-impl
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
import org.cytoscape.work.internal.task.*;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
	
		UndoSupportImpl undoSupport = new UndoSupportImpl();
		
		DialogTaskManager jDialogTaskManager = new HDialogTaskManager();
		
		Properties undoSupportProps = new Properties();
		registerService(bc,undoSupport,UndoSupport.class, undoSupportProps);
		registerService(bc,undoSupport,SwingUndoSupport.class, undoSupportProps);
		
		registerService(bc,jDialogTaskManager,DialogTaskManager.class, new Properties());

		
		/*SyncTunableMutator syncTunableMutator = new SyncTunableMutator();
		SyncTaskManager syncTaskManager = new SyncTaskManager(syncTunableMutator);
		
		registerAllServices(bc,syncTaskManager, new Properties());
		
		TunableRecorderManager trm = new TunableRecorderManager();

		TunableSetterImpl tsi = new TunableSetterImpl(syncTunableMutator,trm);
		registerService(bc,tsi,TunableSetter.class, new Properties());
		
		SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();
		Properties syncFactoryProp = new Properties();
		registerService(bc,syncTunableHandlerFactory, TunableHandlerFactory.class, syncFactoryProp);
		syncTunableMutator.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);*/
		
	}
}