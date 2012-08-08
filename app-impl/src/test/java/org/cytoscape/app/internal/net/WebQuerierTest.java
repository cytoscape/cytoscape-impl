package org.cytoscape.app.internal.net;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WebQuerierTest {

	@Test
	public void testCheckUpdates() {

	}
	
	@Test
	public void testCompareVersions() {
		WebQuerier webQuerier = new WebQuerier(null);
		
		// <0 = first is newer, >0 = first is older
		assertTrue(webQuerier.compareVersions("3.0.0", "3.0.0") == 0);
		assertTrue(webQuerier.compareVersions("3.0.0-tag", "3.0.0") == 0);
		assertTrue(webQuerier.compareVersions("3.0.0-tag1", "3.0.0-tag2") == 0);
		
		assertTrue(webQuerier.compareVersions("3.0.0-tag1", "3.0.1-tag2") > 0);
		assertTrue(webQuerier.compareVersions("3.0.1-tag1", "3.0.0-tag2") < 0);
		assertTrue(webQuerier.compareVersions("3.0.1-tag1", "3.1.0-tag2") > 0);
		
		assertTrue(webQuerier.compareVersions("3.0", "3.0.0") > 0);
		assertTrue(webQuerier.compareVersions("3.0-tag", "3.0.0") > 0);
		assertTrue(webQuerier.compareVersions("3.1", "3.0.0") < 0);

		assertTrue(webQuerier.compareVersions("3", "3.0.0") > 0);
		assertTrue(webQuerier.compareVersions("3.1", "") == 0);
		assertTrue(webQuerier.compareVersions("", "3.1") == 0);
		assertTrue(webQuerier.compareVersions("", "") == 0);
	}
	
	@Test
	public void testFail() {
		//assertTrue(false);
	}
}
