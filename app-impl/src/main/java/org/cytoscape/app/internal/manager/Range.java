package org.cytoscape.app.internal.manager;

import org.cytoscape.app.internal.util.MiniTest;
import org.cytoscape.app.internal.util.Utils;

class Range {
	private static final char VERSION_DELIMITER = ',';
	private static final char HARD_START = '[';
	private static final char SOFT_START = '(';
	private static final char HARD_END = ']';
	private static final char SOFT_END = ')';

	private Version lower;
	private boolean lowerInclusive;
	private Version upper;
	private boolean upperInclusive;

	private Range(Version lower, boolean lowerInclusive, Version upper,
			boolean upperInclusive) {
		setRange(lower, lowerInclusive, upper, upperInclusive);
	}

	/*
	 * Parse a range that can be in several formats. If v is a version, we
	 * allow "v", "v,v", ",v", and "v,". The ends can have brackets or
	 * parens (e.g., "[3.0.1,4)". The simplest version is just "v", which
	 * means exactly version v and is the same as "[v]". "v," means a lower
	 * bound of "v" with no upper bound.
	 */
	public Range(String range) throws Exception {
		range = (range == null) ? null : range.trim();
		if (range == null) {
			setRange(new Version(null), true, new Version(null), true);
		} else {
			String[] rangeList = Utils.dumbSplit(range, VERSION_DELIMITER);
			if (rangeList.length > 2) {
				throw new Exception("Invalid range " + range);
			}

			String startVersion = rangeList[0];
			char startBound = HARD_START;
			String endVersion = rangeList[rangeList.length - 1];
			char endBound = HARD_END;

			if (startVersion.length() > 0) {
				startBound = startVersion.charAt(0);
				if (startBound == HARD_START || startBound == SOFT_START) {
					startVersion = startVersion.substring(1);
				} else {
					startBound = HARD_START;
				}
			}

			if (endVersion.length() > 0) {
				endBound = endVersion.charAt(endVersion.length() - 1);
				if (endBound == HARD_END || endBound == SOFT_END) {
					endVersion = endVersion.substring(0,
							endVersion.length() - 1);
				} else {
					endBound = HARD_END;
				}
			}

			setRange(new Version(startVersion),
					this.lowerInclusive = startBound == HARD_START,
					this.upper = new Version(endVersion),
					this.upperInclusive = endBound == HARD_END);
		}
	}

	private void setRange(Version lower, boolean lowerInclusive,
			Version upper, boolean upperInclusive) {
		this.lower = lower;
		this.lowerInclusive = lowerInclusive;
		this.upper = upper;
		this.upperInclusive = upperInclusive;
	}

	public boolean equals(Range r) {
		return equals(r.lower, r.lowerInclusive, r.upper, r.upperInclusive);
	}

	public boolean equals(Version lower, boolean lowerInclusive,
			Version upper, boolean upperInclusive) {
		return this.lower.compare(lower) == 0
				&& this.lowerInclusive == lowerInclusive
				&& this.upper.compare(upper) == 0
				&& this.upperInclusive == upperInclusive;
	}

