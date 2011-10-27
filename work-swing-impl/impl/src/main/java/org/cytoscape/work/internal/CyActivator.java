



package org.cytoscape.work.internal;

import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.property.bookmark.BookmarksUtil;

import org.cytoscape.work.internal.task.*;
import org.cytoscape.work.internal.tunables.*;
import org.cytoscape.work.internal.submenu.*;
import org.cytoscape.work.internal.UndoSupportImpl;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;

import org.cytoscape.work.swing.BasicGUITunableHandlerFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.util.*;
import org.cytoscape.work.swing.*;

import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		FileUtil fileUtilRef = getService(bc,FileUtil.class);
		CyProperty bookmarkServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=bookmarks)");
		BookmarksUtil bookmarksUtilServiceRef = getService(bc,BookmarksUtil.class);
		
		UndoSupportImpl undoSupport = new UndoSupportImpl();
		JDialogTunableMutator jDialogTunableMutator = new JDialogTunableMutator();
		JPanelTunableMutator jPanelTunableMutator = new JPanelTunableMutator();
		BasicSubmenuTunableHandlerFactory submenuListSingleSelectionHandlerFactory = new BasicSubmenuTunableHandlerFactory(SubmenuTunableHandlerImpl.class,ListSingleSelection.class);

		JDialogTaskManager jDialogTaskManager = new JDialogTaskManager(jDialogTunableMutator);

		SubmenuTunableMutator submenuTunableMutator = new SubmenuTunableMutator(jDialogTaskManager);

		PanelTaskManager jPanelTaskManager = new JPanelTaskManager(jPanelTunableMutator, jDialogTaskManager);
		SubmenuTaskManager submenuTaskManager = new SubmenuTaskManagerImpl(submenuTunableMutator,jDialogTaskManager);

		SupportedFileTypesManager supportedFileTypesManager = new SupportedFileTypesManager();
		BasicGUITunableHandlerFactory booleanHandlerFactory = new BasicGUITunableHandlerFactory(BooleanHandler.class, Boolean.class, boolean.class);
		BasicGUITunableHandlerFactory integerHandlerFactory = new BasicGUITunableHandlerFactory(IntegerHandler.class, Integer.class, int.class);
		BasicGUITunableHandlerFactory floatHandlerFactory = new BasicGUITunableHandlerFactory(FloatHandler.class, Float.class, float.class);
		BasicGUITunableHandlerFactory doubleHandlerFactory = new BasicGUITunableHandlerFactory(DoubleHandler.class, Double.class, double.class);
		BasicGUITunableHandlerFactory longHandlerFactory = new BasicGUITunableHandlerFactory(LongHandler.class, Long.class, long.class);
		BasicGUITunableHandlerFactory stringHandlerFactory = new BasicGUITunableHandlerFactory(StringHandler.class,String.class);
		BasicGUITunableHandlerFactory boundedIntegerHandlerFactory = new BasicGUITunableHandlerFactory(BoundedHandler.class,BoundedInteger.class);
		BasicGUITunableHandlerFactory boundedFloatHandlerFactory = new BasicGUITunableHandlerFactory(BoundedHandler.class,BoundedFloat.class);
		BasicGUITunableHandlerFactory boundedDoubleHandlerFactory = new BasicGUITunableHandlerFactory(BoundedHandler.class,BoundedDouble.class);
		BasicGUITunableHandlerFactory boundedLongHandlerFactory = new BasicGUITunableHandlerFactory(BoundedHandler.class,BoundedLong.class);
		BasicGUITunableHandlerFactory listSingleSelectionHandlerFactory = new BasicGUITunableHandlerFactory(ListSingleHandler.class,ListSingleSelection.class);
		BasicGUITunableHandlerFactory listMultipleSelectionHandlerFactory = new BasicGUITunableHandlerFactory(ListMultipleHandler.class,ListMultipleSelection.class);
		URLHandlerFactory urlHandlerFactory = new URLHandlerFactory(bookmarkServiceRef,bookmarksUtilServiceRef);
		FileHandlerFactory fileHandlerFactory = new FileHandlerFactory(fileUtilRef,supportedFileTypesManager);
		
		registerService(bc,undoSupport,UndoSupport.class, new Properties());

		registerService(bc,jDialogTaskManager,DialogTaskManager.class, new Properties());
		registerService(bc,jDialogTaskManager,TaskManager.class, new Properties());

		registerService(bc,jPanelTaskManager,PanelTaskManager.class, new Properties());

		registerService(bc,submenuTaskManager,SubmenuTaskManager.class, new Properties());

		registerService(bc,submenuListSingleSelectionHandlerFactory,SubmenuTunableHandlerFactory.class, new Properties());

		registerService(bc,integerHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,floatHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,doubleHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,longHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,booleanHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,stringHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedIntegerHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedFloatHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedDoubleHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedLongHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,listSingleSelectionHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,listMultipleSelectionHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,fileHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,urlHandlerFactory,GUITunableHandlerFactory.class, new Properties());

		registerServiceListener(bc,supportedFileTypesManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,supportedFileTypesManager,"addCyWriterTaskFactory","removeCyWriterTaskFactory",CyWriterFactory.class);

		registerServiceListener(bc,jDialogTaskManager,"addTunableRecorder","removeTunableRecorder",TunableRecorder.class);

		registerServiceListener(bc,jPanelTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",GUITunableHandlerFactory.class, TunableHandlerFactory.class);
		registerServiceListener(bc,jDialogTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",GUITunableHandlerFactory.class, TunableHandlerFactory.class);
		registerServiceListener(bc,submenuTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",SubmenuTunableHandlerFactory.class, TunableHandlerFactory.class);

	}
}

