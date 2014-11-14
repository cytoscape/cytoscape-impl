package org.cytoscape.io.internal.write.json;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsNetworkModule;
import org.cytoscape.io.internal.write.websession.WebSessionWriterFactoryImpl;
import org.cytoscape.io.internal.write.websession.WebSessionWriterImpl;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSessionWriterTest {

	private WebSessionWriterImpl writer;

	private CyApplicationConfiguration appConfig;
	private VisualMappingManager vmm;

	private VizmapWriterFactory jsonStyleWriterFactory;
	private CyNetworkViewManager viewManager;
	private CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory;
	private TaskMonitor tm;
	private CyNetworkView view;

	private CyApplicationManager appManager;

	public WebSessionWriterTest() throws Exception {

		final URL source = this.getClass().getClassLoader().getResource("web.zip");
		File destDir = new File("target");
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
		final byte[] buffer = new byte[4096];
		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));

		int read = 0;
		while ((read = zis.read(buffer)) != -1) {
			bos.write(buffer, 0, read);
		}
		bos.close();
	}

	@Before
	public void setUp() throws Exception {
		this.appConfig = mock(CyApplicationConfiguration.class);
		when(appConfig.getConfigurationDirectoryLocation()).thenReturn(new File("target"));

		this.appManager = mock(CyApplicationManager.class, RETURNS_DEEP_STUBS);
		this.vmm = mock(VisualMappingManager.class);
		this.viewManager = mock(CyNetworkViewManager.class);
		CytoscapeJsViewWriterTest test = new CytoscapeJsViewWriterTest();
		CyFileFilter filter = mock(CyFileFilter.class);
		final ObjectMapper cytoscapeJsMapper = new ObjectMapper();
		CyVersion cyVersion = mock(CyVersion.class);
		cytoscapeJsMapper.registerModule(new CytoscapeJsNetworkModule(cyVersion));
		this.cytoscapejsWriterFactory = new CytoscapeJsNetworkWriterFactory(filter, cytoscapeJsMapper);
		this.tm = mock(TaskMonitor.class);
		this.view = test.generateNetworkView();
		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(view);

		this.jsonStyleWriterFactory = new CytoscapeJsVisualStyleWriterFactory(filter, appManager, cyVersion);
		when(viewManager.getNetworkViewSet()).thenReturn(views);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFullExport() throws Exception {
		final String exportType = WebSessionWriterFactoryImpl.FULL_EXPORT;
		File temp = new File("target", "ws-full.zip");
		OutputStream os = new FileOutputStream(temp);

		final CustomGraphicsManager cgManager = mock(CustomGraphicsManager.class);
		DVisualLexicon lexicon = new DVisualLexicon(cgManager);
		when(appManager.getCurrentRenderingEngine().getVisualLexicon()).thenReturn(lexicon);
		this.writer = new WebSessionWriterImpl(os, exportType, jsonStyleWriterFactory, vmm, cytoscapejsWriterFactory,
				viewManager, appConfig);
		writer.run(tm);
		
		testZipArchiveContents(temp);
	}

	private final void testZipArchiveContents(final File sessionZip) throws Exception {
		ZipFile zipFile = new ZipFile(sessionZip);
		Enumeration<?> enumeration = zipFile.entries();
		
		boolean dataDirFlag = false;
		boolean filelistFlag = false;
		boolean indexFlag = false;
		
		
		while (enumeration.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) enumeration.nextElement();

			String name = entry.getName();
			long bytes = entry.getSize();
			assertTrue(name.startsWith("web_session"));
			
			if(name.startsWith("web_session/data")) {
				dataDirFlag = true;
			}
			if(name.equals("web_session/index.html")) {
				indexFlag = true;
			}
			if(name.startsWith("web_session/filelist.json")) {
				filelistFlag = true;
			}
			System.out.println(name + ", " + bytes);
		}
		zipFile.close();
		assertTrue(indexFlag);
		assertTrue(filelistFlag);
		assertTrue(dataDirFlag);
	}
}
