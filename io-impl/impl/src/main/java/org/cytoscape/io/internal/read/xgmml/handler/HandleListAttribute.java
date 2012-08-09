/*
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.read.SUIDUpdater;
import org.cytoscape.io.internal.read.xgmml.ObjectType;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleListAttribute extends AbstractHandler {

    @Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        final String type = atts.getValue("type");
        final String name = manager.currentAttributeID;
        final ObjectType objType = typeMap.getType(type);
        final Object value = attributeValueUtil.getTypedAttributeValue(objType, atts, name);
        Class<?> clazz = null;

        switch (objType) {
            case BOOLEAN:
                clazz = Boolean.class;
                break;
            case REAL:
                clazz = SUIDUpdater.isUpdatableSUIDColumnName(name) ? Long.class : Double.class;
                break;
            case INTEGER:
                clazz = Integer.class;
                break;
            case STRING:
            default:
                clazz = String.class;
                break;
        }

        final CyRow row = manager.getCurrentRow();
        CyColumn column = row.getTable().getColumn(name);

        if (column == null) {
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
