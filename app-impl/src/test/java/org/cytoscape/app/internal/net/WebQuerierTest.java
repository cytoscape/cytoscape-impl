package org.cytoscape.app.internal.net;

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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.regex.Pattern;

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
		assertTrue(webQuerier.compareVersions("3.0.0.tag", "3.0.0") < 0);
		assertTrue(webQuerier.compareVersions("3.0.0.tag1", "3.0.0.tag2") > 0);
		
		assertTrue(webQuerier.compareVersions("3.0.0.tag1", "3.0.1.tag2") > 0);
		assertTrue(webQuerier.compareVersions("3.0.1.tag1", "3.0.0.tag2") < 0);
		assertTrue(webQuerier.compareVersions("3.0.1.tag1", "3.1.0.tag2") > 0);
		
		assertTrue(webQuerier.compareVersions("3.0", "3.0.0") == 0);
		assertTrue(webQuerier.compareVersions("3.1", "3.0.0") < 0);

		assertTrue(webQuerier.compareVersions("3", "3.0.0") == 0);
		
		assertTrue(webQuerier.compareVersions("1.7", "3.0.0.alpha9-SNAPSHOT") > 0);
	}
	
	@Test
	public void testFormatOutputFilename() {
		
		Pattern regex = WebQuerier.OUTPUT_FILENAME_DISALLOWED_CHARACTERS;
		
		assertEquals("test", regex.matcher("test").replaceAll(""));
		assertEquals("Test", regex.matcher("Test").replaceAll(""));
		
		assertEquals("Test123", regex.matcher("Test123").replaceAll(""));
		assertEquals("Test123.123", regex.matcher("Test123.123").replaceAll(""));
		assertEquals("Test", regex.matcher("Test/\\").replaceAll(""));
		assertEquals("Test", regex.matcher("Test@$(*&").replaceAll(""));
		assertEquals("Test.2", regex.matcher("Test@.2@").replaceAll(""));
		
	}
	
	@Test
	public void testFail() {
		//assertTrue(false);
	}
}
