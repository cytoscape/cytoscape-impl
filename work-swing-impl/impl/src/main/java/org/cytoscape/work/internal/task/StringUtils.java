
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.work.internal.task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Misc Utilities for Formatting Times and Strings.
 *
 * @author Ethan Cerami
 */
class StringUtils {
	/**
	 * Not Available String.
	 */
	public static final String NOT_AVAILABLE_STR = "N/A";

	/**
	 * String Length.
	 */
	public static final int STR_LENGTH = 60;

	/**
	 * Given a time value in milliseconds, this method returns a human
	 * readable representation.
	 * Here are a few examples:
	 * <P>
	 * <UL>
	 * <LI>If time is negative, the human readable representation is "N/A".
	 * <LI>If time is 3000, the human readable representation is "00:03".
	 * <LI>If time is 300000, the human readable representation is "05:00".
	 * <LI>If time is 302000, the human readable representation is "05:02".
	 * <LI>If time is 3601000, the human readable representation is
	 * "01:00:01".
	 * </UL>
	 *
	 * @param time Time in millseconds.
	 * @return Human Readable Representation of Time.
	 */
	public static String getTimeString(long time) {
		if (time < 0) {
			return NOT_AVAILABLE_STR;
		} else {
			//  Use GMT Time Zone so that we start exactly at the
			//  Epoch:  midnight, GM, January 1st, 1970.
			SimpleDateFormat dateFormat;
			TimeZone timeZone = TimeZone.getTimeZone("GMT");
			Calendar calendar = Calendar.getInstance(timeZone);
			calendar.setTimeInMillis(time);

			if (calendar.get(Calendar.HOUR) >= 1) {
				dateFormat = new SimpleDateFormat("HH:mm:ss");
			} else {
				dateFormat = new SimpleDateFormat("mm:ss");
			}

			dateFormat.setCalendar(calendar);

			return dateFormat.format(calendar.getTime());
		}
	}

	/**
	 * Truncates a String to a specific length or pads it with extra spaces.
	 *
	 * @param str Original String.
	 * @return Trucated or Padded String.
	 */
	public static String truncateOrPadString(String str) {
		StringBuffer temp;

		if (str == null) {
			temp = new StringBuffer();
		} else {
			temp = new StringBuffer(str);
		}

		int diff = STR_LENGTH - temp.length();

		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				temp.append(" ");
			}

			return temp.toString();
		} else {
			return new String(temp.substring(0, STR_LENGTH) + "...");
		}
	}
}
