

package org.cytoscape.editor.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc, VisualMappingManager.class);
		CyGroupManager cyGroupManagerServiceRef = getService(bc, CyGroupManager.class);
		CreateNetworkViewTaskFactory createNetworkViewTaskFactoryServiceRef = getService(bc, CreateNetworkViewTaskFactory.class);

		SIFInterpreterTaskFactory sifInterpreterTaskFactory = new SIFInterpreterTaskFactory();
		Properties sifInterpreterTaskFactoryProps = new Properties();
		sifInterpreterTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		sifInterpreterTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
//		sifInterpreterTaskFactoryProps.setProperty(PREFERRED_MENU, "Add");
		sifInterpreterTaskFactoryProps.setProperty(MENU_GRAVITY, "2.5");
		sifInterpreterTaskFactoryProps.setProperty(TITLE, "SIF Interpreter");
		registerService(bc, sifInterpreterTaskFactory, NetworkViewTaskFactory.class, sifInterpreterTaskFactoryProps);

		NetworkViewLocationTaskFactory networkViewLocationTaskFactory = new AddNodeTaskFactory(cyEventHelperServiceRef, visualMappingManagerServiceRef);
		Properties networkViewLocationTaskFactoryProps = new Properties();
		networkViewLocationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		networkViewLocationTaskFactoryProps.setProperty(PREFERRED_MENU, "Add");
		networkViewLocationTaskFactoryProps.setProperty(MENU_GRAVITY, "1.1");
		networkViewLocationTaskFactoryProps.setProperty(TITLE, "Add Node");
		registerService(bc, networkViewLocationTaskFactory, NetworkViewLocationTaskFactory.class, networkViewLocationTaskFactoryProps);

		// We need a place to hold the objects themselves
		ClipboardManagerImpl clipboardManager = new ClipboardManagerImpl();

		// Copy node
		NetworkViewTaskFactory copyTaskFactory = 
			new CopyTaskFactory(clipboardManager, cyNetworkManagerServiceRef);
		Properties copyTaskFactoryProps = new Properties();
		copyTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		copyTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		copyTaskFactoryProps.setProperty(PREFERRED_MENU, "Edit");
		copyTaskFactoryProps.setProperty(ACCELERATOR, "cmd c");
		copyTaskFactoryProps.setProperty(TITLE, "Copy");
		copyTaskFactoryProps.setProperty(MENU_GRAVITY, "0.0f");
		registerService(bc, copyTaskFactory, NetworkViewTaskFactory.class, copyTaskFactoryProps);

		// Cut node
		NetworkViewTaskFactory cutTaskFactory = 
			new CutTaskFactory(clipboardManager, cyNetworkManagerServiceRef);
		Properties cutTaskFactoryProps = new Properties();
		cutTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		cutTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		cutTaskFactoryProps.setProperty(PREFERRED_MENU, "Edit");
		cutTaskFactoryProps.setProperty(ACCELERATOR, "cmd x");
		cutTaskFactoryProps.setProperty(MENU_GRAVITY, "0.1f");
		cutTaskFactoryProps.setProperty(TITLE, "Cut");
		registerService(bc, cutTaskFactory, NetworkViewTaskFactory.class, cutTaskFactoryProps);

		// Paste node
		NetworkViewLocationTaskFactory pasteTaskFactory = 
			new PasteTaskFactory(clipboardManager, cyEventHelperServiceRef, visualMappingManagerServiceRef);
		Properties pasteTaskFactoryProps = new Properties();
		cutTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		pasteTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		pasteTaskFactoryProps.setProperty(PREFERRED_MENU, "Edit");
		pasteTaskFactoryProps.setProperty(TITLE, "Paste");
		pasteTaskFactoryProps.setProperty(MENU_GRAVITY, "0.2f");
		pasteTaskFactoryProps.setProperty(ACCELERATOR, "cmd v");
		pasteTaskFactoryProps.setProperty(IN_MENU_BAR, "true");
		registerService(bc, pasteTaskFactory, NetworkViewLocationTaskFactory.class, pasteTaskFactoryProps);

		// At some point, add Paste Special.  Paste special would allow paste node only, paste copy, etc.


		NodeViewTaskFactory addNestedNetworkTaskFactory = 
			new AddNestedNetworkTaskFactory(cyNetworkManagerServiceRef, visualMappingManagerServiceRef, cyGroupManagerServiceRef);
		Properties addNestedNetworkProps = new Properties();
		addNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		addNestedNetworkProps.setProperty(PREFERRED_MENU, "Nested Network");
		addNestedNetworkProps.setProperty(TITLE, "Add Nested Network");
		registerService(bc, addNestedNetworkTaskFactory, NodeViewTaskFactory.class, addNestedNetworkProps);

		NodeViewTaskFactory deleteNestedNetworkTaskFactory = 
			new DeleteNestedNetworkTaskFactory(cyNetworkManagerServiceRef, visualMappingManagerServiceRef, cyGroupManagerServiceRef);
		Properties deleteNestedNetworkProps = new Properties();
		deleteNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		deleteNestedNetworkProps.setProperty(PREFERRED_MENU, "Nested Network");
		deleteNestedNetworkProps.setProperty(TITLE, "Delete Nested Network");
		registerService(bc, deleteNestedNetworkTaskFactory, NodeViewTaskFactory.class, deleteNestedNetworkProps);
		
		NodeViewTaskFactory goToNestedNetworkTaskFactory = new GoToNestedNetworkTaskFactory(cyNetworkManagerServiceRef, cyNetworkViewManagerServiceRef, cyApplicationManagerServiceRef, createNetworkViewTaskFactoryServiceRef);
		Properties goToNestedNetworkProps = new Properties();
		goToNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		goToNestedNetworkProps.setProperty(PREFERRED_MENU, "Nested Network");
		goToNestedNetworkProps.setProperty(TITLE, "Go to Nested Network");
		registerService(bc, goToNestedNetworkTaskFactory, NodeViewTaskFactory.class, goToNestedNetworkProps);
	}
}
