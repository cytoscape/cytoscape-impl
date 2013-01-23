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