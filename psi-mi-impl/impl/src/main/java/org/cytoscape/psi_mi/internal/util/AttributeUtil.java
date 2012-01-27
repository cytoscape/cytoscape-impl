/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.psi_mi.internal.util;

import org.cytoscape.psi_mi.internal.model.Interaction;


/**
 * Misc Utility Class for Extracting / Manipulating Attributes and Attribute
 * Values.
 *
 * @author Ethan Cerami
 */
public class AttributeUtil {
	// TODO: This should come from model-api
	public static final String NODE_NAME_ATTR_LABEL = "name";

	private static int baitStatus = 0;

	/**
	 * Gets Interaction Attribute with specified key.
	 *
	 * @param interaction Interaction Object.
	 * @param key         Key.
	 * @return Attribute Value.
	 */
	public static String getAttribute(Interaction interaction, String key) {
		String value = (String) interaction.getAttribute(key);

		if (value == null) {
			value = "";
		}

		return value;
	}

	/**
	 * Appends a String to the specified Object.
	 *
	 * @param object Object (either a String or a String[]).
	 * @param value  String to append.
	 * @return Array of Strings.
	 */
	public static String[] appendString(Object object, String value) {
		String[] newValues = null;

		if (object instanceof String) {
			newValues = new String[2];
			newValues[0] = (String) object;
			newValues[1] = value;
		} else if (object instanceof String[]) {
			String[] strs = (String[]) object;
			newValues = new String[strs.length + 1];

			for (int i = 0; i < strs.length; i++) {
				newValues[i] = strs[i];
			}

			newValues[strs.length] = value;
		}

		return newValues;
	}

	/**
	 * Sets the Bait Status.
	 * @param s Bait Status.
	 */
	public static void setbaitStatus(int s) {
		baitStatus = s;
	}

	/**
	 * Gets the Bait Status.
	 * @return Bait Status.
	 */
	public static int getbaitStatus() {
		return baitStatus;
	}
}
