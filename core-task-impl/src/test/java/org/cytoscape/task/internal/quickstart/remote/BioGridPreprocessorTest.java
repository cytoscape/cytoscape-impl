package org.cytoscape.task.internal.quickstart.remote;

import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.task.internal.quickstart.datasource.BioGridPreprocessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BioGridPreprocessorTest {

	@Mock
	CyProperty<Properties> properties;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(properties.getProperties()).thenReturn(new Properties());

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void bioGridPreprocessorTest() throws Exception {
		final File file = new File("src/test/resources/BIOGRID-ORGANISM-3.1.74.mitab.zip");
		final BioGridPreprocessor processor = new BioGridPreprocessor(properties);

		processor.setSource(file.toURI().toURL());
		processor.processFile();
	}

}
