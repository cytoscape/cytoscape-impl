package org.cytoscape.io.internal.write.websession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.cytoscape.application.CyApplicationConfiguration;
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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSessionWriterImpl extends AbstractTask implements CyWriter, WebSessionWriter {

	private static final Logger logger = LoggerFactory.getLogger(WebSessionWriterImpl.class);

	protected static final String FOLDER_NAME = "/web_session/";
	private static final String FILE_LIST_NAME = "filelist";
	
	protected static final String WEB_RESOURCE_NAME = "/web";

	protected File webResourceDirectory;
	
	protected ZipOutputStream zos;
	private TaskMonitor taskMonitor;

	protected final OutputStream outputStream;
	private final VizmapWriterFactory jsonStyleWriterFactory;
	private final VisualMappingManager vmm;
	private final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory;
	private final CyNetworkViewManager viewManager;

	private final Map<String, File> name2fileMap;
	
	protected final String exportType;
	

	public WebSessionWriterImpl(final OutputStream outputStream, final String exportType, final VizmapWriterFactory jsonStyleWriterFactory,
			final VisualMappingManager vmm, final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory,
			final CyNetworkViewManager viewManager, final CyApplicationConfiguration appConfig) {
		this.outputStream = outputStream;
		this.jsonStyleWriterFactory = jsonStyleWriterFactory;
		this.vmm = vmm;
		this.cytoscapejsWriterFactory = cytoscapejsWriterFactory;
		this.viewManager = viewManager;
		
		this.webResourceDirectory = appConfig.getConfigurationDirectoryLocation();
		this.name2fileMap = new HashMap<String, File>();
		this.exportType = exportType;

	}


	@Override
	public void run(TaskMonitor tm) throws Exception {
		this.taskMonitor = tm;
		try {
			tm.setProgress(0.1);
			tm.setTitle("Archiving into zip files");
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
	
	/**
	 * Prepare list of files to be used in the web page.
	 * 
	 * This is necessary because JavaScript cannot get contents of a directory
	 * because of security model.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 * 
	 */
	private final File prepareFileList(final Map<String, String> fileMap) throws JsonGenerationException, JsonMappingException, IOException {
		File tempFile = File.createTempFile(FILE_LIST_NAME, ".json");
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(tempFile, fileMap);
		
		return tempFile;
	}
	

	@Override
	public void writeFiles(TaskMonitor tm) throws Exception {
		// Phase 0: Prepare local temp files. This is necessary because
		// Jackson library forces to close the stream!

		// Phase 1: Write all network files as Cytoscape.js-style JSON
		tm.setProgress(0.1);
		tm.setStatusMessage("Saving networks as Cytoscape.js JSON...");
		final Set<CyNetworkView> netViews = viewManager.getNetworkViewSet();
		final Collection<File> files = createNetworkViewFiles(netViews);
		tm.setProgress(0.7);
		if (cancelled)
			return;

		// Phase 2: Write a Style JSON.
		tm.setStatusMessage("Saving Visual Styles as JSON...");
		File styleFile = createStyleFile();
		files.add(styleFile);
		tm.setProgress(0.9);
		
		// Phase 3: Prepare list of files
		name2fileMap.put("style", styleFile);
		
		final Map<String, String> nameMap = new HashMap<String, String>();
		for(String key: name2fileMap.keySet()) {
			nameMap.put(key, name2fileMap.get(key).getName());
		}
		final File fileList = prepareFileList(nameMap);
		files.add(fileList);
		
		// Phase 4: Zip everything
		final File webResourceFiles = new File(webResourceDirectory, WEB_RESOURCE_NAME + "/" + exportType);
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


	private void addDir(final File[] files,final ZipOutputStream out) throws IOException {
		final byte[] buffer = new byte[4096];

		for (final File file:files) {
			if (file.isDirectory()) {
				// Recursively add contents in the directory
				addDir(file.listFiles(), out);
				continue;
			}
			final FileInputStream in = new FileInputStream(file.getAbsolutePath());
			
			if(file.getName().startsWith(FILE_LIST_NAME)) {
				// Rename File List
				out.putNextEntry(new ZipEntry(FOLDER_NAME + "/" + FILE_LIST_NAME + ".json"));
			} else if(file.getAbsolutePath().contains(webResourceDirectory.getAbsolutePath() + WEB_RESOURCE_NAME) == false) {
				final String newFileName = FOLDER_NAME + "data/" + file.getName();
				out.putNextEntry(new ZipEntry(newFileName));
			} else {
				final String newFileName = file.getAbsolutePath().replace(webResourceDirectory.getAbsolutePath() + WEB_RESOURCE_NAME + "/" + exportType, "");
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


	/**
	 * Write a JSON file for Visual Styles.
	 * 
	 * @throws Exception
	 */
	protected final File createStyleFile() throws Exception {
		// Write all Styles into one JSON file.
		final Set<VisualStyle> styles = vmm.getAllVisualStyles();
		File styleFile = File.createTempFile("style_", ".json");
		CyWriter vizmapWriter = jsonStyleWriterFactory.createWriter(new FileOutputStream(styleFile), styles);
		vizmapWriter.run(taskMonitor);
		return styleFile;
	}


	protected Collection<File> createNetworkViewFiles(final Collection<CyNetworkView> netViews) throws Exception {
		if(netViews.isEmpty()) {
			throw new IllegalArgumentException("No network view.");
		}
		
		final Collection<File> networkFiles = new HashSet<File>();
		for (final CyNetworkView view : netViews) {
			if (cancelled)
				return networkFiles;

			final CyNetwork network = view.getModel();
			final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
			final Long networkSUID = network.getSUID();

			final String jsonFileName = networkName + "-" + networkSUID.toString();

			File tempFile = File.createTempFile(jsonFileName + "_", ".json");
			JSONNetworkViewWriter writer = (JSONNetworkViewWriter) cytoscapejsWriterFactory.createWriter(
					new FileOutputStream(tempFile), view);
			writer.run(taskMonitor);
			networkFiles.add(tempFile);
			name2fileMap.put(networkName, tempFile);
		}
		return networkFiles;
	}
}