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