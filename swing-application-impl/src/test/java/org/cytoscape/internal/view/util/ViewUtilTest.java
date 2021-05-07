package org.cytoscape.internal.view.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
}
