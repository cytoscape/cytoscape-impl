package org.cytoscape.datasource.biogrid.internal.task;

import java.io.File;
import java.net.URL;

import org.cytoscape.datasource.biogrid.internal.BiogridDataLoader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class UpdateDatasourceTask extends AbstractTask {

	private final CyProperty<?> props;
	private final URL dataSource;
	private final File settingFileLocation;

	public UpdateDatasourceTask(final CyProperty<?> props, final URL dataSource, final File settingFileLocation) {
		this.props = props;
		this.dataSource = dataSource;
		this.settingFileLocation = settingFileLocation;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setTitle("Updating Organism Interactome Data Files");
		taskMonitor.setProgress(-1);
		taskMonitor.setStatusMessage("Downloading data from BioGRID...");

		BiogridDataLoader task = new BiogridDataLoader(props, dataSource, settingFileLocation);
		task.extract(true);

		final String version = task.getVersion();

		taskMonitor.setProgress(1.0d);
		taskMonitor.setStatusMessage("Organism network data files has been updated to BioGRID version " + version
				+ ".  Please restart Cytoscape to use new data.");
	}
}