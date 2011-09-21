



package org.cytoscape.log.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskManager;

import org.cytoscape.log.internal.CytoStatusBar;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.cytoscape.log.internal.SimpleQueueAppender;
import org.cytoscape.log.internal.AdvancedQueueAppender;
import java.util.concurrent.LinkedBlockingQueue;
import org.cytoscape.log.internal.LowPriorityDaemonThreadFactory;
import org.cytoscape.log.internal.StatusBarQueueAppender;
import org.cytoscape.log.internal.StatusBarUpdater;
import org.cytoscape.log.internal.ConsoleTaskFactory;
import java.lang.String;

import org.cytoscape.work.TaskFactory;
import org.ops4j.pax.logging.spi.PaxAppender;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		TaskManager taskManagerRef = getService(bc,TaskManager.class);
		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);

		Map logViewConfigMap = new HashMap();
		logViewConfigMap.put("baseHTMLPath","/consoledialogbase.html"); 
		logViewConfigMap.put("colorParityTrue","ffffff");
		logViewConfigMap.put("colorParityFalse","eeeeee");
		logViewConfigMap.put("entryTemplate"," <html> <body bgcolor=\"#%s\"> <table border=0 width=\"100%%\" cellspacing=5> <tr> <td width=\"0%%\"><img src=\"%s\"></td> <td><h3>%s</h3></td> </tr> <tr> <td></td> <td><font size=\"-2\" color=\"#555555\">%s</font></td> </tr> </table> </body> </html>");
		logViewConfigMap.put("DEBUG","console-debug.png");
		logViewConfigMap.put("ERROR","console-error.png");
		logViewConfigMap.put("FATAL","console-error.png");
		logViewConfigMap.put("INFO","console-info.png");
		logViewConfigMap.put("TRACE","console-debug.png");
		logViewConfigMap.put("WARN","console-warning.png");

		Map statusBarConfigMap = new HashMap();
		statusBarConfigMap.put("DEBUG","/petit-debug.png"); 
		statusBarConfigMap.put("ERROR","/petit-error.png");
		statusBarConfigMap.put("FATAL","/petit-error.png"); 
		statusBarConfigMap.put("INFO","/petit-info.png");
		statusBarConfigMap.put("TRACE","/petit-debug.png");
		statusBarConfigMap.put("WARN","/petit-warning.png");

		LowPriorityDaemonThreadFactory threadFactory = new LowPriorityDaemonThreadFactory();
		ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);
		LinkedBlockingQueue simpleQueue = new LinkedBlockingQueue();
		LinkedBlockingQueue advancedQueue = new LinkedBlockingQueue();
		LinkedBlockingQueue statusBarQueue = new LinkedBlockingQueue();
		AdvancedQueueAppender advancedAppender = new AdvancedQueueAppender(advancedQueue);
		SimpleQueueAppender simpleAppender = new SimpleQueueAppender(simpleQueue);
		StatusBarQueueAppender statusBarAppender = new StatusBarQueueAppender(statusBarQueue);
		CytoStatusBar cytoStatusBar = new CytoStatusBar(cySwingApplicationRef,"/user-trash.png");
		StatusBarUpdater statusBarUpdater = new StatusBarUpdater(cytoStatusBar,statusBarQueue,statusBarConfigMap);
		ConsoleTaskFactory consoleTaskFactory = new ConsoleTaskFactory(simpleQueue,advancedQueue,executorService,cytoStatusBar,cySwingApplicationRef,taskManagerRef,logViewConfigMap,logViewConfigMap);
		
		
		Properties advancedAppenderProps = new Properties();
		advancedAppenderProps.setProperty("org.ops4j.pax.logging.appender.name","OrgCytoscapeLogSwingAdvancedAppender");
		registerService(bc,advancedAppender,PaxAppender.class, advancedAppenderProps);

		Properties simpleAppenderProps = new Properties();
		simpleAppenderProps.setProperty("org.ops4j.pax.logging.appender.name","OrgCytoscapeLogSwingSimpleAppender");
		registerService(bc,simpleAppender,PaxAppender.class, simpleAppenderProps);

		Properties statusBarAppenderProps = new Properties();
		statusBarAppenderProps.setProperty("org.ops4j.pax.logging.appender.name","OrgCytoscapeLogSwingStatusBarAppender");
		registerService(bc,statusBarAppender,PaxAppender.class, statusBarAppenderProps);

		Properties consoleTaskFactoryProps = new Properties();
		consoleTaskFactoryProps.setProperty("preferredMenu","Help");
		consoleTaskFactoryProps.setProperty("title","Log Console");
		registerService(bc,consoleTaskFactory,TaskFactory.class, consoleTaskFactoryProps);

	}
}

