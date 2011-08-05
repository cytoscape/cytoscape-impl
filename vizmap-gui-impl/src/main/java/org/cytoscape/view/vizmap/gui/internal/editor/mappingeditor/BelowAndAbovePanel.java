
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;


/**
 * Drawing and updating below & above values in Gradient Editor.
 *
 * @author $author$
 */
public class BelowAndAbovePanel extends JPanel {
	private final static long serialVersionUID = 1202339876961477L;
	private VisualProperty<?> type;
	private Color boxColor;
	private boolean below;
	private Object value;
	private VisualMappingManager vmm;
		
	private EditorManager editorManager;

	private final ContinuousMapping<?, ?> mapping; 
	
	/**
	 * DOCUMENT ME!
	 */
	public static final String COLOR_CHANGED = "COLOR_CHANGED";

	/**
	 * Creates a new BelowAndAbovePanel object. This will be used for drawing
	 * below & above triangle
	 *
	 * @param color
	 *            DOCUMENT ME!
	 * @param below
	 *            DOCUMENT ME!
	 */
	public BelowAndAbovePanel(Color color, boolean below, final ContinuousMapping<?, ?> mapping) {
		this.boxColor = color;
		this.below = below;
		this.mapping = mapping;
		this.type = mapping.getVisualProperty();

		if (below)
			this.setToolTipText("Double-click triangle to set below color...");
		else
			this.setToolTipText("Double-click triangle to set above color...");

		this.addMouseListener(new MouseEventHandler(this));
	}

	/**
	 * Creates a new BelowAndAbovePanel object.
	 *
	 * @param type DOCUMENT ME!
	 * @param below DOCUMENT ME!
	 */
	public BelowAndAbovePanel(boolean below, ContinuousMapping<?, ?> mapping) {
		this(Color.DARK_GRAY, below, mapping);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param newColor DOCUMENT ME!
	 */
	public void setColor(Color newColor) {
		final Color oldColor = boxColor;
		this.boxColor = newColor;
		this.repaint();
		this.getParent().repaint();

		this.firePropertyChange(COLOR_CHANGED, oldColor, newColor);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param value DOCUMENT ME!
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param g
	 *            DOCUMENT ME!
	 */
	public void paintComponent(Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;

		final Polygon poly = new Polygon();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setColor(boxColor);

		if (below) {
			poly.addPoint(9, 0);
			poly.addPoint(9, 10);
			poly.addPoint(0, 5);
		} else {
			poly.addPoint(0, 0);
			poly.addPoint(0, 10);
			poly.addPoint(9, 5);
		}

		g2d.fillPolygon(poly);

		g2d.setColor(Color.black);
		g2d.draw(poly);
	}

	class MouseEventHandler extends MouseAdapter {
		private BelowAndAbovePanel caller;

		public MouseEventHandler(BelowAndAbovePanel c) {
			this.caller = c;
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				Object newValue = null;

				if (type.getRange().getType() == Color.class) {
					//FIXME
//					newValue = editorManager).showDialog(caller, "Select new color", boxColor);
					caller.setColor((Color) newValue);
				} else if (type.getRange().getType() == Number.class) {
					newValue = Double.parseDouble(JOptionPane.showInputDialog(caller,
					                                                          "Please enter new value."));
					caller.setValue(newValue);
				}

				if (newValue == null) {
					return;
				}

				
				

				BoundaryRangeValues brv;
				BoundaryRangeValues original;

				if (below) {
					original = mapping.getPoint(0).getRange();
					brv = new BoundaryRangeValues(newValue, original.equalValue,
					                              original.greaterValue);
					mapping.getPoint(0).setRange(brv);
				} else {
					original = mapping.getPoint(mapping.getPointCount() - 1).getRange();

					brv = new BoundaryRangeValues(original.lesserValue, original.equalValue,
					                              newValue);
					mapping.getPoint(mapping.getPointCount() - 1).setRange(brv);
				}

				//mapping.fireStateChanged();

				// Update view.
				//Cytoscape.redrawGraph(vmm.getNetworkView());

				caller.repaint();
				caller.getParent().repaint();
			}
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param vmm DOCUMENT ME!
	 */
	public void setVmm(VisualMappingManager vmm) {
		this.vmm = vmm;
	}
}
