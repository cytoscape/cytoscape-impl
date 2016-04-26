package org.cytoscape.app.internal.manager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

import org.cytoscape.app.internal.util.MiniTest;
import org.cytoscape.app.internal.util.Utils;

public class ParseAppDependencies {
	private static final char PACKAGE_DELIMITER = ',';
	private static final char ATTR_DELIMITER = ';';
	private static final char VALUE_DELIMITER = '=';
	private static final String PSEUDO_NAME = "#name";
	private static final String VERSION_ATTR = "version";

	/*
	 * Get a list of package entries. An entry is a list of attributes separated
	 * by ';', and the first attribute is assumed to be a name all by itself.
	 * All other attributes are assumed to be named and with/without values, and
	 * the value may be surrounded by quotes.
	 */
	private static ArrayList<String> splitByPkg(String packageList) {
		return Utils.splitByChar(packageList.trim(), PACKAGE_DELIMITER);
	}

	/*
	 * Build map of attributes for an app entry. Attributes are separated by
	 * ';', and the first attribute is assumed to be a name all by itself. The
	 * name is stored under the pseudo-attribute #name. All other attributes are
	 * assumed to be named and with/without values. If an attribute has no
	 * value, it's still mapped, but with a null value. If a value is surrounded
	 * by quotes, the quotes are stripped.
	 */
	private static AbstractMap<String, String> mapAppAttrs(String app)
			throws Exception {
		HashMap<String, String> attrMap = new HashMap<>();

		String[] attrList = Utils.dumbSplit(app, ATTR_DELIMITER);
		for (int index = 0; index < attrList.length; index++) {
			String attr = attrList[index];
			if (index == 0) {
				attrMap.put(PSEUDO_NAME, attr);
			} else {
				String attrName = attr;
				String attrValue = null;

				int indexDelimiter = attr.indexOf(VALUE_DELIMITER);
				if (indexDelimiter >= 0) {
					attrName = attr.substring(0, indexDelimiter);
					attrValue = attr.substring(indexDelimiter + 1).trim();
					if (attrValue.length() >= 2 && attrValue.startsWith("\"")
							&& attrValue.endsWith("\"")) {
						attrValue = attrValue.substring(1,
								attrValue.length() - 1);
					}
				}

				attrName = attrName.trim();

				if (attrName.length() == 0) {
					throw new Exception("Attribute missing name " + attr);
				}

				attrMap.put(attrName, attrValue);
			}
		}

		return attrMap;
	}

	/*
	 * Return a mapping between app name and version range. Note that if there
	 * is a version identifier but no range given, it's interpreted as
	 * "all versions".
	 */
	private static AbstractMap<String, Range> parseAppNameDependencies(
			String manifestList, String packageQualifier) throws Exception {
		AbstractMap<String, Range> appVersions = new HashMap<>();
		ArrayList<String> appList = splitByPkg(manifestList);
		for (String app : appList) {
			if (app.startsWith(packageQualifier)) {
				AbstractMap<String, String> attrs = mapAppAttrs(app);
				String appName = attrs.get(PSEUDO_NAME);
				if (appName != null && appName.length() > 0) {
					try {
						appVersions.put(appName,
								new Range(attrs.get(VERSION_ATTR)));
					} catch (Throwable t) {
						throw new Exception("for app " + appName + ":" + t);
					}
				} else {
					throw new Exception("no app name specified");
				}
			}
		}
		return appVersions;
	}

