package org.cytoscape.cpath2.internal.view;

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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.cpath2.internal.schemas.summary_response.BasicRecordType;

/**
 * Chained Filter.
 *
 * @author Ethan Cerami
 */
public class ChainedFilter implements Filter {
    private ArrayList<Filter> filterList = new ArrayList<Filter>();

    /**
     * Adds a new filter.
     * @param filter Filter Object.
     */
    public void addFilter (Filter filter) {
        filterList.add(filter);
    }

    /**
     * Filters the record list.  Those items which pass the filter
     * are included in the returned list.
     *
     * @param recordList List of RecordType Objects.
     * @return List of RecordType Objects.
     */    
    public List<BasicRecordType> filter(List<BasicRecordType> recordList) {
        for (Filter filter:  filterList) {
            recordList = filter.filter(recordList);
        }
        return recordList;
    }
}