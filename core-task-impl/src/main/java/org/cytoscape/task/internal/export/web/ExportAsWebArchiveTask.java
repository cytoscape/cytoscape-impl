package org.cytoscape.task.internal.export.web;

import java.io.File;
import java.io.FileOutputStream;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ExportAsWebArchiveTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Export to Web Archive";
	}

	@Tunable(description = "Export Networks and Styles As:", params = "fileCategory=session;input=false")
	public File file;

	private final CySessionWriterFactory writerFactory;
	private CyWriter writer;

	public ExportAsWebArchiveTask(final CySessionWriterFactory writerFactory) {
		super();
		this.writerFactory = writerFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.05);
		System.out.println("AAA ============File is " + file);

		final FileOutputStream os = new FileOutputStream(file);
		System.out.println("============File is " + file.getName());
		final CyWriter writer = writerFactory.createWriter(os, null);
		writer.run(taskMonitor);
		
		os.close();

		taskMonitor.setProgress(1.0);

		// Add this session file URL as the most recent file.
		if (!file.getName().endsWith(".zip"))
			file = new File(file.getPath() + ".zip");
	}

	@Override
	public void cancel() {
		super.cancel();

		if (writer != null)
			writer.cancel();
	}
}