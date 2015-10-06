package org.cytoscape.app.internal.util;

public class MiniTest {
	public static void eq(String arg1, String arg2) {
		isTrue(arg1.equals(arg2), arg1 + " == " + arg2);
	}

	public static void isTrue(boolean test, String testName) {
		if (test) {
			System.out.println(testName + " PASSED");
		} else {
			System.out.println(testName + " FAILED");
		}
	}
}

