package org.cytoscape.util.swing.internal;

import static org.junit.Assert.assertFalse;

import java.awt.Desktop;

import org.cytoscape.util.swing.OpenBrowser;
import org.junit.Assume;
import org.junit.Test;

public class OpenBrowserImplTest {

	OpenBrowser openBrowser = new OpenBrowserImpl();

	
	@Test
	public void testOpenURL() {
		Assume.assumeTrue(Desktop.isDesktopSupported());

		// Invalid URL
		assertFalse(openBrowser.openURL("123 @#$ ww ?*  cyto"));
		
		// Warning: This actually opens web browser!
		//assertTrue(openBrowser.openURL("http://www.cytoscape.org/"));
	}

}
