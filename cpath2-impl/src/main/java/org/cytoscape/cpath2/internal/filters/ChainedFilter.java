package org.cytoscape.cpath2.internal.filters;

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