package org.cytoscape.io.internal.write.websession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.write.json.CytoscapeJsNetworkWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;

public class ZippedArchiveWriter extends WebSessionWriterImpl {
	
	private static final String ZIP_FOLDER = "networks";

	public ZippedArchiveWriter(OutputStream outputStream, String exportType,
			VizmapWriterFactory jsonStyleWriterFactory,
			VisualMappingManager vmm,
			CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory,
			CyNetworkViewManager viewManager,
			CyApplicationConfiguration appConfig,
			final CyApplicationManager applicationManager) {
		
		super(outputStream, exportType, jsonStyleWriterFactory, vmm,
				cytoscapejsWriterFactory, viewManager, appConfig);
	}

	@Override
	public void writeFiles(TaskMonitor tm) throws Exception {

		// Phase 1: Write current network files as Cytoscape.js-style JSON
		tm.setProgress(0.1);
		tm.setStatusMessage("Saving networks as Cytoscape.js JSON...");

		final Set<CyNetworkView> viewSet = this.viewManager.getNetworkViewSet();
		final Collection<File> files = createNetworkViewFiles(viewSet);
		
		tm.setProgress(0.7);

		if (cancelled)
			return;

		// Phase 2: Write a Style JSON.
		tm.setStatusMessage("Saving Visual Styles as JSON...");
		final File styleFile = createStyleFile();
		files.add(styleFile);
		tm.setProgress(0.9);

		// Zip networks and styles into a file.
		zipAll(files);

		if (cancelled)
			return;

		tm.setStatusMessage("Done.");
		tm.setProgress(1.0);
	}

	private final void zipAll(final Collection<File> files) throws IOException {
		// Zip them into one file
		zos = new ZipOutputStream(outputStream);
		addFiles(files, zos);
		zos.close();
	}

	private void addFiles(final Collection<File> files,
			final ZipOutputStream out) throws IOException {
		
		final byte[] buffer = new byte[4096];

		for (final File file : files) {
			if (file.isDirectory()) {
				// This should not happen. Only JSON files will be saved.
				continue;
			}

			final FileInputStream in = new FileInputStream(file);
			String zipFilePath = null;
			final String fileName = file.getName();
			final Path dataFilePath = Paths.get(ZIP_FOLDER, fileName);
			zipFilePath = dataFilePath.toString();

			// This is for Windows System: Replace file separator to slash.
			if (File.separatorChar != '/') {
				zipFilePath = zipFilePath.replace('\\', '/');
			}

			// Add normalized path name;
			if(zipFilePath.contains("style_")) {
				zipFilePath = "style.json";
			}
			
			final ZipEntry entry = new ZipEntry(zipFilePath);
			out.putNextEntry(entry);

			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			out.closeEntry();
			in.close();
		}
	}
}
