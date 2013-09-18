package org.cytoscape.cpath2.internal.util;

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

import java.net.URL;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.cytoscape.cpath2.internal.CPathFactory;
import org.cytoscape.cpath2.internal.CPathProperties;
import org.cytoscape.cpath2.internal.view.NetworkWrapper;
import org.cytoscape.cpath2.internal.view.SearchDetailsPanel;
import org.cytoscape.model.CyNetwork;

/**
 * Network Merge Utility.
 */
public class NetworkMergeUtil {
    private Vector networkVector;
	private final CPathFactory factory;

    /**
     * Constructor.
     */
    public NetworkMergeUtil(CPathFactory factory) {
    	this.factory = factory;
    	
        CPathProperties cPathProperties = CPathProperties.getInstance();
        int downloadMode = cPathProperties.getDownloadMode();

        //  Get networks which we could merge with.
        Set<CyNetwork> networkSet = NetworkGroupUtil.getNetworkSet(downloadMode, factory.getNetworkManager());

        networkVector = new Vector();
        networkVector.add(new NetworkWrapper(null));
        if (networkSet != null && networkSet.size() > 0) {
            for (CyNetwork net : networkSet) {
                NetworkWrapper netWrapper = new NetworkWrapper (net);
                networkVector.add(netWrapper);
            }
        }
    }

    /**
     * Prompt User for Network to Merge.
     * @return NetworkWrapper Object.
     */
    public NetworkWrapper promptForNetworkToMerge() {
        if (mergeNetworksExist()) {
            NetworkWrapper[] networks = (NetworkWrapper[]) getMergeNetworks().toArray
                (new NetworkWrapper[getMergeNetworks().size()]);
            URL iconURL = SearchDetailsPanel.class.getResource("question.png");
            Icon icon = null;
            if (iconURL != null) {
                icon = new ImageIcon(iconURL);
            }
            NetworkWrapper mergeNetwork = (NetworkWrapper)
                    JOptionPane.showInputDialog(factory.getCySwingApplication().getJFrame(),
                    "Create new network or merge with existing network?", "Create / Merge",
                    JOptionPane.PLAIN_MESSAGE, icon,
                    networks, networks[0]);
            if (mergeNetwork == null) {
                return null;
            } else {
                return mergeNetwork;
            }
        }
        return null;
    }

    /**
     * Do mergeable network exist?
     * @return true or false.
     */
    public boolean mergeNetworksExist() {
        if (networkVector != null && networkVector.size() >1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Candidate networks for merging.
     * @return Vector of NetworkWrapper Objects.
     */
    public Vector getMergeNetworks() {
        return networkVector;
    }
}
