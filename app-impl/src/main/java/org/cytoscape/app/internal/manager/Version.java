package org.cytoscape.app.internal.manager;

import org.cytoscape.app.internal.util.MiniTest;
import org.cytoscape.app.internal.util.Utils;

/*
 * Version class parses and compares versions. We assume a semantic version
 * of the form M.m.s-x where M is the major version, m is the minor version,
 * s is the step, and x is a modifier. M, m, and s must be integers, and x x
 * can be a string. Any any component is optional, but if it's missing, all
 * subsequent components must be missing, too. Examples of valid versions
 * are 4, 4.3, 4.3.2, and 4.3.2-alpha. If a component separator is present,
 * it must be followed by a valid component. Examples of invalid versions
 * are a, ., 1., 1..3, and 1.2.3-.
 */

class Version {
	static private final int UNDEFINED_INT = -1;
	static private final String UNDEFINED_STRING = null;

	int major = UNDEFINED_INT;
	int minor = UNDEFINED_INT;
	int step = UNDEFINED_INT;
	String modifier = UNDEFINED_STRING;

	public Version(String version) throws Exception {
		if (version != null) {
			version = version.trim();
			if (version.length() > 0) {
				String[] parts = Utils.dumbSplit(version, '.');
				if (parts.length > 3) {
					throw new Exception("Invalid version " + version);
				}
				if (parts.length > 0) {
					major = Integer.valueOf(parts[0]);
					if (parts.length > 1) {
						minor = Integer.valueOf(parts[1]);
						if (parts.length > 2) {
							String[] stepParts = Utils.dumbSplit(parts[2],
									'-');
							if (stepParts.length > 0) {
								step = Integer.valueOf(stepParts[0]);
								if (stepParts.length > 1) {
									modifier = stepParts[1];
									if (modifier.length() == 0) {
										throw new Exception(
												"Invalid modifier "
														+ version);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public boolean isDefined() {
		return major != UNDEFINED_INT;
	}

	/*
	 * Return 0 if versions match, -1 if the version argument is greater
	 * than this object, and +1 if the version argument is less. Note that
	 * we consider version 3 and 3.0 to be different versions, with 3 < 3.0.
	 * This isn't hard to change, and should be changed in the constructor.
	 */
	public int compare(Version version) {
		// Check major version cases
		if (major == UNDEFINED_INT && version.major == UNDEFINED_INT) {
			return 0;
		}
		if (major == UNDEFINED_INT) {
			return -1;
		}
		if (version.major == UNDEFINED_INT) {
			return 1;
		}
		if (major != version.major) {
			return (major - version.major)
					/ Math.abs(major - version.major);
		}

		// Check minor version cases
		if (minor == UNDEFINED_INT && version.minor == UNDEFINED_INT) {
			return 0;
		}
		if (minor == UNDEFINED_INT) {
			return -1;
		}
		if (version.minor == UNDEFINED_INT) {
			return 1;
		}
		if (minor != version.minor) {
			return (minor - version.minor)
					/ Math.abs(minor - version.minor);
		}

		// Check step cases
		if (step == UNDEFINED_INT && version.step == UNDEFINED_INT) {
			return 0;
		}
		if (step == UNDEFINED_INT) {
			return -1;
		}
		if (version.step == UNDEFINED_INT) {
			return 1;
		}
		if (step != version.step) {
			return (step - version.step) / Math.abs(step - version.step);
		}

		// Check modifier cases
		if (modifier == UNDEFINED_STRING
				&& version.modifier == UNDEFINED_STRING) {
			return 0;
		}
		if (modifier == UNDEFINED_STRING) {
			return -1;
		}
		if (version.modifier == UNDEFINED_STRING) {
			return 1;
		}
		return modifier.compareTo(version.modifier);
	}

	public String toString() {
		String val = "";
		if (major != UNDEFINED_INT) {
			val = String.valueOf(major);
			if (minor != UNDEFINED_INT) {
				val += "." + String.valueOf(minor);
				if (step != UNDEFINED_INT) {
					val += "." + String.valueOf(step);
					if (modifier != UNDEFINED_STRING) {
						val += "-" + modifier;
					}
				}
			}
		}
		return val;
	}

	public static void main(String[] args) {
		System.out.println("Tester for class Version");

		// Things that should work
		try {
			// Test formation of Version object
			MiniTest.eq((new Version(null)).toString(), "");
			MiniTest.eq((new Version("")).toString(), "");
			MiniTest.eq((new Version("1")).toString(), "1");
			MiniTest.eq((new Version("1.2")).toString(), "1.2");
			MiniTest.eq((new Version("1.2.3")).toString(), "1.2.3");
			MiniTest.eq((new Version("1.2.3-alpha")).toString(),
					"1.2.3-alpha");

			// Test = comparison of Version objects
			MiniTest.isTrue(
					(new Version("1")).compare(new Version("1")) == 0,
					"1 == 1");
			MiniTest.isTrue(
					(new Version("1.2")).compare(new Version("1.2")) == 0,
					"1.2 == 1.2");
			MiniTest.isTrue(
					(new Version("1.2.3")).compare(new Version("1.2.3")) == 0,
					"1.2.3 == 1.2.3");
			MiniTest.isTrue((new Version("1.2.3-alpha"))
					.compare(new Version("1.2.3-alpha")) == 0,
					"1.2.3-alpha == 1.2.3-alpha");

			// Test <> comparison of Version objects with equal numbers of
			// components
			MiniTest.isTrue(
					(new Version("1")).compare(new Version("2")) < 0,
					"1 < 2");
			MiniTest.isTrue(
					(new Version("2")).compare(new Version("1")) > 0,
					"2 > 1");
			MiniTest.isTrue(
					(new Version("1.1")).compare(new Version("1.2")) < 0,
					"1.1 < 1.2");
			MiniTest.isTrue(
					(new Version("1.2")).compare(new Version("1.1")) > 0,
					"1.2 > 1.1");
			MiniTest.isTrue(
					(new Version("1.1.1")).compare(new Version("1.1.2")) < 0,
					"1.1.1 < 1.1.2");
			MiniTest.isTrue(
					(new Version("1.1.2")).compare(new Version("1.1.1")) > 0,
					"1.1.2 > 1.1.1");
			MiniTest.isTrue((new Version("1.1.1-alpha"))
					.compare(new Version("1.1.1-beta")) < 0,
					"1.1.1-alpha < 1.1.2-beta");
			MiniTest.isTrue((new Version("1.1.1-beta"))
					.compare(new Version("1.1.1-alpha")) > 0,
					"1.1.1-beta > 1.1.1-alpha");

			// Test <> comparison of Version objects with unequal numbers of
			// components
			MiniTest.isTrue(
					(new Version("1")).compare(new Version("1.2")) < 0,
					"1 < 1.2");
			MiniTest.isTrue(
					(new Version("1.2")).compare(new Version("1")) > 0,
					"1.2 > 1");
			MiniTest.isTrue(
					(new Version("1.2")).compare(new Version("1.2.3")) < 0,
					"1.2 < 1.2.3");
			MiniTest.isTrue(
					(new Version("1.2.3")).compare(new Version("1.2")) > 0,
					"1.2.3 > 1.2");
			MiniTest.isTrue((new Version("1.2.3")).compare(new Version(
					"1.2.3-alpha")) < 0, "1.2.3 < 1.2.3-alpha");
			MiniTest.isTrue((new Version("1.2.3-alpha"))
					.compare(new Version("1.2.3")) > 0,
					"1.2.3-alpha > 1.2.3");

		} catch (Throwable t) {
			System.out.println("Should not have seen exception " + t);
		}

		// Things that should not work
		try {
			new Version("a");
			System.out.println("a exception test FAILED");
		} catch (Throwable t) {
			System.out.println("a exception test PASSED");
		}

		try {
			new Version(".");
			System.out.println(". exception test FAILED");
		} catch (Throwable t) {
			System.out.println(". exception test PASSED");
		}

		try {
			new Version("1.");
			System.out.println("1. exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1. exception test PASSED");
		}

		try {
			new Version("1..");
			System.out.println("1.. exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.. exception test PASSED");
		}

		try {
			new Version("1.a.");
			System.out.println("1.a. exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.a. exception test PASSED");
		}

		try {
			new Version("1.2.");
			System.out.println("1.2. exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.2. exception test PASSED");
		}

		try {
			new Version("1.2..");
			System.out.println("1.2.. exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.2.. exception test PASSED");
		}

		try {
			new Version("1.2.a.");
			System.out.println("1.2.a. exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.2.a. exception test PASSED");
		}

		try {
			new Version("1.2.3.");
			System.out.println("1.2.3. exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.2.3. exception test PASSED");
		}

		try {
			new Version("1.2.3-");
			System.out.println("1.2.3- exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.2.3- exception test PASSED");
		}

		try {
			new Version("1.2.3+alpha");
			System.out.println("1.2.3+alpha exception test FAILED");
		} catch (Throwable t) {
			System.out.println("1.2.3+alpha exception test PASSED");
		}
	}
}


