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
import java.util.List;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.io.internal.util.xgmml.ObjectType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleListAttribute extends AbstractHandler {

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        final String type = atts.getValue("type");
        final String cyType = atts.getValue("cy:type");
        final String name = manager.currentAttributeID;
        
        final ObjectType objType = typeMap.fromXgmml(cyType, type);
        final Object value = attributeValueUtil.getTypedAttributeValue(objType, atts, name);

        final CyRow row = manager.getCurrentRow();
        CyColumn column = row.getTable().getColumn(name);

        if (column == null) {
        	final Class<?> clazz = typeMap.getClass(objType, name);
            
        	row.getTable().createListColumn(name, clazz, false, new ArrayList());
            column = row.getTable().getColumn(name);
        }

        if (List.class.isAssignableFrom(column.getType())) {
            if (manager.listAttrHolder == null) {
                manager.listAttrHolder = new ArrayList<Object>();
                row.set(name, manager.listAttrHolder);
            }

            if (manager.listAttrHolder != null && value != null)
                manager.listAttrHolder.add(value);
        }

        return current;
    }
}
