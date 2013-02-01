package org.cytoscape.cpath2.internal.view.model;

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

import java.util.List;
import java.util.TreeMap;

import org.cytoscape.cpath2.internal.schemas.summary_response.BasicRecordType;
import org.cytoscape.cpath2.internal.schemas.summary_response.DataSourceType;
import org.cytoscape.cpath2.internal.schemas.summary_response.SummaryResponseType;
import org.cytoscape.cpath2.internal.util.BioPaxEntityTypeMap;

/**
 * List of BioPAX Records.
 *
 * @author Ethan Cerami.
 */
public class RecordList {
    private SummaryResponseType summaryResponse;
    TreeMap<String, Integer> dataSourceMap = new TreeMap<String, Integer>();
    TreeMap<String, Integer> interactionTypeMap = new TreeMap<String, Integer>();

    /**
     * Constructor.
     * @param summaryResponse   SummaryResponseType Object.
     */
    public RecordList (SummaryResponseType summaryResponse) {
        this.summaryResponse = summaryResponse;
        catalogInteractions();
    }

    /**
     * Gets number of records.
     * @return number of records.
     */
    public int getNumRecords() {
        if (summaryResponse != null && summaryResponse.getRecord() != null) {
            return summaryResponse.getRecord().size();
        } else {
            return -1;
        }
    }

    /**
     * Gets the Summary Response XML.
     * @return SummaryResponse Object.
     */
    public SummaryResponseType getSummaryResponse() {
        return summaryResponse;
    }

    /**
     * Gets catalog of data sources.
     * @return Map<Data Source Name, # Records>
     */
    public TreeMap<String, Integer> getDataSourceMap() {
        return dataSourceMap;
    }

    /**
     * Gets catalog of entity sources.
     * @return Map<Entity Type, # Records>
     */
    public TreeMap<String, Integer> getEntityTypeMap() {
        return interactionTypeMap;
    }

    private void catalogInteractions() {
        List<BasicRecordType> recordList = summaryResponse.getRecord();
        if (recordList != null) {
            for (BasicRecordType record:  recordList) {
                catalogDataSource(record.getDataSource());
                catalogInteractionType(record);
                //  TODO:  additional catalogs, as needed.
            }
        }
    }

    private void catalogDataSource(DataSourceType dataSource) {
        String name = dataSource.getName();
        Integer count = dataSourceMap.get(name);
        if (count != null) {
            count = count + 1;
        } else {
            count = 1;
        }
        dataSourceMap.put(name, count);
    }

    private void catalogInteractionType(BasicRecordType record) {
        String type = record.getEntityType();
        BioPaxEntityTypeMap map = BioPaxEntityTypeMap.getInstance();
        String bioPaxType = (String) map.get(type);
        if (bioPaxType != null) {
            type = bioPaxType;
            record.setEntityType(type);
        }
        Integer count = interactionTypeMap.get(type);
        if (count != null) {
            count = count + 1;
        } else {
            count = 1;
        }
        interactionTypeMap.put(type, count);
    }
}