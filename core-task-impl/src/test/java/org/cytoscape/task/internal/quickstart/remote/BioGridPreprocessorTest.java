package org.cytoscape.task.internal.quickstart.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.internal.quickstart.datasource.BioGridPreprocessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class BioGridPreprocessorTest {
	
	private static final String OUTPUT_DIR = "target/testoutput";

	@Mock
	CyProperty<Properties> properties;
	
	@Mock
	CyApplicationConfiguration config;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(properties.getProperties()).thenReturn(new Properties());
		final File sampleDirectory = new File(OUTPUT_DIR);
		if(sampleDirectory.isDirectory() == false)
			assertTrue(sampleDirectory.mkdir());
		
		when(config.getSettingLocation()).thenReturn(sampleDirectory);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void bioGridPreprocessorTest() throws Exception {
		final File file = new File("src/test/resources/BIOGRID-ORGANISM-3.1.74.mitab.zip");
		final BioGridPreprocessor processor = new BioGridPreprocessor(properties, config);

		processor.setSource(file.toURI().toURL());
		processor.processFile();
		
		File testDir = new File(OUTPUT_DIR, "interactions");
		assertTrue(testDir.exists());
		File[] fileList = testDir.listFiles();
		assertNotNull(fileList);
		
		// 23 species specific files should be created.
		assertEquals(23, fileList.length);
	}

}
