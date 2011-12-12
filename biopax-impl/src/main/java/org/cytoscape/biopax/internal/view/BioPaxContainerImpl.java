// $Id: BioPaxContainer.java,v 1.7 2006/06/15 22:06:02 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal.view;


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
import org.cytoscape.biopax.BioPaxContainer;
import org.cytoscape.biopax.internal.MapBioPaxToCytoscapeImpl;
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
public class BioPaxContainerImpl extends JPanel implements BioPaxContainer {
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
	public BioPaxContainerImpl(LaunchExternalBrowser browser, CyApplicationManager applicationManager, CyNetworkViewManager viewManager, BioPaxDetailsPanel bpDetailsPanel, CySwingApplication application) {
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
	@Override
    public void showDetails() {
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, DETAILS_CARD);
        label.setText("<a href='LEGEND'>Visual Legend</a>");
    }

    /**
     * Show Legend Panel.
     */
	@Override
    public void showLegend() {
        CardLayout cl = (CardLayout)(cards.getLayout());
        CyNetwork network = applicationManager.getCurrentNetwork();
        CyRow row = network.getRow(network);
        Boolean isBioPaxNetwork = row.get(MapBioPaxToCytoscapeImpl.BIOPAX_NETWORK, Boolean.class);
        if (isBioPaxNetwork != null) {
            cl.show(cards, LEGEND_BIOPAX_CARD);
        } else {
            cl.show(cards, LEGEND_BINARY_CARD);
        }
        label.setText("<a href='DETAILS'>View Details</a>");
    }

	@Override
	public Component getComponent() {
		return this;
	}
}
