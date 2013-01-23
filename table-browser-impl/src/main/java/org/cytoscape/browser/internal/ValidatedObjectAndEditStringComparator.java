package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2013 The Cytoscape Consortium
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


import java.util.Comparator;


public class ValidatedObjectAndEditStringComparator implements Comparator<ValidatedObjectAndEditString> {
	private final Class<?> internalColumnType;

	ValidatedObjectAndEditStringComparator(final Class<?> internalColumnType) {
		this.internalColumnType = internalColumnType;
	}

	@Override
	public int compare(final ValidatedObjectAndEditString v1, final ValidatedObjectAndEditString v2) {
		// Deal with ValidatedObjectAndEditString objects that must display an error message:
		final String errorText1 = v1.getErrorText();
		final String errorText2 = v2.getErrorText();
		if (errorText1 != null && errorText2 != null)
			return errorText1.compareToIgnoreCase(errorText2);
		if (errorText2 != null)
			return +1;
		if (errorText1 != null)
			return -1;

		final Object val1 = v1.getValidatedObject();
		final Object val2 = v2.getValidatedObject();

		if (internalColumnType == Double.class)
			return doubleCompare((double)(Double)val1, (double)(Double)val2);
		if (internalColumnType == Long.class)
			return longCompare((long)(Long)val1, (long)(Long)val2);
		if (internalColumnType == Integer.class)
			return integerCompare((int)(Integer)val1, (int)(Integer)val2);

		if (internalColumnType == Boolean.class)
			return booleanCompare((boolean)(Boolean)val1, (boolean)(Boolean)val2);

		return stringCompare(val1.toString(), val2.toString());
	}

	private static int doubleCompare(final double d1, final double d2) {
		if (d1 < d2)
			return -1;
		return d1 > d2 ? +1 : 0;
	}

	private static int longCompare(final long l1, final long l2) {
		if (l1 < l2)
			return -1;
		return l1 > l2 ? +1 : 0;
	}

	private static int integerCompare(final int i1, int i2) {
		if (i1 < i2)
			return -1;
		return i1 > i2 ? +1 : 0;
	}

	private static int booleanCompare(final boolean b1, final boolean b2) {
		if ((b1 && b2) || (!b1 && !b2))
			return 0;
		return b1 ? -1 : +1;
	}

	private static int stringCompare(final String s1, final String s2) {
		return s1.compareToIgnoreCase(s2);
	}

	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof ValidatedObjectAndEditStringComparator)
			&& ((ValidatedObjectAndEditStringComparator)obj).internalColumnType == internalColumnType;
	}
}
