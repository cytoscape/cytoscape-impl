package org.cytoscape.app.internal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.application.CyVersion;

public class AppHelper {
	private static Pattern COMPAT_VERSION_REGEX = Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?([\\-\\.\\w]+)?$");
	
	public static boolean isCompatible(final CyVersion cyVer, final String compatibleCytoscapeVersions) {
		final String compatVersStr = compatibleCytoscapeVersions;
		final String[] compatVers = compatVersStr.split(",");
		for (final String compatVer : compatVers) {
			final Matcher matcher = COMPAT_VERSION_REGEX.matcher(compatVer);
			if (!matcher.matches())
				continue;
			final String majorStr = matcher.group(1);
			final int major = Integer.parseInt(majorStr);
			final String minorStr = matcher.group(2);
			final int minor = minorStr != null ? Integer.parseInt(minorStr) : 0;
			final String patchStr = matcher.group(3);
			final int patch = patchStr != null ? Integer.parseInt(patchStr) : 0;
			final String tagStr = matcher.group(4);
			if (cyVer.getMajorVersion() == major &&
					cyVer.getMinorVersion() >= minor &&
					cyVer.getBugFixVersion() >= patch) {
				return true;
			}
		}
		return false;
	}
}
