package org.cytoscape.work.internal;

import java.util.Properties;

import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.internal.task.CyUserLogAppender;
import org.cytoscape.work.internal.task.JDialogTaskManager;
import org.cytoscape.work.internal.task.JPanelTaskManager;
import org.cytoscape.work.internal.task.TaskHistory;
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
import org.cytoscape.work.internal.tunables.UserActionHandler;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.internal.view.TaskMediator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.SimpleGUITunableHandlerFactory;
import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.cytoscape.work.swing.util.UserAction;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		{
			UndoSupportImpl undoSupport = new UndoSupportImpl();
			Properties props = new Properties();
			registerService(bc, undoSupport, UndoSupport.class, props);
			registerService(bc, undoSupport, SwingUndoSupport.class, props);
		}
		
		JDialogTunableMutator jDialogTunableMutator = new JDialogTunableMutator();
		JPanelTunableMutator jPanelTunableMutator = new JPanelTunableMutator();

		TaskHistory taskHistory = new TaskHistory();
		TaskMediator taskMediator = new TaskMediator(taskHistory, serviceRegistrar);
		registerService(bc, taskMediator, CyStartListener.class);
		registerService(bc, new CyUserLogAppender(taskMediator, taskHistory), PaxAppender.class, ezProps("org.ops4j.pax.logging.appender.name", "CyUserLog"));

		JDialogTaskManager jDialogTaskManager = new JDialogTaskManager(jDialogTunableMutator, taskMediator, taskHistory, serviceRegistrar);
		PanelTaskManager jPanelTaskManager = new JPanelTaskManager(jPanelTunableMutator, jDialogTaskManager);

		SupportedFileTypesManager supportedFileTypesManager = new SupportedFileTypesManager();
		SimpleGUITunableHandlerFactory<BooleanHandler> booleanHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				BooleanHandler.class, Boolean.class, boolean.class);
		SimpleGUITunableHandlerFactory<IntegerHandler> integerHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				IntegerHandler.class, Integer.class, int.class);
		SimpleGUITunableHandlerFactory<FloatHandler> floatHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				FloatHandler.class, Float.class, float.class);

		SimpleGUITunableHandlerFactory<DoubleHandler> doubleHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				DoubleHandler.class, Double.class, double.class);
		SimpleGUITunableHandlerFactory<LongHandler> longHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				LongHandler.class, Long.class, long.class);
		SimpleGUITunableHandlerFactory<StringHandler> stringHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				StringHandler.class, String.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedIntegerHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				BoundedHandler.class, BoundedInteger.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedFloatHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				BoundedHandler.class, BoundedFloat.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedDoubleHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				BoundedHandler.class, BoundedDouble.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedLongHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				BoundedHandler.class, BoundedLong.class);
		SimpleGUITunableHandlerFactory<ListSingleHandler> listSingleSelectionHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				ListSingleHandler.class, ListSingleSelection.class);
		SimpleGUITunableHandlerFactory<ListMultipleHandler> listMultipleSelectionHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				ListMultipleHandler.class, ListMultipleSelection.class);
		SimpleGUITunableHandlerFactory<UserActionHandler> userActionHandlerFactory = new SimpleGUITunableHandlerFactory<>(
				UserActionHandler.class, UserAction.class);

		URLHandlerFactory urlHandlerFactory = new URLHandlerFactory(serviceRegistrar);
		registerService(bc, urlHandlerFactory, GUITunableHandlerFactory.class);

		FileHandlerFactory fileHandlerFactory = new FileHandlerFactory(supportedFileTypesManager, serviceRegistrar);
		registerService(bc, fileHandlerFactory, GUITunableHandlerFactory.class);

		registerService(bc, jDialogTaskManager, DialogTaskManager.class);
		registerService(bc, jDialogTaskManager, TaskManager.class);
		
		registerService(bc, jPanelTaskManager, PanelTaskManager.class);

		registerService(bc, integerHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, floatHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, doubleHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, longHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, booleanHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, stringHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, boundedIntegerHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, boundedFloatHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, boundedDoubleHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, boundedLongHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, listSingleSelectionHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, listMultipleSelectionHandlerFactory, GUITunableHandlerFactory.class);
		registerService(bc, userActionHandlerFactory, GUITunableHandlerFactory.class);

		registerServiceListener(bc,supportedFileTypesManager::addInputStreamTaskFactory,supportedFileTypesManager::removeInputStreamTaskFactory,InputStreamTaskFactory.class);
		registerServiceListener(bc,supportedFileTypesManager::addCyWriterTaskFactory,supportedFileTypesManager::removeCyWriterTaskFactory,CyWriterFactory.class);

		registerServiceListener(bc,jDialogTaskManager::addTunableRecorder,jDialogTaskManager::removeTunableRecorder,TunableRecorder.class);

		registerServiceListener(bc,jPanelTunableMutator::addTunableHandlerFactory,jPanelTunableMutator::removeTunableHandlerFactory,GUITunableHandlerFactory.class);
		registerServiceListener(bc,jDialogTunableMutator::addTunableHandlerFactory,jDialogTunableMutator::removeTunableHandlerFactory,GUITunableHandlerFactory.class);
	}

	static Properties ezProps(String... args) {
		final Properties props = new Properties();
		for (int i = 0; i < args.length; i += 2)
			props.setProperty(args[i], args[i + 1]);
		return props;
	}
}
