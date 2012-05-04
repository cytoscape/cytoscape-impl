package org.cytoscape.work.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.internal.sync.SyncTaskManager;
import org.cytoscape.work.internal.sync.SyncTunableMutator;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
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

		
		SyncTunableMutator syncTunableMutator = new SyncTunableMutator();
		SyncTaskManager syncTaskManager = new SyncTaskManager(syncTunableMutator);
		
		registerAllServices(bc,syncTaskManager, new Properties());
		
		TunableRecorderManager trm = new TunableRecorderManager();

		TunableSetterImpl tsi = new TunableSetterImpl(syncTunableMutator,trm);
		registerService(bc,tsi,TunableSetter.class, new Properties());
		
		
	}
}