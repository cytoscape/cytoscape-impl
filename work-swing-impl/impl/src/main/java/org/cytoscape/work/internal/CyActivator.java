



package org.cytoscape.work.internal;

import java.util.Properties;

import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.internal.submenu.SubmenuTaskManagerImpl;
import org.cytoscape.work.internal.submenu.SubmenuTunableHandlerImpl;
import org.cytoscape.work.internal.submenu.SubmenuTunableMutator;
import org.cytoscape.work.internal.sync.SyncTaskManager;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutator;
import org.cytoscape.work.internal.task.JDialogTaskManager;
import org.cytoscape.work.internal.task.JPanelTaskManager;
import org.cytoscape.work.internal.tunables.BooleanHandler;
import org.cytoscape.work.internal.tunables.BoundedHandler;
import org.cytoscape.work.internal.tunables.DoubleHandler;
import org.cytoscape.work.internal.tunables.FileHandlerFactory;
import org.cytoscape.work.internal.tunables.FloatHandler;
import org.cytoscape.work.internal.tunables.IntegerHandler;
import org.cytoscape.work.internal.tunables.JDialogTunableMutator;
import org.cytoscape.work.internal.tunables.JPanelTunableMutator;
import org.cytoscape.work.internal.tunables.ListMultipleHandler;
import org.cytoscape.work.internal.tunables.ListSingleHandler;
import org.cytoscape.work.internal.tunables.LongHandler;
import org.cytoscape.work.internal.tunables.StringHandler;
import org.cytoscape.work.internal.tunables.URLHandlerFactory;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.SimpleGUITunableHandlerFactory;
import org.cytoscape.work.swing.SimpleSubmenuTunableHandlerFactory;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.swing.SubmenuTunableHandlerFactory;
import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		
		DataSourceManager dsManager = getService(bc, DataSourceManager.class);

		FileUtil fileUtilRef = getService(bc,FileUtil.class);
		CyProperty bookmarkServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=bookmarks)");
		BookmarksUtil bookmarksUtilServiceRef = getService(bc,BookmarksUtil.class);
		
		UndoSupportImpl undoSupport = new UndoSupportImpl();
		JDialogTunableMutator jDialogTunableMutator = new JDialogTunableMutator();
		JPanelTunableMutator jPanelTunableMutator = new JPanelTunableMutator();
		SimpleSubmenuTunableHandlerFactory submenuListSingleSelectionHandlerFactory = new SimpleSubmenuTunableHandlerFactory(SubmenuTunableHandlerImpl.class,ListSingleSelection.class);

		JDialogTaskManager jDialogTaskManager = new JDialogTaskManager(jDialogTunableMutator);

		SubmenuTunableMutator submenuTunableMutator = new SubmenuTunableMutator(jDialogTaskManager);

		PanelTaskManager jPanelTaskManager = new JPanelTaskManager(jPanelTunableMutator, jDialogTaskManager);
		SubmenuTaskManager submenuTaskManager = new SubmenuTaskManagerImpl(submenuTunableMutator,jDialogTaskManager);

		SupportedFileTypesManager supportedFileTypesManager = new SupportedFileTypesManager();
		SimpleGUITunableHandlerFactory booleanHandlerFactory = new SimpleGUITunableHandlerFactory(BooleanHandler.class, Boolean.class, boolean.class);
		SimpleGUITunableHandlerFactory integerHandlerFactory = new SimpleGUITunableHandlerFactory(IntegerHandler.class, Integer.class, int.class);
		SimpleGUITunableHandlerFactory floatHandlerFactory = new SimpleGUITunableHandlerFactory(FloatHandler.class, Float.class, float.class);
		SimpleGUITunableHandlerFactory doubleHandlerFactory = new SimpleGUITunableHandlerFactory(DoubleHandler.class, Double.class, double.class);
		SimpleGUITunableHandlerFactory longHandlerFactory = new SimpleGUITunableHandlerFactory(LongHandler.class, Long.class, long.class);
		SimpleGUITunableHandlerFactory stringHandlerFactory = new SimpleGUITunableHandlerFactory(StringHandler.class,String.class);
		SimpleGUITunableHandlerFactory boundedIntegerHandlerFactory = new SimpleGUITunableHandlerFactory(BoundedHandler.class,BoundedInteger.class);
		SimpleGUITunableHandlerFactory boundedFloatHandlerFactory = new SimpleGUITunableHandlerFactory(BoundedHandler.class,BoundedFloat.class);
		SimpleGUITunableHandlerFactory boundedDoubleHandlerFactory = new SimpleGUITunableHandlerFactory(BoundedHandler.class,BoundedDouble.class);
		SimpleGUITunableHandlerFactory boundedLongHandlerFactory = new SimpleGUITunableHandlerFactory(BoundedHandler.class,BoundedLong.class);
		SimpleGUITunableHandlerFactory listSingleSelectionHandlerFactory = new SimpleGUITunableHandlerFactory(ListSingleHandler.class,ListSingleSelection.class);
		SimpleGUITunableHandlerFactory listMultipleSelectionHandlerFactory = new SimpleGUITunableHandlerFactory(ListMultipleHandler.class,ListMultipleSelection.class);
		
		URLHandlerFactory urlHandlerFactory = new URLHandlerFactory(dsManager);
		
		FileHandlerFactory fileHandlerFactory = new FileHandlerFactory(fileUtilRef,supportedFileTypesManager);

		SyncTunableMutator syncTunableMutator = new SyncTunableMutator();
		SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();
		SyncTaskManager syncTaskManager = new SyncTaskManager(syncTunableMutator);
	
		Properties undoSupportProps = new Properties();
		registerService(bc,undoSupport,UndoSupport.class, undoSupportProps);
		registerService(bc,undoSupport,SwingUndoSupport.class, undoSupportProps);

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
		registerService(bc,syncTaskManager,SynchronousTaskManager.class, new Properties());
		registerService(bc,syncTunableHandlerFactory,TunableHandlerFactory.class, new Properties());

		registerServiceListener(bc,supportedFileTypesManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,supportedFileTypesManager,"addCyWriterTaskFactory","removeCyWriterTaskFactory",CyWriterFactory.class);

		registerServiceListener(bc,jDialogTaskManager,"addTunableRecorder","removeTunableRecorder",TunableRecorder.class);
		registerServiceListener(bc,syncTaskManager,"addTunableRecorder","removeTunableRecorder",TunableRecorder.class);

		registerServiceListener(bc,jPanelTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",GUITunableHandlerFactory.class, TunableHandlerFactory.class);
		registerServiceListener(bc,jDialogTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",GUITunableHandlerFactory.class, TunableHandlerFactory.class);
		registerServiceListener(bc,submenuTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",SubmenuTunableHandlerFactory.class, TunableHandlerFactory.class);
		registerServiceListener(bc,syncTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",SyncTunableHandlerFactory.class, TunableHandlerFactory.class);

	}
}

