package org.cytoscape.log.internal;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Log Swing Impl (log-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

	static Properties ezProps(String... args) {
		final Properties props = new Properties();
		for (int i = 0; i < args.length; i += 2)
			props.setProperty(args[i], args[i + 1]);
		return props;
	}

	static <T> Map<T,T> ezMap(T... args) {
		final Map<T,T> map = new HashMap<>();
		for (int i = 0; i < args.length; i+= 2)
			map.put(args[i], args[i + 1]);
		return map;
	}

	@Override
	public void start(final BundleContext bc) {
		final CySwingApplication cySwingApplicationRef = getService(bc, CySwingApplication.class);
		final TaskManager taskManagerRef = getService(bc, TaskManager.class);

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
			@Override
			public TaskIterator createTaskIterator() {
				return new TaskIterator(new Task() {
					@Override
					public void cancel() {}
					@Override
					public void run(TaskMonitor tm) {
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

		final LinkedBlockingQueue<PaxLoggingEvent> userMessagesQueue = new LinkedBlockingQueue<>();
		executor.submit(new UserMessagesProcesser(userMessagesQueue, statusBar, userMessagesDialog));
		registerService(bc, new AppenderToQueue(userMessagesQueue), PaxAppender.class, ezProps(
			"org.ops4j.pax.logging.appender.name", "OrgCytoscapeLogSwingUserMessagesAppender"));

		final ConsoleDialog consoleDialog = new ConsoleDialog(taskManagerRef, cySwingApplicationRef, logViewerConfig);

		final LinkedBlockingQueue<PaxLoggingEvent> allLogMessagesQueue = new LinkedBlockingQueue<>();
		executor.submit(new AllLogMessagesProcesser(allLogMessagesQueue, consoleDialog));
		registerService(bc, new AppenderToQueue(allLogMessagesQueue), PaxAppender.class, ezProps(
			"org.ops4j.pax.logging.appender.name", "OrgCytoscapeLogSwingAllLogMessagesAppender"));

		registerService(bc, new AbstractTaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return new TaskIterator(new Task() {
					@Override
					public void cancel() {}
					@Override
					public void run(TaskMonitor tm) {
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

