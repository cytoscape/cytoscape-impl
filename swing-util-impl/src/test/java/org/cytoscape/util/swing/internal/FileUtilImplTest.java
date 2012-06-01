package org.cytoscape.util.swing.internal;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FileUtilImplTest {
	private FileUtilImpl fileUtil;
	
	@Mock private CyProperty<Properties> cyProperty;
	
	private Properties props = new Properties();
	
	

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(cyProperty.getProperties()).thenReturn(props);
		
		fileUtil = new FileUtilImpl(cyProperty);
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
