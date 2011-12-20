package org.cytoscape.cpath2.internal.util;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;


/**
 * Utility for Finding Groups of Networks.
 *
 * @author Ethan Cerami.
 */
public class NetworkGroupUtil {

    /**
     * Constructs a set of X networks.
     *
     * @return Set<CyNetwork>
     */
    public static Set<CyNetwork> getNetworkSet(int type, CyNetworkManager networkManager) {

        // set to return
        Set<CyNetwork> networkSet = new HashSet<CyNetwork>();

        // get set of cynetworks
        Set<CyNetwork> cyNetworks = networkManager.getNetworkSet();
        if (cyNetworks.size() == 0) return cyNetworks;

        for (CyNetwork net : cyNetworks) {
            String attribute = BioPaxUtil.BIOPAX_NETWORK;
            Boolean b = net.getRow(net).get(attribute, Boolean.class);
            if (b != null && b) {
                networkSet.add(net);
            }
        }

        return networkSet;
    }
}
