package org.cytoscape.task.internal.export.web;

import java.io.File;
import java.io.FileOutputStream;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 * Task to export all networks and styles as Cytoscape.js style JSON.
 * 
 */
public class ExportAsWebArchiveTask extends AbstractTask {

	private static final String FILE_EXTENSION = ".zip";

	@ProvidesTitle
	public String getTitle() {
		return "Export as Cytoscape.js Web Page";
	}

	@Tunable(description = "Export Networks and Styles As:", params = "fileCategory=session;input=false")
	public File file;

	private final CySessionWriterFactory writerFactory;
	private CyWriter writer;

	public ExportAsWebArchiveTask(final CySessionWriterFactory writerFactory) {
		super();
		this.writerFactory = writerFactory;
	}

	/**
	 * Archive the data into a zip file.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.05);

		// Add extension if missing.
		if (!file.getName().endsWith(FILE_EXTENSION))
			file = new File(file.getPath() + FILE_EXTENSION);

		// Compress everything as a zip archive.
		final FileOutputStream os = new FileOutputStream(file);
		final CyWriter writer = writerFactory.createWriter(os, null);
		writer.run(taskMonitor);
		os.close();

		taskMonitor.setProgress(1.0);
	}

	@Override
	public void cancel() {
		super.cancel();
		if (writer != null)
			writer.cancel();
	}
}