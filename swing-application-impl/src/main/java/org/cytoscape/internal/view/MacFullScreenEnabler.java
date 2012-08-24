package org.cytoscape.internal.view;

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
