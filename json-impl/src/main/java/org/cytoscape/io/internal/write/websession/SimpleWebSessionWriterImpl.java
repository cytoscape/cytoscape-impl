package org.cytoscape.io.internal.write.websession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
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

public class SimpleWebSessionWriterImpl extends WebSessionWriterImpl {

	private final CyApplicationManager applicationManager;
	private Path resourceFilePath;

	public SimpleWebSessionWriterImpl(OutputStream outputStream, String exportType,
			VizmapWriterFactory jsonStyleWriterFactory, VisualMappingManager vmm,
			CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory, CyNetworkViewManager viewManager,
			CyApplicationConfiguration appConfig, final CyApplicationManager applicationManager) {
		super(outputStream, exportType, jsonStyleWriterFactory, vmm, cytoscapejsWriterFactory, viewManager, appConfig);
		this.applicationManager = applicationManager;
	}

	@Override
	public void writeFiles(TaskMonitor tm) throws Exception {

		// Phase 1: Write current network files as Cytoscape.js-style JSON
		tm.setProgress(0.1);
		tm.setStatusMessage("Saving networks as Cytoscape.js JSON...");
		final CyNetworkView view = applicationManager.getCurrentNetworkView();
		final Set<CyNetworkView> viewSet = new HashSet<CyNetworkView>();
		viewSet.add(view);
		final Collection<File> files = createNetworkViewFiles(viewSet);
		if (files.size() != 1) {
			throw new IllegalStateException("Simple Web Session Writer takes only one network view.");
		}
		tm.setProgress(0.7);

		if (cancelled)
			return;

		// Phase 2: Write a Style JSON.
		tm.setStatusMessage("Saving Visual Styles as JSON...");
		final File styleFile = createStyleFile();
		files.add(styleFile);
		tm.setProgress(0.9);

		// Phase 3: Zip everything
		this.resourceFilePath = Paths.get(webResourceDirectory.getAbsolutePath(), WEB_RESOURCE_NAME, exportType);
		files.add(resourceFilePath.toFile());
		zipAll(files);

		if (cancelled)
			return;

		tm.setStatusMessage("Done.");
		tm.setProgress(1.0);
	}

	private final void zipAll(final Collection<File> files) throws IOException {
		// Zip them into one file
		zos = new ZipOutputStream(outputStream);
		addDir(files.toArray(new File[0]), zos);
		zos.close();
	}

	private void addDir(final File[] files, final ZipOutputStream out) throws IOException {
		final byte[] buffer = new byte[4096];

		for (final File file : files) {
			if (file.isDirectory()) {
				// Recursively add contents in the directory
				addDir(file.listFiles(), out);
				continue;
			}
			
			final FileInputStream in = new FileInputStream(file);
			final Path filePath = file.toPath();
			String zipFilePath = null;
			
			if (file.getAbsolutePath().contains(resourceFilePath.toString()) == false) {
				final String currentFileName = file.getName();

				final String fileName;
				if (currentFileName.startsWith("style")) {
					fileName = "style.json";
				} else {
					fileName = "network.json";
				}
				final Path dataFilePath = Paths.get(FOLDER_NAME, fileName);
				zipFilePath = dataFilePath.toString();
			} else {
				final Path relPath = resourceFilePath.relativize(filePath);
				Path newResourceFilePath = Paths.get(FOLDER_NAME, relPath.toString());
				zipFilePath = newResourceFilePath.toString();
			}
			
			
			// This is for Windows System:  Replace file separator to slash.
			if (File.separatorChar != '/') {
				zipFilePath = zipFilePath.replace('\\', '/');
			}
			
			// Add normalized path name;
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
