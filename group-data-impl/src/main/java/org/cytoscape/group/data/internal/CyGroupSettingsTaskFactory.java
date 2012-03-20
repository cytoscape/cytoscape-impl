package org.cytoscape.group.data.internal;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

class CyGroupSettingsTaskFactory extends AbstractTaskFactory {
	CyGroupSettingsImpl settings;

	public CyGroupSettingsTaskFactory(CyGroupSettingsImpl settings) {
		this.settings = settings;
	}

	public CyGroupSettingsImpl getSettings() { return settings; }

	public TaskIterator createTaskIterator() {
		return new TaskIterator(settings);
	}
}
