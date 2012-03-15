package org.cytoscape.group.data.internal;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

class CyGroupSettingsTaskFactory implements TaskFactory {
	CyGroupSettingsImpl settings;

	public CyGroupSettingsTaskFactory(CyGroupSettingsImpl settings) {
		this.settings = settings;
	}

	public CyGroupSettingsImpl getSettings() { return settings; }

	public TaskIterator createTaskIterator() {
		return new TaskIterator(settings);
	}
}
