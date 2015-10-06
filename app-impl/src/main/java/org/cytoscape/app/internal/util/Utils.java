package org.cytoscape.app.internal.util;

import java.util.ArrayList;

public class Utils {

	/*
	 * dumbSplit splits a string on delimiter boundaries and doesn't try any
	 * cute optimizations that the Java split would. For example, two
	 * delimiters in a row are interpreted as delimiting an empty string.
	 * So, this function returns one string for each delimiter and the
	 * string that precedes any delimiter.
	 */
	public static String[] dumbSplit(String s, char delimiter) {
		if (s == null) {
			return null;
		}

		// Count the number of delimiters
		int delimiterCount = 0;
		int scanIndex = 0;
		while (scanIndex < s.length()) {
			int delimiterIndex = s.indexOf(delimiter, scanIndex);
			if (delimiterIndex >= 0) {
				delimiterCount++;
				scanIndex = delimiterIndex + 1;
			} else {
				scanIndex = s.length();
			}
		}

		String[] splitList = new String[delimiterCount + 1];
		scanIndex = 0;
		delimiterCount = 0;
		while (scanIndex < s.length()) {
			int delimiterIndex = s.indexOf(delimiter, scanIndex);
			if (delimiterIndex >= 0) {
				splitList[delimiterCount] = s.substring(scanIndex,
						delimiterIndex);
				delimiterCount++;
				scanIndex = delimiterIndex + 1;
			} else {
				break;
			}
		}
		splitList[delimiterCount] = s.substring(scanIndex);

		return splitList;
	}

	private static int pyIndexOfChar(String s, int fromIndex, char ch) {
		int index = s.indexOf(ch, fromIndex);
		return (index < 0 ? s.length() : index);
	}

	/*
	 * Returns the index of a given character in a string, skipping
	 * sequences that are quoted.
	 */
	private static int indexOfChar(String s, int startIndex, char delimiter) {
		int indexDelimiter = pyIndexOfChar(s, startIndex, delimiter);
		int indexQuote = pyIndexOfChar(s, startIndex, '"');
		while (indexQuote < indexDelimiter) {
			int indexCloseQuote = pyIndexOfChar(s, indexQuote + 1, '"');
			indexDelimiter = pyIndexOfChar(s, indexCloseQuote + 1,
					delimiter);
			indexQuote = pyIndexOfChar(s, indexDelimiter, '"');
		}
		return indexDelimiter;
	}

	/*
	 * Returns an array of strings separated by a particular delimiter. This
	 * function is sensitive to quoted strings, which are not split if they
	 * contain the delimiter.
	 */
	public static ArrayList<String> splitByChar(String packageList,
			char delimiter) {
		ArrayList<String> splitList = new ArrayList<String>();
		int index = 0;
		while (index < packageList.length()) {
			int nextIndex = indexOfChar(packageList, index, delimiter);
			splitList.add(packageList.substring(index, nextIndex));
			index = nextIndex + 1;
		}
		return splitList;
	}
}


