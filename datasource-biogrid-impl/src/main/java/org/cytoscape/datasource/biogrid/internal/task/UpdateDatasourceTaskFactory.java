package org.cytoscape.datasource.biogrid.internal.task;

import java.io.File;
import java.net.URL;

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class UpdateDatasourceTaskFactory extends AbstractTaskFactory {
	
	private final CyProperty<?> props;
	private final URL dataSource;
	private final File settingFileLocation;

	public UpdateDatasourceTaskFactory(final CyProperty<?> props, final URL dataSource, final File settingFileLocation) {
		this.props = props;
		this.dataSource = dataSource;
		this.settingFileLocation = settingFileLocation;
	}


	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new UpdateDatasourceTask(props, dataSource, settingFileLocation));
	}

}
