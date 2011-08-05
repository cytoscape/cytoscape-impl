package org.cytoscape.cpath2.internal.util;

import org.cytoscape.work.TaskMonitor;

public class NullTaskMonitor implements TaskMonitor {

	@Override
	public void setTitle(String title) {
	}

	@Override
	public void setProgress(double progress) {
	}

	@Override
	public void setStatusMessage(String statusMessage) {
	}
}