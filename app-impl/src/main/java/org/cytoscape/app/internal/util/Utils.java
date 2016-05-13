package org.cytoscape.app.internal.util;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2016 The Cytoscape Consortium
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
	
	public static Window getWindowAncestor(final ActionEvent evt, final CySwingApplication swingApplication) {
		Window window = null;
		
		if (evt.getSource() instanceof JMenuItem) {
			if (swingApplication.getJMenuBar() != null)
				window = SwingUtilities.getWindowAncestor(swingApplication.getJMenuBar());
		} else if (evt.getSource() instanceof Component) {
			window = SwingUtilities.getWindowAncestor((Component) evt.getSource());
		}
		
		if (window == null)
			window = swingApplication.getJFrame();
		
		return window;
	}
}
