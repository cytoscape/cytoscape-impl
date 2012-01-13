package org.cytoscape.cpath2.internal.view.model;

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