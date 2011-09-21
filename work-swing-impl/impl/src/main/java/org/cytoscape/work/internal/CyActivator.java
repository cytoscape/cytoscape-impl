



package org.cytoscape.work.internal;

import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.property.bookmark.BookmarksUtil;

import org.cytoscape.work.internal.task.SwingTaskManager;
import org.cytoscape.work.internal.tunables.*;
import org.cytoscape.work.internal.UndoSupportImpl;
import org.cytoscape.work.internal.tunables.GUITunableInterceptorImpl;
import org.cytoscape.work.internal.tunables.FileHandlerFactory;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.BasicGUITunableHandlerFactory;

import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.TunableInterceptor;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.work.swing.GUITunableInterceptor;
import org.cytoscape.work.util.*;

import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.work.TunableInterceptor;
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
		GUITunableInterceptorImpl guiTunableInterceptor = new GUITunableInterceptorImpl();
		SwingTaskManager swingTaskManager = new SwingTaskManager(guiTunableInterceptor);
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
		registerService(bc,guiTunableInterceptor,GUITunableInterceptor.class, new Properties());
		registerService(bc,guiTunableInterceptor,TunableInterceptor.class, new Properties());
		registerService(bc,swingTaskManager,GUITaskManager.class, new Properties());
		registerService(bc,swingTaskManager,TaskManager.class, new Properties());
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
		registerServiceListener(bc,swingTaskManager,"addTunableInterceptor","removeTunableInterceptor",TunableInterceptor.class);
		registerServiceListener(bc,guiTunableInterceptor,"addTunableHandlerFactory","removeTunableHandlerFactory",GUITunableHandlerFactory.class, TunableHandlerFactory.class);


	}
}

