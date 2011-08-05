package org.cytoscape.cpath2.internal.view.model;

import java.util.Observable;

/**
 * Contains information regarding the currently selected set of interaction bundles.
 *
 * @author Ethan Cerami
 */
public class InteractionBundleModel extends Observable {
    private RecordList recordList;
    private String physicalEntityName;

    /**
     * Sets the SummaryResponse Object.
     * @param recordList Record List.
     */
    public void setRecordList (RecordList recordList) {
        this.recordList = recordList;
        this.setChanged();
        this.notifyObservers();
    }

    /**
     * Gets the Record List.
     * @return RecordList Object.
     */
    public RecordList getRecordList() {
        return recordList;
    }

    /**
     * Gets the Physical Entity Name.
     * @return PE Name.
     */
    public String getPhysicalEntityName() {
        return physicalEntityName;
    }

    /**
     * Sets the Physical Entity Name.
     * @param physicalEntityName PE Name.
     */
    public void setPhysicalEntityName(String physicalEntityName) {
        this.physicalEntityName = physicalEntityName;
    }
}