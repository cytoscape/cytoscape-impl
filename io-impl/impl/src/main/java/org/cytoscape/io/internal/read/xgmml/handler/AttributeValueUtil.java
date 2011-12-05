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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
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
    
    static final Pattern XLINK_PATTERN = Pattern.compile(".*#(\\d+)");

    private Locator locator;

    private final ReadDataManager manager;
    private final ObjectTypeMap typeMap;
    
    protected static final Logger logger = LoggerFactory.getLogger(AttributeValueUtil.class);

    public AttributeValueUtil(ObjectTypeMap typeMap, ReadDataManager manager) {
        this.typeMap = typeMap;
        this.manager = manager;
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

    protected ParseState handleAttribute(Attributes atts, CyRow row) throws SAXParseException {
    	ParseState parseState = ParseState.NONE;
    	
    	String name = atts.getValue("name");
        String type = atts.getValue("type");
        String equationStr = atts.getValue("cy:equation");
        boolean isEquation = equationStr != null ? Boolean.parseBoolean(equationStr) : false;
        
        ObjectType objType = typeMap.getType(type);
        CyColumn column = row.getTable().getColumn(name);
        Object value = null;

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
    
    public static boolean fromXGMMLBoolean(String s) {
    	return s != null && s.matches("(?i)1|true"); // should be only "1", but let's be nice and also accept "true"
    }
    
    public static String toXGMMLBoolean(boolean value) {
    	return value ? "1" : "0";
    }
    
    public static double parseDocumentVersion(String value) {
		double version = 0.0;
    	
    	try {
			version = Double.parseDouble(value);
		} catch (Exception nfe) {
		}
    	
    	return version;
	}
    
	public static String getIdFromXLink(String href) {
		Matcher matcher = XLINK_PATTERN.matcher(href);
		return matcher.matches() ? matcher.group(1) : null;
	}
}
