package org.cytoscape.work.internal.task;

import java.util.concurrent.ThreadFactory;
import java.util.Properties;
import org.cytoscape.property.CyProperty;

class TaskThreadFactory implements ThreadFactory {
	final static long DEFAULT_STACK_SIZE = 1024L * 1024L * 10L;

	final CyProperty<Properties> cyProperty;
	public TaskThreadFactory(CyProperty<Properties> cyProperty) {
		this.cyProperty = cyProperty;
	}

	public Thread newThread(Runnable r) {
		long stackSize;
		try {
			Properties props = cyProperty.getProperties();
			stackSize = Long.parseLong(props.getProperty("taskStackSize"));
		} catch (Exception e) {	
			stackSize = DEFAULT_STACK_SIZE;
		}
		return new Thread(null, r, "Task Thread", stackSize);
	}

}