	/*
	 * Return true if the named version is within all app ranges. We assume that
	 * all apps have a range, even if it's the "all versions" range.
	 */
	private static boolean checkVersions(
			AbstractMap<String, Range> appVersions, Version checkVersion) {
		for (String app : appVersions.keySet()) {
			Range appRange = appVersions.get(app);
			if (!appRange.inRange(checkVersion)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Return true if the checkVersion is consistent with all modules in the
	 * manifest list. The moduleFilter determines which modules are examined. If
	 * it is something like org.cytoscape, only versions for cytoscape modules
	 * will be checked.
	 */
	public static boolean checkVersions(String manifestList,
			String moduleFilter, String checkVersion) throws Exception {
		AbstractMap<String, Range> cytoRanges = parseAppNameDependencies(
				manifestList, moduleFilter);

		return checkVersions(cytoRanges, new Version(checkVersion));
	}

	public static void main(String[] args) {
		try {
		} catch (Throwable t) {
			System.out.println("Should not have seen exception " + t);
		}

		String manifestExample = "javax.swing,org.cytoscape.application;version=\"[3.0,4)\","
				+ "org.cytoscape.diagnostics,org.cytoscape.io.util;version=\"[3.0,4)\","
				+ "org.cytoscape.service.util;version=\"[3.0,4)\","
				+ "org.cytoscape.work;version=\"[3.0,4)\",org.osgi.framework;version=\"[1.5,2)\""
				+ "javax.swing,org.cytoscape.application;version=\"[3.0,4)\","
				+ "org.cytoscape.diagnostics,org.cytoscape.io.util;version=\"[3.0,4)\","

				+ "org.cytoscape.service.util;version=\"[3.0,4)\","
				+ "org.cytoscape.work;version=\"[3.0,4)\","
				+ "org.osgi.framework;version=\"[1.5,2)\","
				+ "javax.swing,javax.swing.border,"
				+ "javax.swing.event,"
				+ "javax.swing.plaf,"
				+ "javax.swing.plaf."

				+ "basic,javax.swing.table,"
				+ "javax.swing.text,"
				+ "javax.swing.text.html,"
				+ "org.cytoscape.application;version=\"[3.2,4)\","
				+ "org.cytoscape.application.events;version=\"[3.2,4)\","
				+ "org.cytoscape.application.swing;version=\"[3.2,4)\","
				+ "org.cytoscape.application.swing.events;version=\"[3.2,4)\","
				+ "org.cytoscape.event;version=\"[3.2,4)\","
				+ "org.cytoscape.model;version=\"[3.2,4)\","
				+ "org.cytoscape.model.events;version=\"[3.2,4)\","
				+ "org.cytoscape.model.subnetwork;version=\"[3.2,4)\","
				+ "org.cytoscape.service.util;version=\"[3.2,4)\","
				+ "org.cytoscape.util.swing;version=\"[3.2,4)\","
				+ "org.cytoscape.view.model;version=\"[3.2,4)\","
				+ "org.cytoscape.view.presentation;version=\"[3.2,4)\","
				+ "org.cytoscape.view.presentation.property;version=\"[3.2,4)\","
				+ "org.cytoscape.view.presentation.property.values;version=\"[3.2,4)\","
				+ "org.cytoscape.view.vizmap;version=\"[3.2,4)\","
				+ "org.cytoscape.view.vizmap.mappings;version=\"[3.2,4)\","
				+ "org.cytoscape.work;version=\"[3.2,4)\","
				+ "org.osgi.framework;version=\"[1.5,2)\","
				+ "org.slf4j;version=\"[1.5,2)\","
				+ "javax.swing,javax.swing.border,"
				+ "javax.swing.event,"
				+ "javax.swing.plaf,"
				+ "javax.swing.plaf.basic,"
				+ "javax.swing.table,"
				+ "javax.swing.text,"
				+ "javax.swing.text.html,org."
				+ "cytoscape.application;version=\"[3.2,4)\","
				+ "org.cytoscape.application.events;version=\"[3.2,4)\","
				+ "org.cytoscape.application.swing;version=\"[3.2,4)\","
				+ "org.cytoscape.application.swing.events;version=\"[3.2,4)\","
				+ "org.cytoscape.event;version=\"[3.2,4)\","
				+ "org.cytoscape.model;version=\"[3.2,4)\","
				+ "org.cytoscape.model.events;version=\"[3.2,4)\","
				+ "org.cytoscape.model.subnetwork;version=\"[3.2,4)\","
				+ "org.cytoscape.service.util;version=\"[3.2,4)\","
				+ "org.cytoscape.util.swing;version=\"[3.2,4)\","
				+ "org.cytoscape.view.model;version=\"[3.2,4)\","
				+ "org.cytoscape.view.presentation;version=\"[3.2,4)\","
				+ "org.cytoscape.view.presentation.property;version=\"[3.2,4)\","
				+ "org.cytoscape.view.presentation.property.values;version=\"[3.2,4)\","
				+ "org.cytoscape.view.vizmap;version=\"[3.2,4)\","
				+ "org.cytoscape.view.vizmap.mappings;version=\"[3.2,4)\","
				+ "org.cytoscape.work;version=\"[3.2,4)\","
				+ "org.osgi.framework;version=\"[1.5,2)\","
				+ "org.slf4j;version=\"[1.5,2)\"";

		try {
			AbstractMap<String, Range> cytoRanges = parseAppNameDependencies(
					manifestExample, "org.cytoscape");

			MiniTest.isTrue(!checkVersions(cytoRanges, new Version("2.8.3")),
					"2.8.3 not in range");
			MiniTest.isTrue(!checkVersions(cytoRanges, new Version("3")),
					"3 not in range");
			MiniTest.isTrue(!checkVersions(cytoRanges, new Version("3.0")),
					"3.0 not in range");
			MiniTest.isTrue(checkVersions(cytoRanges, new Version("3.2.1")),
					"3.2.1 in range");
			MiniTest.isTrue(!checkVersions(cytoRanges, new Version("4")),
					"4 not in range");

			MiniTest.isTrue(
					!checkVersions(manifestExample, "org.cytoscape", "2.8.3"),
					"2.8.3 not in range");
			MiniTest.isTrue(
					checkVersions(manifestExample, "org.cytoscape", "3.2.1"),
					"3.2.1 in range");

			MiniTest.isTrue(
					checkVersions(manifestExample, "puretrash", "3.2.1"),
					"3.2.1 in range of empty app list");

			MiniTest.isTrue(
					checkVersions("", "org.cytoscape", "3.2.1"),
					"3.2.1 in range of empty manifest");

		} catch (Throwable t) {
			System.out.println(t);
			System.exit(-1);
		}
	}

}
