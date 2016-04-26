package org.cytoscape.command.internal;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.command.StringToModel;
import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.command.util.RowList;
import org.cytoscape.command.internal.tunables.*;
import org.cytoscape.command.internal.available.*;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;

import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.*;

import org.osgi.framework.BundleContext;

import java.io.File;
import java.net.URL;
import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyTableManager cytableManagerServiceRef = getService(bc,CyTableManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		TunableSetter tunableSetterServiceRef = getService(bc,TunableSetter.class);
		DynamicTaskFactoryProvisioner dynamicTaskFactoryProvisionerServiceRef = getService(bc, DynamicTaskFactoryProvisioner.class);
		
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

		registerService(bc,fileTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,intTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,doubleTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,floatTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,longTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,booleanTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,stringTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedIntTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedDoubleTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedFloatTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedLongTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,urlTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,listSingleTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,listMultipleTHF,StringTunableHandlerFactory.class,new Properties());


		StringToModel stm = new StringToModelImpl(cyApplicationManagerServiceRef, 
		                                          cyNetworkManagerServiceRef, cytableManagerServiceRef,cyNetworkViewManagerServiceRef);
		registerService(bc,stm,StringToModel.class,new Properties());

		CyIdentifiableStringTunableHandlerFactory<CyNetworkTunableHandler> networkTHF =
				new CyIdentifiableStringTunableHandlerFactory<>(stm, CyNetworkTunableHandler.class, CyNetwork.class);
		CyIdentifiableStringTunableHandlerFactory<CyTableTunableHandler> tableTHF =
				new CyIdentifiableStringTunableHandlerFactory<>(stm, CyTableTunableHandler.class, CyTable.class);
		CyIdentifiableStringTunableHandlerFactory<NodeListTunableHandler> nodeListTHF =
				new CyIdentifiableStringTunableHandlerFactory<>(stm, NodeListTunableHandler.class, NodeList.class);
		CyIdentifiableStringTunableHandlerFactory<EdgeListTunableHandler> edgeListTHF =
				new CyIdentifiableStringTunableHandlerFactory<>(stm, EdgeListTunableHandler.class, EdgeList.class);
		registerService(bc,networkTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,tableTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,nodeListTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,edgeListTHF,StringTunableHandlerFactory.class,new Properties());

		CyIdentifiableStringTunableHandlerFactory<RowListTunableHandler> rowListTHF =
				new CyIdentifiableStringTunableHandlerFactory<>(stm, RowListTunableHandler.class, RowList.class);
		registerService(bc,rowListTHF,StringTunableHandlerFactory.class,new Properties());

		BasicArgHandlerFactory argHandlerFactory = new BasicArgHandlerFactory();
		registerService(bc,argHandlerFactory,ArgHandlerFactory.class,new Properties());

		ArgRecorder argRec = new ArgRecorder();
		registerServiceListener(bc,argRec,"addTunableHandlerFactory","removeTunableHandlerFactory",ArgHandlerFactory.class);
		AvailableCommandsImpl cla = new AvailableCommandsImpl(argRec, cyApplicationManagerServiceRef);
		registerService(bc,cla,AvailableCommands.class,new Properties());
		registerServiceListener(bc,cla,"addTaskFactory","removeTaskFactory",TaskFactory.class);
		registerServiceListener(bc,cla,"addNetworkTaskFactory","removeNetworkTaskFactory",NetworkTaskFactory.class);
		registerServiceListener(bc,cla,"addNetworkViewTaskFactory","removeNetworkViewTaskFactory",NetworkViewTaskFactory.class);
		registerServiceListener(bc,cla,"addNetworkViewCollectionTaskFactory","removeNetworkViewCollectionTaskFactory",NetworkViewCollectionTaskFactory.class);
		registerServiceListener(bc,cla,"addTableTaskFactory","removeTableTaskFactory",TableTaskFactory.class);

		CommandExecutorImpl commandExecutorImpl = new CommandExecutorImpl(cyApplicationManagerServiceRef, interceptor, cla, dynamicTaskFactoryProvisionerServiceRef);
		CommandExecutorTaskFactoryImpl commandExecutorTaskFactory = new CommandExecutorTaskFactoryImpl(commandExecutorImpl,tunableSetterServiceRef);
		
		
		Properties commandExecutorTaskFactoryProps = new Properties();
		commandExecutorTaskFactoryProps.setProperty(ID,"commandExecutorTaskFactory");
		registerService(bc,commandExecutorTaskFactory,TaskFactory.class, commandExecutorTaskFactoryProps);
		registerService(bc,commandExecutorTaskFactory,CommandExecutorTaskFactory.class, commandExecutorTaskFactoryProps);

		registerServiceListener(bc,commandExecutorImpl,"addTaskFactory","removeTaskFactory",TaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addNetworkTaskFactory","removeNetworkTaskFactory",NetworkTaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addNetworkViewTaskFactory","removeNetworkViewTaskFactory",NetworkViewTaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addNetworkViewCollectionTaskFactory","removeNetworkViewCollectionTaskFactory",NetworkViewCollectionTaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addTableTaskFactory","removeTableTaskFactory",TableTaskFactory.class);

		registerServiceListener(bc,interceptor,"addTunableHandlerFactory","removeTunableHandlerFactory",StringTunableHandlerFactory.class);
	}
}

