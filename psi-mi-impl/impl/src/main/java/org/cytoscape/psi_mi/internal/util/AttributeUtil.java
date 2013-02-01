package org.cytoscape.psi_mi.internal.util;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
