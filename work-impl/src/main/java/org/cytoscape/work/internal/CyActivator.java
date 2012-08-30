package org.cytoscape.work.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.internal.sync.SyncTaskManager;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutatorFactory;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


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
		registerService(bc,tsi,TunableSetter.class, new Properties());
		
		
	}
}