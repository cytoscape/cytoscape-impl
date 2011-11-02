package org.cytoscape.task.internal.loadvizmap;

import java.io.File;
import java.util.Set;

import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class LoadVizmapFileTask extends AbstractTask {

	@Tunable(description = "Vizmap file", params = "fileCategory=vizmap;input=true")
	public File file;

	private final VisualMappingManager vmMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private AddVisualStylesTask addVSTask;

	public LoadVizmapFileTask(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmMgr) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmMgr = vmMgr;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (file == null) 
			throw new NullPointerException("No file specified!");

		VizmapReader reader = vizmapReaderMgr.getReader(file.toURI(), file.getName());

		if (reader == null) 
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);

		addVSTask = new AddVisualStylesTask(reader, vmMgr);

		insertTasksAfterCurrentTask(reader, addVSTask);
	}

	public Set<VisualStyle> getStyles() {
		return addVSTask.getStyles();
	}
}

class AddVisualStylesTask extends AbstractTask {

	private final VizmapReader reader;
	private final VisualMappingManager vmMgr;
	private Set<VisualStyle> styles; 

	public AddVisualStylesTask(VizmapReader reader, VisualMappingManager vmMgr) {
		this.reader = reader;
		this.vmMgr = vmMgr;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Loading visual styles...");
		styles = reader.getVisualStyles();

		if (styles != null) {
			int count = 1;
			int total = styles.size();

			for (VisualStyle vs : styles) {
				if (cancelled) break;
				taskMonitor.setStatusMessage(count + " of " + total + ": " + vs.getTitle());
				vmMgr.addVisualStyle(vs);
				taskMonitor.setProgress(count / total);
				count++;
			}

			if (cancelled) {
				// remove recently added styles
				for (VisualStyle vs : styles) {
					vmMgr.removeVisualStyle(vs);
				}

				taskMonitor.setProgress(1.0);
			}
		}
	}

	public Set<VisualStyle> getStyles() {
		return styles; 
	}
}
