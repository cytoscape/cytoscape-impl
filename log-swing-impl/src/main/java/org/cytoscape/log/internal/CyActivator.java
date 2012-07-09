package org.cytoscape.log.internal;

import org.osgi.framework.BundleContext;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Task;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;

import static org.cytoscape.work.ServiceProperties.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	static Properties ezProps(String... args) {
		final Properties props = new Properties();
		for (int i = 0; i < args.length; i += 2)
			props.setProperty(args[i], args[i + 1]);
		return props;
	}

	static <T> Map<T,T> ezMap(T... args) {
		final Map<T,T> map = new HashMap<T,T>();
		for (int i = 0; i < args.length; i+= 2)
			map.put(args[i], args[i + 1]);
		return map;
	}

	public void start(final BundleContext bc) {
		final CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
		final TaskManager taskManagerRef = getService(bc,TaskManager.class);

		final Map<String,String> logViewerConfig = ezMap(
			"baseHTMLPath","/consoledialogbase.html",
			"colorParityTrue","ffffff",
			"colorParityFalse","f9f9f9",
			"entryTemplate"," <html> <body bgcolor=\"#%s\"> <table border=0 width=\"100%%\" cellspacing=5> <tr> <td width=\"0%%\"><img src=\"%s\"></td> <td><h3>%s</h3></td> </tr> <tr> <td></td> <td><font size=\"-2\" color=\"#555555\">%s</font></td> </tr> </table> </body> </html>",
			"DEBUG","console-debug.png",
			"ERROR","console-error.png",
			"FATAL","console-error.png",
			"INFO","console-info.png",
			"TRACE","console-debug.png",
			"WARN","console-warn.png");

		final UserMessagesDialog userMessagesDialog = new UserMessagesDialog(cySwingApplicationRef, logViewerConfig);
		registerService(bc, new AbstractTaskFactory() {
			public TaskIterator createTaskIterator() {
				return new TaskIterator(new Task() {
					public void cancel() {}
					public void run(TaskMonitor monitor) {
						userMessagesDialog.open();
					}
				});
			}
		}, TaskFactory.class, ezProps(
			PREFERRED_MENU, "Help",
			TITLE, "User Messages...", MENU_GRAVITY, "3.0")
		);

		final CyStatusBar statusBar = new CyStatusBar(cySwingApplicationRef, "/logConsole.png", userMessagesDialog, ezMap(
			"DEBUG","/status-bar-debug.png", 
			"ERROR","/status-bar-error.png",
			"FATAL","/status-bar-error.png", 
			"INFO", "/status-bar-info.png",
			"TRACE","/status-bar-debug.png",
			"WARN", "/status-bar-warn.png"));

		final ExecutorService executor = Executors.newCachedThreadPool(new LowPriorityDaemonThreadFactory());

		final LinkedBlockingQueue<PaxLoggingEvent> userMessagesQueue = new LinkedBlockingQueue<PaxLoggingEvent>();
		executor.submit(new UserMessagesProcesser(userMessagesQueue, statusBar, userMessagesDialog));
		registerService(bc, new AppenderToQueue(userMessagesQueue), PaxAppender.class, ezProps(
			"org.ops4j.pax.logging.appender.name", "OrgCytoscapeLogSwingUserMessagesAppender"));

		final ConsoleDialog consoleDialog = new ConsoleDialog(taskManagerRef, cySwingApplicationRef, logViewerConfig);

		final LinkedBlockingQueue<PaxLoggingEvent> allLogMessagesQueue = new LinkedBlockingQueue<PaxLoggingEvent>();
		executor.submit(new AllLogMessagesProcesser(allLogMessagesQueue, consoleDialog));
		registerService(bc, new AppenderToQueue(allLogMessagesQueue), PaxAppender.class, ezProps(
			"org.ops4j.pax.logging.appender.name", "OrgCytoscapeLogSwingAllLogMessagesAppender"));

		registerService(bc, new AbstractTaskFactory() {
			public TaskIterator createTaskIterator() {
				return new TaskIterator(new Task() {
					public void cancel() {}
					public void run(TaskMonitor monitor) {
						consoleDialog.open();
					}
				});
			}
		}, TaskFactory.class, ezProps(
			PREFERRED_MENU, "Help",
			TITLE, "Developer's Log Console...", MENU_GRAVITY, "4.0")
		);
	}
}

