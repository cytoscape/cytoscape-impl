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

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Task;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TFExecutor implements Executor {
	private final TaskFactory tf;
	private final CommandTunableInterceptorImpl interceptor; 
	private final TaskMonitor tm = new OutTaskMonitor(); 
	private static final Logger logger = LoggerFactory.getLogger(TFExecutor.class);

	public TFExecutor(TaskFactory tf, CommandTunableInterceptorImpl interceptor) {
		this.tf = tf;
		this.interceptor = interceptor;
	}

	public void execute(String args) throws Exception {
		// TODO
		// At some point in the future, this code should be reorganized into
		// a proper TaskManager - that's really what's happening here.
		TaskIterator ti = tf.createTaskIterator();
		while (ti.hasNext()) {
			Task t = ti.next();
			interceptor.setConfigurationContext(args);
			interceptor.validateAndWriteBackTunables(t);
			t.run(tm);
		}
	}

	private class OutTaskMonitor implements TaskMonitor {
		public void setTitle(String title) {
			//System.out.println("set title: " + title);
		}
		public void setProgress(double progress) {
			//System.out.println("set progress: " + progress);
		}
		public void setStatusMessage(String statusMessage) {
			//System.out.println("set statusMessage: " + statusMessage);
		}
        public void showMessage(TaskMonitor.Level level, String message) {
        }
	}
}
