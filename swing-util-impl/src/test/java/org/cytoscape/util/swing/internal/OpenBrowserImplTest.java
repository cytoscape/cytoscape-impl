package org.cytoscape.util.swing.internal;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
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

import static org.junit.Assert.assertFalse;

import java.awt.Desktop;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class OpenBrowserImplTest {
	@Mock private CyServiceRegistrar serviceRegistrar;
	@Mock private CyProperty<Properties> cyProperty;
	OpenBrowser openBrowser;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(serviceRegistrar.getService(CyProperty.class)).thenReturn(cyProperty);

		openBrowser = new OpenBrowserImpl(serviceRegistrar);
	}


	
	@Test(expected=java.lang.IllegalArgumentException.class)
	public void testOpenURL() {
		Assume.assumeTrue(Desktop.isDesktopSupported());

		// Invalid URL
		assertFalse(openBrowser.openURL("123 @#$ ww ?*  cyto"));
		
		// Warning: This actually opens web browser!
		//assertTrue(openBrowser.openURL("http://www.cytoscape.org/"));
	}

}
