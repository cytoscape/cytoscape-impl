package org.cytoscape.log.internal;


import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;


public class TestTaskFactory implements TaskFactory {
	int i = 0;
	public TaskIterator getTaskIterator() {
		Task task = new TestTask(i);
		i = (i + 1) % 4;
		return new TaskIterator(task);
	}
}


class TestTask extends AbstractTask {
	int i;
	public TestTask(int i) {
		this.i = i;
	}

	public void run(TaskMonitor taskMonitor) {
		Logger logger = Logger.getLogger(getClass());
		if (i == 0)
			logger.info("Happiness is a warm gun");
		else if (i == 1)
			logger.debug("When I hold you in my arms");
		else if (i == 2)
			logger.warn("And I put my finger on your trigger");
		else if (i == 3)
			logger.error("I know nobody can do me no harm, because...");
	}

	public void cancel() {
	}
}
