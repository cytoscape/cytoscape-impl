package org.cytoscape.ding.impl.work;

public interface ProgressMonitorCloseable extends AutoCloseable {

	@Override
	void close();
}