	/*
	 * Returns true if version is properly between the lower and upper
	 * bounds of this object. Note that if this object has no lower bound,
	 * the version automatically passes ... likewise with the upper bound.
	 */
	public boolean inRange(Version check) {
		if (check != null && check.isDefined()) {
			boolean aboveFloor = true;
			if (lower.isDefined()) {
				int compareFloor = lower.compare(check);
				aboveFloor = compareFloor < 0
						|| (compareFloor == 0 && lowerInclusive);
			}

			boolean belowCeiling = true;
			if (upper.isDefined()) {
				int compareCeiling = upper.compare(check);
				belowCeiling = compareCeiling > 0
						|| (compareCeiling == 0 && upperInclusive);
			}

			return aboveFloor && belowCeiling;
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
		System.out.println("Tester for class Range");

		try {
			// Test range parsing and creation
			MiniTest.isTrue((new Range(null)).equals(new Version(""), true,
					new Version(""), true), "null = open range");
			MiniTest.isTrue((new Range("")).equals(new Version(""), true,
					new Version(""), true), "\"\" = open range");
			MiniTest.isTrue((new Range("  ")).equals(new Version(""), true,
					new Version(""), true), "\"  \" = open range");
			MiniTest.isTrue((new Range("1")).equals(new Version("1"), true,
					new Version("1"), true), "1,1");
			MiniTest.isTrue((new Range("1,")).equals(new Version("1"),
					true, new Version(""), true), "1,open");
			MiniTest.isTrue((new Range(",1")).equals(new Version(""), true,
					new Version("1"), true), "open,1");
			MiniTest.isTrue((new Range("1,10")).equals(new Version("1"),
					true, new Version("10"), true), "1,10");
			MiniTest.isTrue((new Range("1.2.3-alpha,10.11.12-beta"))
					.equals(new Version("1.2.3-alpha"), true, new Version(
							"10.11.12-beta"), true),
					"1.2.3-alpha,10.11.12-beta");
			MiniTest.isTrue((new Range("[1.2.3-alpha,10.11.12-beta)"))
					.equals(new Version("1.2.3-alpha"), true, new Version(
							"10.11.12-beta"), false),
					"[1.2.3-alpha,10.11.12-beta)");
			MiniTest.isTrue((new Range("(1.2.3-alpha,10.11.12-beta]"))
					.equals(new Version("1.2.3-alpha"), false, new Version(
							"10.11.12-beta"), true),
					"(1.2.3-alpha,10.11.12-beta]");
			MiniTest.isTrue((new Range("(,10.11.12-beta]")).equals(
					new Version(""), false, new Version("10.11.12-beta"),
					true), "(,10.11.12-beta]");
			MiniTest.isTrue((new Range("(1.2.3-alpha,]")).equals(
					new Version("1.2.3-alpha"), false, new Version(""),
					true), "(1.2.3-alpha,]");

			try {
				new Range("1,2,3");
				System.out.println("1,2,3 exception test FAILED");
			} catch (Throwable t) {
				System.out.println("1,2,3 exception test PASSED");
			}

			try {
				new Range("junk");
				System.out.println("junk exception test FAILED");
			} catch (Throwable t) {
				System.out.println("junk exception test PASSED");
			}

			try {
				new Range("[junk]");
				System.out.println("[junk] exception test FAILED");
			} catch (Throwable t) {
				System.out.println("[junk] exception test PASSED");
			}

			// Test checking within a range
			MiniTest.isTrue(!(new Range(new Version("3"), true,
					new Version("4"), false)).inRange(null),
					"null in [3,4)");
			MiniTest.isTrue(!(new Range(new Version("3"), true,
					new Version("4"), false)).inRange(new Version("")),
					"undefined in [3,4)");
			MiniTest.isTrue((new Range(new Version("3"), true, new Version(
					"4"), false)).inRange(new Version("3")), "3 in [3,4)");
			MiniTest.isTrue(!(new Range(new Version("3"), false,
					new Version("4"), false)).inRange(new Version("3")),
					"3 in (3,4)");
			MiniTest.isTrue((new Range(new Version("3"), true, new Version(
					"4"), false)).inRange(new Version("3.1")),
					"3.1 in [3,4)");
			MiniTest.isTrue(!(new Range(new Version("3"), true,
					new Version("4"), false)).inRange(new Version("4")),
					"4 in (3,4)");
			MiniTest.isTrue((new Range(new Version("3"), true, new Version(
					"4"), true)).inRange(new Version("4")), "4 in (3,4]");
			MiniTest.isTrue(!(new Range(new Version("3"), true,
					new Version("4"), true)).inRange(new Version("2")),
					"2 in (3,4]");
			MiniTest.isTrue(!(new Range(new Version("3"), true,
					new Version("4"), true)).inRange(new Version("5")),
					"5 in (3,4]");
		} catch (Throwable t) {
			System.out.println("Should not see exception: " + t);
		}
	}
}


