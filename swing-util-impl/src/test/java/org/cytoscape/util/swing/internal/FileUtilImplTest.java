package org.cytoscape.util.swing.internal;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class FileUtilImplTest {
	
	private FileUtilImpl fileUtil;
	
	@Mock private CyApplicationManager applicationManager;
	@Mock private CyServiceRegistrar serviceRegistrar;
	

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		
		fileUtil = new FileUtilImpl(serviceRegistrar);
	}

	@Test
	public void testFileUtilImpl() {
		assertNotNull(fileUtil);
	}

//	@Test
//	public void testGetFileComponentStringIntCollectionOfFileChooserFilter() {
//		Component parent = null;
//		String title = test;
//		int load_save_custom;
//		Collection<FileChooserFilter> filters;
//		fileUtil.getFile(parent, title, load_save_custom, filters);
//	}
//
//	@Test
//	public void testGetFileComponentStringIntStringStringCollectionOfFileChooserFilter() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetFilesComponentStringIntCollectionOfFileChooserFilter() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetFilesComponentStringIntStringStringCollectionOfFileChooserFilter() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetFilesComponentStringIntStringStringBooleanCollectionOfFileChooserFilter() {
//		fail("Not yet implemented");
//	}

}
