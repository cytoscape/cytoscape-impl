package org.cytoscape.cpath2.internal.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.cpath2.internal.schemas.summary_response.BasicRecordType;
import org.cytoscape.cpath2.internal.schemas.summary_response.DataSourceType;

/**
 * Data Source Filter.
 *
 * @author Ethan Cerami
 */
public class DataSourceFilter implements Filter {
    Set<String> dataSourceSet;

    /**
     * Constructor.
     *
     * @param dataSourceSet Set of Data Sources we want to keep.
     */
    public DataSourceFilter(Set<String> dataSourceSet) {
        this.dataSourceSet = dataSourceSet;
    }

    /**
     * Filters the record list.  Those items which pass the filter
     * are included in the returned list.
     *
     * @param recordList List of RecordType Objects.
     * @return List of RecordType Objects.
     */
    public List<BasicRecordType> filter(List<BasicRecordType> recordList) {
        ArrayList<BasicRecordType> passedList = new ArrayList<BasicRecordType>();
        for (BasicRecordType record : recordList) {
            DataSourceType dataSource = record.getDataSource();
            if (dataSource != null) {
                String dataSourceName = dataSource.getName();
                if (dataSourceName != null) {
                    if (dataSourceSet.contains(dataSourceName)) {
                        passedList.add(record);
                    }
                }
            }
        }
        return passedList;
    }
}
