package org.cytoscape.command.internal;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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

import java.util.Map;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TFExecutor implements Executor {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final TaskFactory tf;
	private final CommandTunableInterceptorImpl interceptor; 
	

	public TFExecutor(TaskFactory tf, CommandTunableInterceptorImpl interceptor) {
		this.tf = tf;
		this.interceptor = interceptor;
	}

	@Override
	public void execute(String args, TaskMonitor taskMonitor, TaskObserver observer) throws Exception {
		interceptor.setConfigurationContext((String)null); // Reset TunableInterceptor
		TaskIterator ti = tf.createTaskIterator();
		while (ti.hasNext()) {
			Task t = ti.next();
			interceptor.setConfigurationContext(args);
			interceptor.validateAndWriteBackTunables(t);
			TaskMonitor tm = new LogDelegateTaskMonitor(taskMonitor);
			if(!interceptor.validateTunableInput(t, tm)) {
				if(observer != null)
					observer.allFinished(FinishStatus.newCancelled(t));
				return;
			}
			t.run(tm);
			if (observer != null && t instanceof ObservableTask) {
				observer.taskFinished((ObservableTask)t);
			}
		}
	}

	@Override
	public void execute(Map<String, Object> args, TaskMonitor taskMonitor, TaskObserver observer) throws Exception {
		interceptor.setConfigurationContext((Map<String, Object>)null); // Reset TunableInterceptor
		TaskIterator ti = tf.createTaskIterator();
		while (ti.hasNext()) {
			Task t = ti.next();
			interceptor.setConfigurationContext(args);
			interceptor.validateAndWriteBackTunables(t);
			TaskMonitor tm = new LogDelegateTaskMonitor(taskMonitor);
			if(!interceptor.validateTunableInput(t, tm)) {
				if(observer != null)
					observer.allFinished(FinishStatus.newCancelled(t));
				return;
			}
			t.run(tm);
			if (observer != null && t instanceof ObservableTask) {
				observer.taskFinished((ObservableTask)t);
			}
		}
	}

	
	private class LogDelegateTaskMonitor implements TaskMonitor {
		
		private TaskMonitor delegate;
		
		public LogDelegateTaskMonitor(TaskMonitor delegate) {
			this.delegate = delegate;
		}
		
		public void setTitle(String title) { }
		
		public void setProgress(double progress) { }
		
		public void setStatusMessage(String statusMessage) {
			showMessage(TaskMonitor.Level.INFO, statusMessage);
		}
		
		public void showMessage(TaskMonitor.Level level, String message) {
			switch(level) {
				case INFO:
					logger.info(message);
					break;
				case WARN:
					logger.warn(message);
					break;
				case ERROR:
					logger.error(message);
					break;
			}
			
			if(delegate != null)
				delegate.showMessage(level, message);
		}
	}
	
}
