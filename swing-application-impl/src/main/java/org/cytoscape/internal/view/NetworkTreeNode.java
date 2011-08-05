package org.cytoscape.internal.view;

import javax.swing.tree.DefaultMutableTreeNode;

final class NetworkTreeNode extends DefaultMutableTreeNode {
    private final static long serialVersionUID = 1213748836736485L;
    protected Long network_uid;

    public NetworkTreeNode(Object userobj, Long id) {
        super(userobj.toString());
        network_uid = id;
    }

    protected void setNetworkID(Long id) {
        network_uid = id;
    }

    protected Long getNetworkID() {
        return network_uid;
    }
}