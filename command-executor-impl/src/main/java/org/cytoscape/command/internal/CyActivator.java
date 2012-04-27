package org.cytoscape.command.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.command.internal.CommandExecutorImpl;
import org.cytoscape.command.internal.CommandExecutorTaskFactory;
import org.cytoscape.command.internal.tunables.*;

import org.cytoscape.work.TaskFactory;

import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
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
		CommandTunableInterceptorImpl interceptor = new CommandTunableInterceptorImpl();
		
		CommandExecutorImpl commandExecutorImpl = new CommandExecutorImpl(cyApplicationManagerServiceRef, interceptor);
		CommandExecutorTaskFactory commandExecutorTaskFactory = new CommandExecutorTaskFactory(commandExecutorImpl);
		
		
		Properties commandExecutorTaskFactoryProps = new Properties();
		commandExecutorTaskFactoryProps.setProperty(PREFERRED_MENU,"Tools");
		commandExecutorTaskFactoryProps.setProperty(TITLE,"Run Commands...");
		registerService(bc,commandExecutorTaskFactory,TaskFactory.class, commandExecutorTaskFactoryProps);

		registerServiceListener(bc,commandExecutorImpl,"addTaskFactory","removeTaskFactory",TaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addNetworkTaskFactory","removeNetworkTaskFactory",NetworkTaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addNetworkViewTaskFactory","removeNetworkViewTaskFactory",NetworkViewTaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addTableTaskFactory","removeTableTaskFactory",TableTaskFactory.class);

		registerServiceListener(bc,interceptor,"addTunableHandlerFactory","removeTunableHandlerFactory",StringTunableHandlerFactory.class);

		StringTunableHandlerFactory<FileTunableHandler> fileTHF = new SimpleStringTunableHandlerFactory<FileTunableHandler>(FileTunableHandler.class, File.class);
		StringTunableHandlerFactory<IntTunableHandler> intTHF = new SimpleStringTunableHandlerFactory<IntTunableHandler>(IntTunableHandler.class, Integer.class, int.class);
		StringTunableHandlerFactory<DoubleTunableHandler> doubleTHF = new SimpleStringTunableHandlerFactory<DoubleTunableHandler>(DoubleTunableHandler.class, Double.class, double.class);
		StringTunableHandlerFactory<FloatTunableHandler> floatTHF = new SimpleStringTunableHandlerFactory<FloatTunableHandler>(FloatTunableHandler.class, Float.class, float.class);
		StringTunableHandlerFactory<LongTunableHandler> longTHF = new SimpleStringTunableHandlerFactory<LongTunableHandler>(LongTunableHandler.class, Long.class, long.class);
		StringTunableHandlerFactory<BooleanTunableHandler> booleanTHF = new SimpleStringTunableHandlerFactory<BooleanTunableHandler>(BooleanTunableHandler.class, Boolean.class, boolean.class);
		StringTunableHandlerFactory<BoundedIntTunableHandler> boundedIntTHF = new SimpleStringTunableHandlerFactory<BoundedIntTunableHandler>(BoundedIntTunableHandler.class, BoundedInteger.class);
		StringTunableHandlerFactory<BoundedDoubleTunableHandler> boundedDoubleTHF = new SimpleStringTunableHandlerFactory<BoundedDoubleTunableHandler>(BoundedDoubleTunableHandler.class, BoundedDouble.class);
		StringTunableHandlerFactory<BoundedFloatTunableHandler> boundedFloatTHF = new SimpleStringTunableHandlerFactory<BoundedFloatTunableHandler>(BoundedFloatTunableHandler.class, BoundedFloat.class);
		StringTunableHandlerFactory<BoundedLongTunableHandler> boundedLongTHF = new SimpleStringTunableHandlerFactory<BoundedLongTunableHandler>(BoundedLongTunableHandler.class, BoundedLong.class);
		StringTunableHandlerFactory<URLTunableHandler> urlTHF = new SimpleStringTunableHandlerFactory<URLTunableHandler>(URLTunableHandler.class, URL.class);
		StringTunableHandlerFactory<ListSingleTunableHandler> listSingleTHF = new SimpleStringTunableHandlerFactory<ListSingleTunableHandler>(ListSingleTunableHandler.class, ListSingleSelection.class);
		StringTunableHandlerFactory<ListMultipleTunableHandler> listMultipleTHF = new SimpleStringTunableHandlerFactory<ListMultipleTunableHandler>(ListMultipleTunableHandler.class, ListMultipleSelection.class);

		registerService(bc,fileTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,intTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,doubleTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,floatTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,longTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,booleanTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedIntTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedDoubleTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedFloatTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,boundedLongTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,urlTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,listSingleTHF,StringTunableHandlerFactory.class,new Properties());
		registerService(bc,listMultipleTHF,StringTunableHandlerFactory.class,new Properties());
	}
}

