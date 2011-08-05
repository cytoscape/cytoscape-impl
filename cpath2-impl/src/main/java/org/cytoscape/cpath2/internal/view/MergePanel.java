package org.cytoscape.cpath2.internal.view;

import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.util.NetworkMergeUtil;

/**
 * Merge Panel
 *
 * @author Ethan Cerami.
 */
public class MergePanel extends JPanel {
    private JComboBox networkComboBox;

    public MergePanel(CPath2Factory factory) {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        NetworkMergeUtil mergeUtil = factory.getNetworkMergeUtil();
        if (mergeUtil.mergeNetworksExist()) {
            Vector networkVector = mergeUtil.getMergeNetworks();
            networkComboBox = new JComboBox(networkVector);
            JLabel label = new JLabel("Create / Merge:  ");
            this.add(label);
            this.add(networkComboBox);
            networkComboBox.setSelectedIndex(0);
        }
    }

    public JComboBox getNetworkComboBox() {
        return this.networkComboBox;
    }
}