package org.cytoscape.task.internal.export.web;

import java.io.File;
import java.io.FileOutputStream;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Task to export all networks and styles as Cytoscape.js style JSON.
 * 
 */
public class ExportAsWebArchiveTask extends AbstractTask {

	private static final String FILE_EXTENSION = ".zip";
	
	private static final String AS_SPA = "Full web application";
	private static final String AS_SIMPLE_PAGE = "Simple viewer for current network only";
	private static final String AS_ZIPPED_ARCHIVE = "Networks and Style JSON files only (No HTML)";

	@ProvidesTitle
	public String getTitle() {
		return "Export as Cytoscape.js Web Page";
	}

	@Tunable(description = "Export Networks and Styles as:", params = "fileCategory=archive;input=false")
	public File file;
	
	@Tunable(description = "Export as:")
	public ListSingleSelection<String> outputFormat;

	private final CySessionWriterFactory fullWriterFactory;
	private final CySessionWriterFactory simpleWriterFactory;
	private final CySessionWriterFactory zippedWriterFactory;
	
	private CyWriter writer;

	public ExportAsWebArchiveTask(
			final CySessionWriterFactory fullWriterFactory,
			final CySessionWriterFactory simpleWriterFactory,
			final CySessionWriterFactory zippedWriterFactory) {
		super();
		this.fullWriterFactory = fullWriterFactory;
		this.simpleWriterFactory = simpleWriterFactory;
		this.zippedWriterFactory = zippedWriterFactory;
		
		this.outputFormat = new ListSingleSelection<String>(AS_SPA, AS_SIMPLE_PAGE, AS_ZIPPED_ARCHIVE);
	}

	/**
	 * Archive the data into a zip file.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(file == null) {
			return;
		}
		// Get export type
		final String exportType = this.outputFormat.getSelectedValue();
		
		taskMonitor.setProgress(0.05);

		// Add extension if missing.
		if (!file.getName().endsWith(FILE_EXTENSION))
			file = new File(file.getPath() + FILE_EXTENSION);

		// Compress everything as a zip archive.
		final FileOutputStream os = new FileOutputStream(file);
		CyWriter writer = null;
		if(exportType.equals(AS_SPA)) {
			writer = fullWriterFactory.createWriter(os, null);
		} else if(exportType.equals(AS_SIMPLE_PAGE)) {
			writer = simpleWriterFactory.createWriter(os, null);
		} else if(exportType.equals(AS_ZIPPED_ARCHIVE)) {
			writer = zippedWriterFactory.createWriter(os, null);
		} else {
			os.close();
			throw new NullPointerException("Could not find web session writer.");
		}
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