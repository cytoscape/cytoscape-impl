package org.cytoscape.biopax.internal.view;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.biopax.internal.BioPaxMapper;
import org.cytoscape.biopax.internal.action.LaunchExternalBrowser;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkViewManager;


/**
 * Container for all BioPax UI Components.
 * <p/>
 * Currently includes:
 * <UL>
 * <LI>BioPaxDetailsPanel
 * <LI>BioPaxLegendPanel
 * <LI>AboutPanel
 * </UL>
 *
 * @author Ethan Cerami
 */
public class BioPaxContainer extends JPanel {
	private static final long serialVersionUID = 1L;
	/**
	 * CytoPanel Location of this Panel
	 */
	public static final CytoPanelName CYTO_PANEL_LOCATION = CytoPanelName.EAST;
    private JEditorPane label;
    private JPanel cards;
	private final CyApplicationManager applicationManager;

    private final static String DETAILS_CARD = "DETAILS";
    private final static String LEGEND_BIOPAX_CARD = "LEGEND_BIOPAX";
    private final static String LEGEND_BINARY_CARD = "LEGEND_BINARY";

    /**
	 * Private Constructor.
     * @param factory 
     * @param applicationManager 
	 */
	public BioPaxContainer(LaunchExternalBrowser browser, CyApplicationManager applicationManager, CyNetworkViewManager viewManager, BioPaxDetailsPanel bpDetailsPanel, CySwingApplication application) {
		this.applicationManager = applicationManager;
		
        cards = new JPanel(new CardLayout());
        LegendPanel bioPaxLegendPanel = new LegendPanel(LegendPanel.BIOPAX_LEGEND, applicationManager, application);
        LegendPanel binaryLegendPanel = new LegendPanel(LegendPanel.BINARY_LEGEND, applicationManager, application);

        cards.add (bpDetailsPanel, DETAILS_CARD);
        cards.add (bioPaxLegendPanel, LEGEND_BIOPAX_CARD);
        cards.add (binaryLegendPanel, LEGEND_BINARY_CARD);
        
        this.setLayout(new BorderLayout());
		this.add(cards, BorderLayout.CENTER);

        label = new JEditorPane ("text/html", "<a href='LEGEND'>Visual Legend</a>");
        label.setEditable(false);
        label.setOpaque(false);
        label.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        label.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
				if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					String name = hyperlinkEvent.getDescription();
					if (name.equalsIgnoreCase("LEGEND")) {
						showLegend();
					} else {
						showDetails();
					}
				}
			}
        });

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        Font font = label.getFont();
        Font newFont = new Font (font.getFamily(), font.getStyle(), font.getSize()-2);
        label.setFont(newFont);
        label.setBorder(new EmptyBorder(5,3,3,3));
        this.add(label, BorderLayout.SOUTH);
	}

    /**
     * Show Details Panel.
     */
    public void showDetails() {
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, DETAILS_CARD);
        label.setText("<a href='LEGEND'>Visual Legend</a>");
    }

    /**
     * Show Legend Panel.
     */
    public void showLegend() {
        CardLayout cl = (CardLayout)(cards.getLayout());
        CyNetwork network = applicationManager.getCurrentNetwork();
        if(network == null)
        	return;
        	
        CyRow row = network.getRow(network);
        Boolean isBioPaxNetwork = row.get(BioPaxMapper.BIOPAX_NETWORK, Boolean.class);
        if (Boolean.TRUE.equals(isBioPaxNetwork)) {
            cl.show(cards, LEGEND_BIOPAX_CARD);
        } else {
            cl.show(cards, LEGEND_BINARY_CARD);
        }
        label.setText("<a href='DETAILS'>View Details</a>");
    }

}
