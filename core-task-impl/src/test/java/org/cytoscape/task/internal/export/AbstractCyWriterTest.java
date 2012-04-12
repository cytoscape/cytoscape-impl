package org.cytoscape.task.internal.export;


import java.io.File;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.task.internal.export.AbstractCyWriter;

import static org.junit.Assert.*;
import org.junit.Test;


public abstract class AbstractCyWriterTest {
	protected AbstractCyWriter cyWriter;
	protected CyFileFilter fileFilter;

	@Test
	public void testOutputFile() {
		final File outputFile = new File("dummy");
		cyWriter.setOutputFile(outputFile);
		assertEquals(outputFile, cyWriter.getOutputFile());
	}

	@Test
	public void testGetWriter() throws Exception {
		final File outputFile = new File("dummy");
		assertNotNull(cyWriter.getWriter(fileFilter, outputFile));
	}
}
