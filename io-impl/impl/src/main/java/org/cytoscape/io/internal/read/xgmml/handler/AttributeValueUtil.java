/*
 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.io.internal.read.xgmml.MetadataEntries;
import org.cytoscape.io.internal.read.xgmml.MetadataParser;
import org.cytoscape.io.internal.read.xgmml.ObjectType;
import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
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

    public AttributeValueUtil(ReadDataManager manager) {
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
     * Return the typed attribute value for the passed attribute. In this case,
     * the caller has already determined that this is the correct attribute and
     * we just lookup the value. This routine is responsible for type conversion
     * consistent with the passed argument.
     * 
     * @param type
     *            the ObjectType of the value
     * @param atts
     *            the attributes
     * @return the value of the attribute in the appropriate type
     */
    protected Object getTypedAttributeValue(ObjectType type, Attributes atts) throws SAXParseException {
        String value = atts.getValue("value");

        try {
            return typeMap.getTypedValue(type, value);
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

    protected ParseState handleAttribute(Attributes atts) throws SAXParseException {
    	ParseState parseState = ParseState.NONE;
    	
    	final String name = atts.getValue("name");
    	final String type = atts.getValue("type");
    	final String equationStr = atts.getValue("cy:equation");
    	final boolean isEquation = equationStr != null ? Boolean.parseBoolean(equationStr) : false;
    	final String hiddenStr = atts.getValue("cy:hidden");
    	final boolean isHidden = hiddenStr != null ? Boolean.parseBoolean(hiddenStr) : false;
        
    	final String tableName = isHidden ? CyNetwork.HIDDEN_ATTRS : CyNetwork.DEFAULT_ATTRS;
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
		
		final CyRow row = curNet.getRow(curElement, tableName);
        final CyColumn column = row.getTable().getColumn(name);
        
        Object value = null;
        ObjectType objType = typeMap.getType(type);

        if (isEquation) {
        	// It is an equation...
        	String formula = atts.getValue("value");
        	
            if (name != null && formula != null) {
            	manager.addEquationString(row, name, formula);
            }
        } else {
        	// Regular attribute value...
        	value = getTypedAttributeValue(objType, atts);
        }

		switch (objType) {
			case BOOLEAN:
				if (name != null) setAttribute(row, name, Boolean.class, (Boolean) value);
				break;
			case REAL:
				if (name != null) setAttribute(row, name, Double.class, (Double) value);
				break;
			case INTEGER:
				if (name != null) setAttribute(row, name, Integer.class, (Integer) value);
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
				return ParseState.LIST_ATT;
		}

        return parseState;
    }
    
    private <T> void setAttribute(CyRow row, String name, Class<T> type, T value) {
        if (name != null) {
            CyTable table = row.getTable();
            
            if (table.getColumn(name) == null) {
            	table.createColumn(name, type, false);
            }
            
            if (value != null) {
            	row.set(name, value);
            }
        }
    }
    
    public static Long getIdFromXLink(String href) {
		Matcher matcher = XLINK_PATTERN.matcher(href);
		return matcher.matches() ? Long.valueOf(matcher.group(1)) : null;
	}
}
