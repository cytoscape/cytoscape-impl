package org.cytoscape.io.internal.write.websession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
		final File webResourceFiles = new File(webResourceDirectory, WEB_RESOURCE_NAME + "/"
				+ WebSessionWriterFactoryImpl.SIMPLE_EXPORT);
		files.add(webResourceFiles);
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
			final FileInputStream in = new FileInputStream(file.getAbsolutePath());

			if (file.getAbsolutePath().contains(webResourceDirectory.getAbsolutePath() + WEB_RESOURCE_NAME) == false) {
				final String currentFuileName = file.getName();

				final String fileName;
				if (currentFuileName.startsWith("style")) {
					fileName = "style.json";
				} else {
					fileName = "network.json";
				}
				final String newFileName = FOLDER_NAME + fileName;
				out.putNextEntry(new ZipEntry(newFileName));
			} else {
				final String newFileName = file.getAbsolutePath().replace(
						webResourceDirectory.getAbsolutePath() + WEB_RESOURCE_NAME + "/" + exportType, "");
				out.putNextEntry(new ZipEntry(FOLDER_NAME + newFileName));
			}

			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			out.closeEntry();
			in.close();
		}
	}
}
