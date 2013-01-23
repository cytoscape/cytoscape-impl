package org.cytoscape.filter.internal.filters.view;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2013 The Cytoscape Consortium
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

import java.util.Vector;

import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.util.WidestStringComboBoxModel;


/**
 *
 * @author Noel Ruddock
 */
public class PassFilterWidestStringComboBoxModel extends WidestStringComboBoxModel {
    public PassFilterWidestStringComboBoxModel() {
        super();
    }

    public PassFilterWidestStringComboBoxModel(Object[] items) {
        super(items);
    }

    public PassFilterWidestStringComboBoxModel(Vector<?> v) {
        super(v);
    }

    @Override
    protected String getLabel(Object anObject) {
        return (anObject != null) ? ((CompositeFilter)anObject).getName() : "";
    }
}

