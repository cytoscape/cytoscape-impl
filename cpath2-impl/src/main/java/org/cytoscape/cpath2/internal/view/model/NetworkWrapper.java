package org.cytoscape.cpath2.internal.view.model;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

public class NetworkWrapper {
    private CyNetwork network;

    public NetworkWrapper (CyNetwork network) {
        this.network = network;
    }

    public CyNetwork getNetwork() {
        return network;
    }

    public String toString() {
        if (network != null) {
        	CyRow row = network.getCyRow();
        	String title = row.get(CyNetwork.NAME, String.class);
            if (title != null && title.length() > 40) {
                title = title.substring(0, 38) + "...";
            }
            return "Merge with:  " + title;
        } else {
            return "Create New Network";
        }
    }
}
