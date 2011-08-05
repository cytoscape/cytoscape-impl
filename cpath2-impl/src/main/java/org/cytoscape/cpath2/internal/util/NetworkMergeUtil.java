package org.cytoscape.cpath2.internal.util;

import java.net.URL;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.view.SearchDetailsPanel;
import org.cytoscape.cpath2.internal.view.model.NetworkWrapper;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.model.CyNetwork;

/**
 * Network Merge Utility.
 */
public class NetworkMergeUtil {
    private Vector networkVector;
	private final CPath2Factory factory;

    /**
     * Constructor.
     */
    public NetworkMergeUtil(CPath2Factory factory) {
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
            URL iconURL = SearchDetailsPanel.class.getResource("resources/question.png");
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
