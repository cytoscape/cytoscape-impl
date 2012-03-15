
package org.cytoscape.command.internal;

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

	}
}
