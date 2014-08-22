package org.cytoscape.io.internal.write.websession;

import org.cytoscape.work.TaskMonitor;

public interface WebSessionWriter {

	void writeFiles(TaskMonitor tm) throws Exception;
	
}
