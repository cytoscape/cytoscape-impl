package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.read.xgmml.ObjectType;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleListAttribute extends AbstractHandler {

    @Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        String type = atts.getValue("type");
        ObjectType objType = typeMap.getType(type);
        Object obj = attributeValueUtil.getTypedAttributeValue(objType, atts);
        Class<?> clazz = null;

        switch (objType) {
            case BOOLEAN:
                clazz = Boolean.class;
                break;
            case REAL:
                clazz = Double.class;
                break;
            case INTEGER:
                clazz = Integer.class;
                break;
            case STRING:
            default:
                clazz = String.class;
                break;
        }

        String name = manager.currentAttributeID;
        CyRow row = manager.getCurrentRow();
        CyColumn column = row.getTable().getColumn(name);

        if (column == null) {
            row.getTable().createListColumn(name, clazz, false);
            column = row.getTable().getColumn(name);
        }

        if (List.class.isAssignableFrom(column.getType())) {
            if (manager.listAttrHolder == null) {
                manager.listAttrHolder = new ArrayList<Object>();
                row.set(name, manager.listAttrHolder);
            }

            if (manager.listAttrHolder != null)
                manager.listAttrHolder.add(obj);
        }

        return current;
    }
}
