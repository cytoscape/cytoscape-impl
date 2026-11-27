package org.cytoscape.internal.view.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.KeyboardFocusManager;
import java.awt.Window;

import org.junit.Test;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2026 The Cytoscape Consortium
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

public class ViewUtilTest {

	@Test
	public void testCreateDefaultIconText() {
		assertEquals("cC", ViewUtil.createDefaultIconText("camelCase"));
		assertEquals("cC", ViewUtil.createDefaultIconText("c4melCas3"));
		assertEquals("4C", ViewUtil.createDefaultIconText("4melCase"));
		assertEquals("PC", ViewUtil.createDefaultIconText("PascalCase"));
		assertEquals("PC", ViewUtil.createDefaultIconText("P4sc4lC4s3"));
		assertEquals("T", ViewUtil.createDefaultIconText("Title"));
		assertEquals("l", ViewUtil.createDefaultIconText("lowercase"));
		assertEquals("U", ViewUtil.createDefaultIconText("UPERCASE"));
		assertEquals("ST", ViewUtil.createDefaultIconText("Short Title"));
		assertEquals("ST", ViewUtil.createDefaultIconText("SHORT TITLE"));
		assertEquals("st", ViewUtil.createDefaultIconText("short title"));
		assertEquals("ML", ViewUtil.createDefaultIconText("My Long Title"));
		assertEquals("MS", ViewUtil.createDefaultIconText("My Super, Very Long Title"));
	}

	@Test
	public void testCanBringToFront() {
		var window = mock(Window.class);
		var otherWindow = mock(Window.class);

		// Cytoscape is the active app and another Cytoscape window is active
		assertTrue(ViewUtil.canBringToFront(window, otherWindow));
		// Window is already the active one
		assertFalse(ViewUtil.canBringToFront(window, window));
		// Cytoscape is not the active application (e.g. user working on another app while cyREST is used)
		assertFalse(ViewUtil.canBringToFront(window, null));
		// No window
		assertFalse(ViewUtil.canBringToFront(null, otherWindow));
		assertFalse(ViewUtil.canBringToFront(null, null));
	}

	@Test
	public void testToFrontIfAppActive_NotCalledWhenAppIsInactive() {
		// No Cytoscape window is active while running the tests, which is the same as having another app in the front
		assumeTrue(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() == null);

		var window = mock(Window.class);

		assertFalse(ViewUtil.toFrontIfAppActive(window));
		verify(window, never()).toFront();
	}

	@Test
	public void testToFrontIfAppActive_NullWindow() {
		assertFalse(ViewUtil.toFrontIfAppActive(null));
	}
}
