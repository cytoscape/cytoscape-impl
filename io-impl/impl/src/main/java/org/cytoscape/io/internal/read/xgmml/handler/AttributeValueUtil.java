package org.cytoscape.io.internal.read.xgmml.handler;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.io.internal.read.xgmml.MetadataEntries;
import org.cytoscape.io.internal.read.xgmml.MetadataParser;
import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.io.internal.util.xgmml.ObjectType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class AttributeValueUtil {

    static final String ATTR_NAME = "name";
    static final String ATTR_LABEL = "label";
    static final String ATTR_VALUE = "value";
    static final String LOCKED_VISUAL_PROPS = "lockedVisualProperties";
    
    static final Pattern XLINK_PATTERN = Pattern.compile(".*#(-?\\d+)");
    
    private Locator locator;

    private final ReadDataManager manager;
    private final ObjectTypeMap typeMap;
    
    protected static final Logger logger = LoggerFactory.getLogger(AttributeValueUtil.class);

    public AttributeValueUtil(final ReadDataManager manager) {
        this.manager = manager;
        this.typeMap = new ObjectTypeMap();
    }

    public void setLocator(Locator locator) {
        this.locator = locator;
    }
    
    public void setMetaData(CyNetwork network) {
        MetadataParser mdp = new MetadataParser(network);
        if (manager.RDFType != null) mdp.setMetadata(MetadataEntries.TYPE, manager.RDFType);
        if (manager.RDFDate != null) mdp.setMetadata(MetadataEntries.DATE, manager.RDFDate);
        if (manager.RDFTitle != null) mdp.setMetadata(MetadataEntries.TITLE, manager.RDFTitle);
        if (manager.RDFDescription != null) mdp.setMetadata(MetadataEntries.DESCRIPTION, manager.RDFDescription);
        if (manager.RDFSource != null) mdp.setMetadata(MetadataEntries.SOURCE, manager.RDFSource);
        if (manager.RDFFormat != null) mdp.setMetadata(MetadataEntries.FORMAT, manager.RDFFormat);
        if (manager.RDFIdentifier != null) mdp.setMetadata(MetadataEntries.IDENTIFIER, manager.RDFIdentifier);
    }

    /********************************************************************
     * Routines to handle attributes
     *******************************************************************/

    /**
     * Return the string attribute value for the attribute indicated by "key".
     * If no such attribute exists, return null. In particular, this routine
     * looks for an attribute with a <b>name</b> or <b>label</b> of <i>key</i>
     * and returns the <b>value</b> of that attribute.
     * 
     * @param atts
     *            the attributes
     * @param key
     *            the specific attribute to get
     * @return the value for "key" or null if no such attribute exists
     */
    protected String getAttributeValue(Attributes atts, String key) {
        String name = atts.getValue(ATTR_NAME);

        if (name == null) name = atts.getValue(ATTR_LABEL);

        if (name != null && name.equals(key))
            return atts.getValue(ATTR_VALUE);
        else
            return null;
    }

    /**
	 * Return the typed attribute value for the passed attribute. In this case, the caller has already determined that
	 * this is the correct attribute and we just lookup the value. This routine is responsible for type conversion
	 * consistent with the passed argument.
	 * 
	 * @param type the ObjectType of the value
	 * @param atts the attributes
	 * @param name the attribute name
	 * @return the value of the attribute in the appropriate type
	 */
    protected Object getTypedAttributeValue(ObjectType type, Attributes atts, String name) throws SAXParseException {
        String value = atts.getValue("value");

        try {
            return typeMap.getTypedValue(type, value, name);
        } catch (Exception e) {
            throw new SAXParseException("Unable to convert '" + value + "' to type " + type.toString(), locator);
        }
    }

    /**
     * Return the attribute value for the attribute indicated by "key". If no
     * such attribute exists, return null.
     * 
     * @param atts
     *            the attributes
     * @param key
     *            the specific attribute to get
     * @return the value for "key" or null if no such attribute exists
     */
    protected String getAttribute(Attributes atts, String key) {
        return atts.getValue(key);
    }

    /**
     * Return the attribute value for the attribute indicated by "key". If no
     * such attribute exists, return null.
     * 
     * @param atts
     *            the attributes
     * @param key
     *            the specific attribute to get
     * @param ns
     *            the namespace for the attribute we're interested in
     * @return the value for "key" or null if no such attribute exists
     */
    protected String getAttributeNS(Attributes atts, String key, String ns) {
        if (atts.getValue(ns, key) != null)
            return atts.getValue(ns, key);
        else
            return atts.getValue(key);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected ParseState handleAttribute(Attributes atts) throws SAXParseException {
    	ParseState parseState = ParseState.NONE;
    	
    	final String name = atts.getValue("name");
    	String type = atts.getValue("type");
    	String cyType = atts.getValue("cy:type");
    	
    	if ("has_nested_network".equals(name))
        	type = ObjectType.BOOLEAN.getXgmmlValue();
    	
    	final boolean isEquation = ObjectTypeMap.fromXGMMLBoolean(atts.getValue("cy:equation"));
    	final boolean isHidden = ObjectTypeMap.fromXGMMLBoolean(atts.getValue("cy:hidden"));
        
		final CyIdentifiable curElement = manager.getCurrentElement();
		CyNetwork curNet = manager.getCurrentNetwork();
        
		// This is necessary, because external edges of 2.x Groups may be written
		// under the group subgraph, but the edge will be created on the root-network only,
		if (curElement instanceof CyNode || curElement instanceof CyEdge) {
			boolean containsElement = (curElement instanceof CyNode && curNet.containsNode((CyNode) curElement));
			containsElement |= (curElement instanceof CyEdge && curNet.containsEdge((CyEdge) curElement));
			
			// So if the current network does not contain this element, the CyRootNetwork should contain it
			if (!containsElement)
				curNet = manager.getRootNetwork();
		}
		
		CyRow row = null;
		
		if (isHidden) {
			row = curNet.getRow(curElement, CyNetwork.HIDDEN_ATTRS);
		} else {
			// TODO: What are the rules here?
			// Node/edge attributes are always shared, except "selected"?
			// Network name must be local, right? What about other network attributes?
			if (CyNetwork.SELECTED.equals(name) || (curElement instanceof CyNetwork))
				row = curNet.getRow(curElement, CyNetwork.LOCAL_ATTRS);
			else
				row = curNet.getRow(curElement, CyNetwork.DEFAULT_ATTRS); // Will be created in the shared table
		}		
		
		CyTable table = row.getTable();
        CyColumn column = table.getColumn(name);
        
        if (column != null) {
        	// Check if it's a virtual column
        	// It's necessary because the source row may not exist yet, which would throw an exception
        	// when the value is set. Doing this forces the creation of the source row.
        	final VirtualColumnInfo info = column.getVirtualColumnInfo();
        	
        	if (info.isVirtual()) {
        		final CyTable srcTable = info.getSourceTable(); 
        		final CyColumn srcColumn = srcTable.getColumn(info.getSourceColumn());
        		final Class<?> jkColType = table.getColumn(info.getTargetJoinKey()).getType();
        		final Object jkValue = row.get(info.getTargetJoinKey(), jkColType);
        		final Collection<CyRow> srcRowList = srcTable.getMatchingRows(info.getSourceJoinKey(), jkValue);
        		final CyRow srcRow; 
        		
        		if (srcRowList == null || srcRowList.isEmpty()) {
        			if (info.getTargetJoinKey().equals(CyIdentifiable.SUID)) {
        				// Try to create the row
        				srcRow = srcTable.getRow(jkValue);
        			} else {
						logger.error("Unable to import virtual column \"" + name + "\": The source table \""
								+ srcTable.getTitle() + "\" does not have any matching rows for join key \""
								+ info.getSourceJoinKey() + "=" + jkValue + "\".");
	        			return parseState;
        			}
        		} else {
        			srcRow = srcRowList.iterator().next();
        		}
        		
        		// Use the source table instead
        		table = srcTable;
        		column = srcColumn;
        		row = srcRow;
        	}
        }
        
        Object value = null;
        final ObjectType objType = typeMap.fromXgmml(cyType, type);

        if (isEquation) {
        	// It is an equation...
        	String formula = atts.getValue("value");
        	
            if (name != null && formula != null) {
            	manager.addEquationString(row, name, formula);
            }
        } else {
        	// Regular attribute value...
        	value = getTypedAttributeValue(objType, atts, name);
        }

        switch (objType) {
			case BOOLEAN:
				if (name != null) setAttribute(row, name, Boolean.class, (Boolean) value);
				break;
			case REAL:
				if (name != null) {
					if (SUIDUpdater.isUpdatable(name))
						setAttribute(row, name, Long.class, (Long) value);
					else
						setAttribute(row, name, Double.class, (Double) value);
				}
				break;
			case INTEGER:
				if (name != null) {
					if (SUIDUpdater.isUpdatable(name))
						setAttribute(row, name, Long.class, (Long) value);
					else
						setAttribute(row, name, Integer.class, (Integer) value);
				}
				break;
			case LONG:
				if (name != null) setAttribute(row, name, Long.class, (Long) value);
				break;
			case STRING:
				if (name != null) setAttribute(row, name, String.class, (String) value);
				break;
			// We need to be *very* careful. Because we duplicate attributes for
			// each network we write out, we wind up reading and processing each
			// attribute multiple times, once for each network. This isn't a problem
			// for "base" attributes, but is a significant problem for attributes
			// like LIST and MAP where we add to the attribute as we parse. So, we
			// must make sure to clear out any existing values before we parse.
			case LIST:
				manager.currentAttributeID = name;
				manager.setCurrentRow(row);
				
				if (column != null && List.class.isAssignableFrom(column.getType()))
					row.set(name, null);
				
				if (column == null) {
					final String elementType = atts.getValue("cy:elementType"); // Since Cytoscape v3.3
					
					if (elementType != null) {
						final ObjectType elementObjType = typeMap.fromXgmml(elementType, null);
			        	final Class<?> clazz = typeMap.getClass(elementObjType, name);
			            
			        	table.createListColumn(name, clazz, false, new ArrayList());
			            column = row.getTable().getColumn(name);
			            
			            manager.listAttrHolder = new ArrayList<>();
					}
		        }
				
				return ParseState.LIST_ATT;
		}

        return parseState;
    }
    
    private <T> void setAttribute(final CyRow row, final String name, final Class<T> type, final T value) {
        if (name != null) {
            final CyTable table = row.getTable();
            CyColumn column = table.getColumn(name);
            
            if (column == null) {
            	table.createColumn(name, type, false);
            	column = table.getColumn(name);
            } else if (column.getVirtualColumnInfo().isVirtual()) {
            	logger.warn("Cannot set value to virtual column \"" + name + "\".");
            	return;
            }
            
            if (value != null) {
            	// Note: The actual column type may be different from the passed "type" argument,
            	//       if the column was not created when importing the XGMML
            	//       (i.e. merging with an existing CyRootNetwork)
            	final Class<?> colType = column.getType();
            	
            	// Preventing ClassCastExeptions when handling numbers,
            	// because there is no 1:1 type matching between numeric CyColumn types and XGMML types
            	// (only when importing standard XGMML files or from Cytoscape version 3.2 or bellow,
            	// since Cytoscape 3.3 adds the "cy:type" and "cy:elementTypes" attributes)
            	if (colType == Integer.class)
            		row.set(name, ((Number)value).intValue());
            	else if (colType == Long.class)
            		row.set(name, ((Number)value).longValue());
            	else if (colType == Double.class)
            		row.set(name, ((Number)value).doubleValue());
            	else
            		row.set(name, value);
            }
        }
    }
    
    public static Long getIdFromXLink(String href) {
		Matcher matcher = XLINK_PATTERN.matcher(href);
		return matcher.matches() ? Long.valueOf(matcher.group(1)) : null;
	}
}
