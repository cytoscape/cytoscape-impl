package org.cytoscape.tableimport.internal.util;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

// TODO Make it an enum
public class AttributeDataTypes {
	
    /**
     * This type corresponds to java.lang.Boolean.
     */
    public static final byte TYPE_BOOLEAN = 1;

    /**
     * This type corresponds to java.lang.Double.
     */
    public static final byte TYPE_FLOATING = 2;

    /**
     * This type corresponds to java.lang.Integer.
     */
    public static final byte TYPE_INTEGER = 3;

    /**
     * This type corresponds to java.lang.String.
     */
    public static final byte TYPE_STRING = 4;

    /**
     * This type corresponds to an attribute which has not been defined.
     */
    public static final byte TYPE_UNDEFINED = -1;


    /**
     * This type corresponds to a 'simple' list.
     * <P>
     * A 'simple' list is defined as follows:
     * <UL>
     * <LI>All items within the list are of the same type, and are chosen
     * from one of the following: <CODE>Boolean</CODE>, <CODE>Integer</CODE>,
     * <CODE>Double</CODE> or <CODE>String</CODE>.
     * </UL>
     */
    public static final byte TYPE_SIMPLE_LIST = -2;

    /**
     * This type corresponds to a 'simple' hash map.
     * <P>
     * A 'simple' map is defined as follows:
     * <UL>
     * <LI>All keys within the map are of type:  <CODE>String</CODE>.
     * <LI>All values within the map are of the same type, and are chosen
     * from one of the following: <CODE>Boolean</CODE>, <CODE>Integer</CODE>,
     * <CODE>Double</CODE> or <CODE>String</CODE>.
     * </UL>
     */
    public static final byte TYPE_SIMPLE_MAP = -3;

    /**
     * This type corresponds to a data structure of arbitrary complexity,
     * e.g. anything more complex than a 'simple' list or a 'simple' map.
     * <P>
     * For complete details, refer to the class comments, or
     * {@link CyAttributes#getMultiHashMap()}.
     */
    public static final byte TYPE_COMPLEX = -4;


}
