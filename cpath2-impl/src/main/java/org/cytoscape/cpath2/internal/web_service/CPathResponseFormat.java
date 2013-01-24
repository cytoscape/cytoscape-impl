package org.cytoscape.cpath2.internal.web_service;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

/**
 * CPath Response Format.
 */
public class CPathResponseFormat {
    private String format;

    /**
     * BioPAX Format.
     */
    public static CPathResponseFormat BIOPAX = new CPathResponseFormat ("biopax");

    /**
     * Simplified Binary SIF Format.
     */
    public static CPathResponseFormat BINARY_SIF = new CPathResponseFormat ("binary_sif");

    /**
     * Generic XML Format.
     */
    public static CPathResponseFormat GENERIC_XML = new CPathResponseFormat ("xml");

    /**
     * Gets the Proper cPath Response Format.
     * @param format    Format String.
     * @return          CPathResponseFormat Object.
     */
    public static CPathResponseFormat getResponseFormat (String format) {
        if (format.equals(BIOPAX.getFormatString())) {
            return BIOPAX;
        } else if (format.equals(BINARY_SIF.getFormatString())) {
            return BINARY_SIF;
        } else if (format.equals(GENERIC_XML.getFormatString())) {
            return GENERIC_XML;
        }
        else {
            throw new IllegalArgumentException ("Format:  " + format + " is not valid.");
        }
    }

    /**
     * Private Constructor.
     * @param format    Format String.
     */
    private CPathResponseFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the format string.
     * @return format string.
     */
    public String getFormatString() {
        return this.format;
    }
}
