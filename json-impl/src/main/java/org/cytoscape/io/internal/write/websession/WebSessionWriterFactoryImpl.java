package org.cytoscape.io.internal.write.websession;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.json.CytoscapeJsNetworkWriterFactory;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

/**
 * 
 * Task factory for web archive writer.
 * 
 */
public class WebSessionWriterFactoryImpl implements CyWriterFactory, CySessionWriterFactory {

	private static final String DEF_WEB_RESOURCE = "web.zip";
	private static final String WEB_RESOURCE_DIR_NAME = "web";

	private static final int BUFFER_SIZE = 4096;

	private final CyFileFilter filter;
	private final VizmapWriterFactory jsonStyleWriterFactory;
	private final VisualMappingManager vmm;
	private final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory;
	private final CyNetworkViewManager viewManager;
	private final CyApplicationConfiguration appConfig;

	public WebSessionWriterFactoryImpl(final VizmapWriterFactory jsonStyleWriterFactory,
			final VisualMappingManager vmm, final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory,
			final CyNetworkViewManager viewManager, final CyFileFilter filter,
			final CyApplicationConfiguration appConfig) {

		this.jsonStyleWriterFactory = jsonStyleWriterFactory;
		this.vmm = vmm;
		this.cytoscapejsWriterFactory = cytoscapejsWriterFactory;
		this.viewManager = viewManager;
		this.filter = filter;
		this.appConfig = appConfig;

		try {
			extractDefault();
		} catch (IOException e) {
			throw new IllegalStateException("Could not create web resource files.", e);
		}
	}

	private final void extractDefault() throws IOException {
		final URL source = this.getClass().getClassLoader().getResource(DEF_WEB_RESOURCE);

		final File configLocation = this.appConfig.getConfigurationDirectoryLocation();
		final File webResource = new File(configLocation, WEB_RESOURCE_DIR_NAME);
		if (!webResource.exists() || !webResource.isDirectory()) {
			// Extract default resource
			extractResources(source, configLocation);
		}
	}

	public void extractResources(final URL source, final File destDir) throws IOException {
		destDir.mkdir();
		final ZipInputStream zipIn = new ZipInputStream(source.openStream());

		ZipEntry entry = zipIn.getNextEntry();
		while (entry != null) {
			final String filePath = destDir.getPath() + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				unzipFile(zipIn, filePath);
			} else {
				final File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	private final void unzipFile(final ZipInputStream zis, final String filePath) throws IOException {
		final byte[] buffer = new byte[BUFFER_SIZE];
		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		
		int read = 0;
		while ((read = zis.read(buffer)) != -1) {
			bos.write(buffer, 0, read);
		}
		bos.close();
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CySession session) {
		return new WebSessionWriterImpl(outputStream, jsonStyleWriterFactory, vmm, cytoscapejsWriterFactory,
				viewManager, appConfig);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}