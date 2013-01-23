package org.cytoscape.task.internal.export;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */



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
