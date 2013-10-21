package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


public class ValidatedObjectAndEditString implements Comparable{
	private final Object validatedObject;
	private final String editString;
	private String errorText;

	public ValidatedObjectAndEditString(final Object validatedObject, final String editString,
	                                    final String errorText)
	{
		this.validatedObject = validatedObject;
		this.editString = editString;
		this.errorText = errorText;
	}

	public ValidatedObjectAndEditString(final Object validatedObject, final String editString) {
		this(validatedObject, editString, null);
	}

	public ValidatedObjectAndEditString(final Object validatedObject) {
		this(validatedObject, null);
	}

	public Object getValidatedObject() { return validatedObject; }
	public String getEditString() {
		if (editString != null)
			return editString;
		if (validatedObject != null)
			return validatedObject.toString();
		return "";
	}

	public void setErrorText(final String newErrorText) { errorText = newErrorText; }
	public String getErrorText() { return errorText; }

	@Override public String toString() {
		return "ValidatedObjectAndEditString: validatedObject=" + validatedObject
		       + ", editString=" + editString;
	}

    @Override
    public int compareTo(Object o) {
        ValidatedObjectAndEditString v = (ValidatedObjectAndEditString)o;
        try
        {
            Double d1 = Double.parseDouble(getEditString());
            Double d2 = Double.parseDouble(v.getEditString());
            return d1.compareTo(d2);

        }
        catch(NumberFormatException nfe)
        {
            return getEditString().compareTo(v.getEditString());
        }

    }


}
