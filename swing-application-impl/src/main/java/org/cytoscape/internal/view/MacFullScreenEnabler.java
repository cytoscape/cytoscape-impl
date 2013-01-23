package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import java.awt.Window;

import com.apple.eawt.FullScreenUtilities;

public class MacFullScreenEnabler {
	public static void setEnabled(Window window, boolean b) {
		FullScreenUtilities.setWindowCanFullScreen(window, true);
	}

	public static boolean supportsNativeFullScreenMode() {
		if (!System.getProperty("os.name").startsWith("Mac OS X")) {
			return false;
		}
		
		String[] parts = System.getProperty("os.version").split("[.]");
		int majorNumber = Integer.parseInt(parts[0]);
		
		int minorNumber = 0;
		if (parts.length > 1) {
			minorNumber = Integer.parseInt(parts[1]);
		}
		
		return majorNumber == 10 && minorNumber >= 7;
	}
}
