package org.cytoscape.io.internal.write.websession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.cytoscape.io.internal.write.json.CytoscapeJsNetworkWriterFactory;
import org.cytoscape.io.internal.write.json.JSONNetworkViewWriter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSessionWriterImpl extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger(WebSessionWriterImpl.class);

	private static final String VIZMAP_FILE = "style.json";

	private final String sessionDir;
	private ZipOutputStream zos;
	private TaskMonitor taskMonitor;

	private final OutputStream outputStream;
	private final VizmapWriterFactory jsonStyleWriterFactory;
	private final VisualMappingManager vmm;
	private final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory;
	private final CyNetworkViewManager viewManager;

	public WebSessionWriterImpl(final OutputStream outputStream, final VizmapWriterFactory jsonStyleWriterFactory,
			final VisualMappingManager vmm, final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory,
			final CyNetworkViewManager viewManager) {
		this.outputStream = outputStream;
		this.jsonStyleWriterFactory = jsonStyleWriterFactory;
		this.vmm = vmm;
		this.cytoscapejsWriterFactory = cytoscapejsWriterFactory;
		this.viewManager = viewManager;

		// Write to top dir.
		sessionDir = "/html5/";
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		this.taskMonitor = tm;
		try {
			tm.setProgress(0.0);
			tm.setTitle("Archiving into HTML5 Files");
			zos = new ZipOutputStream(outputStream);
			writeFiles(tm);
		} finally {
			try {
				if (zos != null) {
					zos.close();
					zos = null;
				}
			} catch (Exception e) {
				logger.error("Error closing zip output stream", e);
			}
		}
	}

	private void writeFiles(TaskMonitor tm) throws Exception {
		// Phase 0: Prepare local temp files. This is necessary because
		// Jackson forces to close the stream!

		// Phase 1: Write all network files as Cytoscape.js-style JSON
		tm.setProgress(0.1);
		tm.setStatusMessage("Saving networks as Cytoscape.js JSON...");
		final Collection<File> files = createNetworkViewFiles();
		tm.setProgress(0.6);

		if (cancelled)
			return;

		// Phase 2: Write a Style JSON.
		tm.setStatusMessage("Saving Visual Styles as JSON...");
		File styleFile = createStyleFile();
		files.add(styleFile);
		tm.setProgress(0.7);

		// Zip them into one file
		zos = new ZipOutputStream(outputStream);

		byte[] buffer = new byte[4096];
		int bytesRead;
		for (final File f : files) {
			FileInputStream in = new FileInputStream(f);
			ZipEntry entry = new ZipEntry(sessionDir + f.getName());
			zos.putNextEntry(entry);
			while ((bytesRead = in.read(buffer)) != -1)
				zos.write(buffer, 0, bytesRead);
			in.close();
			zos.closeEntry();
		}
		zos.close();

		if (cancelled)
			return;

		// Phase 3: HTML and other resource files
		tm.setStatusMessage("Saving HTML5 visualization code...");

		tm.setStatusMessage("Done.");
		tm.setProgress(1.0);
	}

	/**
	 * Write a JSON file for Visual Styles.
	 * 
	 * @throws Exception
	 */
	private final File createStyleFile() throws Exception {
		// Write all Styles into one JSON file.
		final Set<VisualStyle> styles = vmm.getAllVisualStyles();
		File styleFile = File.createTempFile("style", ".json");
		CyWriter vizmapWriter = jsonStyleWriterFactory.createWriter(new FileOutputStream(styleFile), styles);
		vizmapWriter.run(taskMonitor);
		return styleFile;
	}

	/**
	 * Writes network view JSON files to the zip archive.
	 * 
	 * @throws Exception
	 */
	private Collection<File> createNetworkViewFiles() throws Exception {
		final Set<CyNetworkView> netViews = viewManager.getNetworkViewSet();
		final Collection<File> networkFiles = new HashSet<File>();
		for (final CyNetworkView view : netViews) {
			if (cancelled)
				return networkFiles;

			final CyNetwork network = view.getModel();
			String jsonFileName = network.getRow(network).get(CyNetwork.NAME, String.class) + "-"
					+ network.getSUID().toString() + "-";

			File tempFile = File.createTempFile(jsonFileName, ".json");
			JSONNetworkViewWriter writer = (JSONNetworkViewWriter) cytoscapejsWriterFactory.createWriter(
					new FileOutputStream(tempFile), view);
			writer.run(taskMonitor);
			networkFiles.add(tempFile);
		}
		return networkFiles;
	}
}