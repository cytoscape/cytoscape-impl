package org.cytoscape.browser.internal.util;

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


public class ValidatedObjectAndEditString implements Comparable<Object> {
	
	private final Object validatedObject;
	private final String editString;
	private String errorText;
	private boolean isEquation;

	public ValidatedObjectAndEditString(final Object validatedObject, final String editString,
	                                    final String errorText, final boolean isEquation) {
		this.validatedObject = validatedObject;
		this.editString = editString;
		this.errorText = errorText;
		this.isEquation = isEquation;
	}

	public ValidatedObjectAndEditString(final Object validatedObject, final String editString,
			final boolean isEquation) {
		this(validatedObject, editString, null, isEquation);
	}

	public ValidatedObjectAndEditString(final Object validatedObject) {
		this(validatedObject, null, false);
	}

	public Object getValidatedObject() {
		return validatedObject;
	}
	
	public String getEditString() {
		if (editString != null)
			return editString;
		if (validatedObject != null)
			return validatedObject.toString();
		
		return "";
	}

	public void setErrorText(final String newErrorText) {
		errorText = newErrorText;
	}
	
	public String getErrorText() {
		return errorText;
	}

	public void setEquation(boolean isEquation) {
		this.isEquation = isEquation;
	}
	
	public boolean isEquation() {
		return isEquation;
	}
	
	@Override public String toString() {
		return "ValidatedObjectAndEditString: validatedObject=" + validatedObject + ", editString=" + editString;
	}

	@Override
	@SuppressWarnings("unchecked")
    public int compareTo(Object o) {
        ValidatedObjectAndEditString v = (ValidatedObjectAndEditString) o;
        
        if (validatedObject instanceof Comparable && v.validatedObject instanceof Comparable) {
            Comparable<Object> c1 = (Comparable<Object>) validatedObject;
            Comparable<Object> c2 = (Comparable<Object>) v.validatedObject;
            
            return c1.compareTo(c2);
        } else {
            return getEditString().compareTo(v.getEditString());
        }
    }
}
