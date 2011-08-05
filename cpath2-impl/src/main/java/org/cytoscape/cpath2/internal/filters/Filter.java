package org.cytoscape.cpath2.internal.filters;

import java.util.List;

import org.cytoscape.cpath2.internal.schemas.summary_response.BasicRecordType;

/**
 * Filter interface.
 *
 * @author Ethan Cerami
 */
public interface Filter {

    /**
     * Filters the record list.  Those items which pass the filter
     * are included in the returned list.
     *
     * @param recordList List of RecordType Objects.
     * @return List of RecordType Objects. 
     */
    public List<BasicRecordType> filter (List<BasicRecordType> recordList);
}