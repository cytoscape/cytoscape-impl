package org.cytoscape.sbml.internal;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class SBMLFileFilterTest {
	private CyFileFilter filter;

	@Before
	public void setUp() {
		StreamUtil streamUtil = new StreamUtil() {
			
			@Override
			public URLConnection getURLConnection(URL source) throws IOException {
				return null;
			}
			
			@Override
			public InputStream getInputStream(URL source) throws IOException {
				return source.openStream();
			}
		};
		filter = new SBMLFileFilter("SBML", streamUtil);
	}
	
	@Test
	public void testAcceptSBMLLevel2() throws Exception {
		File file = new File("src/test/resources/BIOMD0000000003.xml");
		assertTrue(filter.accepts(new FileInputStream(file), DataCategory.NETWORK));
		assertTrue(filter.accepts(file.toURI(), DataCategory.NETWORK));
	}
}
