package org.cytoscape.command.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.command.StringToModel;
import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.command.internal.available.ArgHandlerFactory;
import org.cytoscape.command.internal.available.ArgRecorder;
import org.cytoscape.command.internal.available.AvailableCommandsImpl;
import org.cytoscape.command.internal.available.BasicArgHandlerFactory;
import org.cytoscape.command.internal.tunables.BooleanTunableHandler;
import org.cytoscape.command.internal.tunables.BoundedDoubleTunableHandler;
import org.cytoscape.command.internal.tunables.BoundedFloatTunableHandler;
import org.cytoscape.command.internal.tunables.BoundedIntTunableHandler;
import org.cytoscape.command.internal.tunables.BoundedLongTunableHandler;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;
import org.cytoscape.command.internal.tunables.CyIdentifiableStringTunableHandlerFactory;
import org.cytoscape.command.internal.tunables.CyNetworkTunableHandler;
import org.cytoscape.command.internal.tunables.CyTableTunableHandler;
import org.cytoscape.command.internal.tunables.DoubleTunableHandler;
import org.cytoscape.command.internal.tunables.EdgeListTunableHandler;
import org.cytoscape.command.internal.tunables.FileTunableHandler;
import org.cytoscape.command.internal.tunables.FloatTunableHandler;
import org.cytoscape.command.internal.tunables.IntTunableHandler;
import org.cytoscape.command.internal.tunables.ListMultipleTunableHandler;
import org.cytoscape.command.internal.tunables.ListSingleTunableHandler;
import org.cytoscape.command.internal.tunables.LongTunableHandler;
import org.cytoscape.command.internal.tunables.NodeListTunableHandler;
import org.cytoscape.command.internal.tunables.RowListTunableHandler;
import org.cytoscape.command.internal.tunables.SimpleStringTunableHandlerFactory;
import org.cytoscape.command.internal.tunables.StringTunableHandlerImpl;
import org.cytoscape.command.internal.tunables.URLTunableHandler;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.command.util.RowList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
		
		CommandTunableInterceptorImpl interceptor = new CommandTunableInterceptorImpl();

		StringTunableHandlerFactory<FileTunableHandler> fileTHF = new SimpleStringTunableHandlerFactory<>(FileTunableHandler.class, File.class);
		StringTunableHandlerFactory<IntTunableHandler> intTHF = new SimpleStringTunableHandlerFactory<>(IntTunableHandler.class, Integer.class, int.class);
		StringTunableHandlerFactory<DoubleTunableHandler> doubleTHF = new SimpleStringTunableHandlerFactory<>(DoubleTunableHandler.class, Double.class, double.class);
		StringTunableHandlerFactory<FloatTunableHandler> floatTHF = new SimpleStringTunableHandlerFactory<>(FloatTunableHandler.class, Float.class, float.class);
		StringTunableHandlerFactory<LongTunableHandler> longTHF = new SimpleStringTunableHandlerFactory<>(LongTunableHandler.class, Long.class, long.class);
		StringTunableHandlerFactory<BooleanTunableHandler> booleanTHF = new SimpleStringTunableHandlerFactory<>(BooleanTunableHandler.class, Boolean.class, boolean.class);
		StringTunableHandlerFactory<StringTunableHandlerImpl> stringTHF = new SimpleStringTunableHandlerFactory<>(StringTunableHandlerImpl.class, String.class);
		StringTunableHandlerFactory<BoundedIntTunableHandler> boundedIntTHF = new SimpleStringTunableHandlerFactory<>(BoundedIntTunableHandler.class, BoundedInteger.class);
		StringTunableHandlerFactory<BoundedDoubleTunableHandler> boundedDoubleTHF = new SimpleStringTunableHandlerFactory<>(BoundedDoubleTunableHandler.class, BoundedDouble.class);
		StringTunableHandlerFactory<BoundedFloatTunableHandler> boundedFloatTHF = new SimpleStringTunableHandlerFactory<>(BoundedFloatTunableHandler.class, BoundedFloat.class);
		StringTunableHandlerFactory<BoundedLongTunableHandler> boundedLongTHF = new SimpleStringTunableHandlerFactory<>(BoundedLongTunableHandler.class, BoundedLong.class);
		StringTunableHandlerFactory<URLTunableHandler> urlTHF = new SimpleStringTunableHandlerFactory<>(URLTunableHandler.class, URL.class);
		StringTunableHandlerFactory<ListSingleTunableHandler> listSingleTHF = new SimpleStringTunableHandlerFactory<>(ListSingleTunableHandler.class, ListSingleSelection.class);
		StringTunableHandlerFactory<ListMultipleTunableHandler> listMultipleTHF = new SimpleStringTunableHandlerFactory<>(ListMultipleTunableHandler.class, ListMultipleSelection.class);

		registerService(bc, fileTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, intTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, doubleTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, floatTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, longTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, booleanTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, stringTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, boundedIntTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, boundedDoubleTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, boundedFloatTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, boundedLongTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, urlTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, listSingleTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, listMultipleTHF, StringTunableHandlerFactory.class, new Properties());


		StringToModel stm = new StringToModelImpl(serviceRegistrar);
		registerService(bc, stm, StringToModel.class, new Properties());

		CyIdentifiableStringTunableHandlerFactory<CyNetworkTunableHandler> networkTHF = new CyIdentifiableStringTunableHandlerFactory<>(
				stm, CyNetworkTunableHandler.class, CyNetwork.class);
		CyIdentifiableStringTunableHandlerFactory<CyTableTunableHandler> tableTHF = new CyIdentifiableStringTunableHandlerFactory<>(
				stm, CyTableTunableHandler.class, CyTable.class);
		CyIdentifiableStringTunableHandlerFactory<NodeListTunableHandler> nodeListTHF = new CyIdentifiableStringTunableHandlerFactory<>(
				stm, NodeListTunableHandler.class, NodeList.class);
		CyIdentifiableStringTunableHandlerFactory<EdgeListTunableHandler> edgeListTHF = new CyIdentifiableStringTunableHandlerFactory<>(
				stm, EdgeListTunableHandler.class, EdgeList.class);
		registerService(bc, networkTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, tableTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, nodeListTHF, StringTunableHandlerFactory.class, new Properties());
		registerService(bc, edgeListTHF, StringTunableHandlerFactory.class, new Properties());

		CyIdentifiableStringTunableHandlerFactory<RowListTunableHandler> rowListTHF = new CyIdentifiableStringTunableHandlerFactory<>(
				stm, RowListTunableHandler.class, RowList.class);
		registerService(bc, rowListTHF, StringTunableHandlerFactory.class, new Properties());

		BasicArgHandlerFactory argHandlerFactory = new BasicArgHandlerFactory();
		registerService(bc, argHandlerFactory, ArgHandlerFactory.class, new Properties());

		ArgRecorder argRec = new ArgRecorder();
		registerServiceListener(bc, argRec, "addTunableHandlerFactory", "removeTunableHandlerFactory", ArgHandlerFactory.class);

		AvailableCommandsImpl availableCommandsImpl = new AvailableCommandsImpl(argRec, serviceRegistrar);
		registerService(bc, availableCommandsImpl, AvailableCommands.class, new Properties());
		registerServiceListener(bc, availableCommandsImpl, "addTaskFactory", "removeTaskFactory", TaskFactory.class);
		registerServiceListener(bc, availableCommandsImpl, "addNetworkTaskFactory", "removeNetworkTaskFactory", NetworkTaskFactory.class);
		registerServiceListener(bc, availableCommandsImpl, "addNetworkViewTaskFactory", "removeNetworkViewTaskFactory", NetworkViewTaskFactory.class);
		registerServiceListener(bc, availableCommandsImpl, "addNetworkViewCollectionTaskFactory", "removeNetworkViewCollectionTaskFactory", NetworkViewCollectionTaskFactory.class);
		registerServiceListener(bc, availableCommandsImpl, "addTableTaskFactory", "removeTableTaskFactory", TableTaskFactory.class);

		CommandExecutorImpl commandExecutor = new CommandExecutorImpl(interceptor, availableCommandsImpl, serviceRegistrar);
		CommandExecutorTaskFactoryImpl commandExecutorTaskFactory = new CommandExecutorTaskFactoryImpl(commandExecutor, serviceRegistrar);

		Properties commandExecutorTaskFactoryProps = new Properties();
		commandExecutorTaskFactoryProps.setProperty(ID, "commandExecutorTaskFactory");
		registerService(bc, commandExecutorTaskFactory, TaskFactory.class, commandExecutorTaskFactoryProps);
		registerService(bc, commandExecutorTaskFactory, CommandExecutorTaskFactory.class, commandExecutorTaskFactoryProps);

		registerServiceListener(bc, commandExecutor, "addTaskFactory", "removeTaskFactory", TaskFactory.class);
		registerServiceListener(bc, commandExecutor, "addNetworkTaskFactory", "removeNetworkTaskFactory", NetworkTaskFactory.class);
		registerServiceListener(bc, commandExecutor, "addNetworkViewTaskFactory", "removeNetworkViewTaskFactory", NetworkViewTaskFactory.class);
		registerServiceListener(bc, commandExecutor, "addNetworkViewCollectionTaskFactory", "removeNetworkViewCollectionTaskFactory", NetworkViewCollectionTaskFactory.class);
		registerServiceListener(bc, commandExecutor, "addTableTaskFactory", "removeTableTaskFactory", TableTaskFactory.class);

		registerServiceListener(bc, interceptor, "addTunableHandlerFactory", "removeTunableHandlerFactory", StringTunableHandlerFactory.class);
	}
}
