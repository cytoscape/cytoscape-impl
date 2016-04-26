package org.cytoscape.app.internal.ui.downloadsites;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import static org.cytoscape.property.CyProperty.SavePolicy.DO_NOT_SAVE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.cytoscape.model.SavePolicy;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DownloadSitesManagerTest {
	
	@Test
	public void testSaveLoad() {
		
		// Create new DownloadSitesManager
		Properties properties = new Properties();
		
		CyProperty<Properties> cyProperty = new SimpleCyProperty<>(
				"TestProperties", properties, properties.getClass(), DO_NOT_SAVE);
		
		DownloadSitesManager downloadSitesManager = new DownloadSitesManager(cyProperty);
		
		// Check initially empty, and loading empty list
		assertEquals(0, downloadSitesManager.getDownloadSites().size());
		downloadSitesManager.loadDownloadSites();
		assertEquals(0, downloadSitesManager.getDownloadSites().size());
		
		// Insert 1, save, check result
		DownloadSite downloadSite = new DownloadSite();
		downloadSite.setSiteName("test1");
		downloadSite.setSiteUrl("http://test1");
		downloadSitesManager.getDownloadSites().add(downloadSite);

		downloadSitesManager.saveDownloadSites();
		
		assertEquals(1, downloadSitesManager.getDownloadSites().size());
		assertEquals("1", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITES_COUNT_KEY));
		
		assertEquals("test1", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_NAME_KEY_PREFIX + "1"));
		assertEquals("http://test1", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_URL_KEY_PREFIX + "1"));
		
		assertNull(properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_NAME_KEY_PREFIX + "2"));
		assertNull(properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_URL_KEY_PREFIX + "2"));
		
		// Insert another, save, check
		downloadSite = new DownloadSite();
		downloadSite.setSiteName("test2");
		downloadSite.setSiteUrl("http://test2");
		downloadSitesManager.getDownloadSites().add(downloadSite);
		
		downloadSitesManager.saveDownloadSites();
	
		assertEquals(2, downloadSitesManager.getDownloadSites().size());
		assertEquals("2", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITES_COUNT_KEY));

		// Old saved value should still be there
		assertEquals("test1", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_NAME_KEY_PREFIX + "1"));
		assertEquals("http://test1", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_URL_KEY_PREFIX + "1"));
		
		// Values should be in order of insertion
		assertEquals("test2", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_NAME_KEY_PREFIX + "2"));
		assertEquals("http://test2", properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_URL_KEY_PREFIX + "2"));
		
		assertNull(properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_NAME_KEY_PREFIX + "3"));
		assertNull(properties.getProperty(DownloadSitesManager.DOWNLOAD_SITE_URL_KEY_PREFIX + "3"));
		
		// Test load
		downloadSitesManager.getDownloadSites().clear(); // Clear sites
		assertEquals(0, downloadSitesManager.getDownloadSites().size());
		
		assertTrue(downloadSitesManager.loadDownloadSites());
		assertEquals(2, downloadSitesManager.getDownloadSites().size());
		
		assertEquals("test1", downloadSitesManager.getDownloadSites().get(0).getSiteName());
		assertEquals("http://test1", downloadSitesManager.getDownloadSites().get(0).getSiteUrl());
		
		assertEquals("test2", downloadSitesManager.getDownloadSites().get(1).getSiteName());
		assertEquals("http://test2", downloadSitesManager.getDownloadSites().get(1).getSiteUrl());
	}
}
