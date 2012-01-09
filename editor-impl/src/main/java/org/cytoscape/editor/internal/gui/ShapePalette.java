/* -*-Java-*-
********************************************************************************
*
* File:         ShapePalette.java
* RCS:          $Header: $
* Description:
* Author:       Allan Kuchinsky
* Created:      Sun May 29 11:18:17 2005
* Modified:     Sun Dec 17 05:33:30 2006 (Michael L. Creech) creech@w235krbza760
* Language:     Java
* Package:
/*
 
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
 
********************************************************************************
*
* Revisions:
*
* Sun Dec 17 05:30:11 2006 (Michael L. Creech) creech@w235krbza760
*  Added DragSourceContextCursorSetter parameter to addShape().
* Mon Dec 04 11:57:11 2006 (Michael L. Creech) creech@w235krbza760
*  Changed the JList to no longer use
*  setFixedCellHeight() since BasicCytoShapeEntitys can now have
*  different sizes.
* Sun Aug 06 11:19:38 2006 (Michael L. Creech) creech@w235krbza760
*  Added generated serial version UUID for serializable classes.
********************************************************************************
*/
package org.cytoscape.editor.internal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.dnd.GraphicalEntity;

import org.cytoscape.editor.internal.GravityTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The <b>ShapePalette</b> class implements a palette from which the user drags and drops shapes onto the canvas
 * The dropping of shapes onto the canvas results in the addition of nodes and edges to the current Cytoscape
 * network, as defined by the behavior of the event handler that responds to the drop events.  For example, in the
 * simple "BioPAX-like" editor, there are node types for proteins, catalysis, small molecules, and biochemical
 * reactions, as well as a directed edge type.
 * <p>
 * The user interface for the ShapePalette is built upon the JList class.
 *
 * @author Allan Kuchinsky
 * @version 1.0
 *
 */
public class ShapePalette extends JPanel {

	private static final long serialVersionUID = -4018789452330887392L;
	private static final Logger logger = LoggerFactory.getLogger(ShapePalette.class);

	/**
	 * mapping of shapes to their titles
	 */
	static Map<String, BasicCytoShapeEntity> shapeMap = new HashMap<String, BasicCytoShapeEntity>();

	/**
	 * the user interface for the ShapePalette
	 */
	protected JList dataList;
	protected DefaultListModel listModel;
	private JPanel controlPane;
	protected JScrollPane scrollPane;
	protected JPanel shapePane;

	private final CySwingApplication app;

	private final GravityTracker<BasicCytoShapeEntity> gravityTracker = new GravityTracker<BasicCytoShapeEntity>();

	/**
	 * Creates a new ShapePalette object.
	 */
	public ShapePalette(CySwingApplication app) {
		super();

		this.app = app;

		controlPane = new JPanel();
		controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.Y_AXIS));
		TitledBorder t2 = BorderFactory.createTitledBorder("Instructions:");
		controlPane.setBorder(t2);
		String instructions = "<html><style type='text/css'>body{ font-family: sans-serif; font-size: 11pt; }</style><b>Drag and Drop:</b> <ul> <li>A node shape onto the network view.  <li>An edge shape onto the source node, then click on the target node.  </ul> <b>Double-click:</b> <ul> <li>To add nodes and edges specified in SIF format </ul>" + (System.getProperty("os.name").startsWith("Mac") ? "<b>CMD-click:</b>" : "<b>CTRL-click:</b>") + "<ul> <li>On empty space to create a node.  <li>On a node to begin an edge and specify the source node. Then click on the target node to finish the edge.  </ul></html>";
		
		JEditorPane instructionsArea = new JEditorPane("text/html",instructions);
		// 32767 ????
		instructionsArea.setPreferredSize(new java.awt.Dimension(32767, 400));
		instructionsArea.setBackground(Color.white);
		controlPane.add(instructionsArea);
		controlPane.setBackground(Color.white);
		
		JPanel pnlSpecifyIdentifier = new JPanel();
		pnlSpecifyIdentifier.setBackground(Color.white);

		pnlSpecifyIdentifier.setMaximumSize(new java.awt.Dimension(32767, 100));
		JLabel chkSpecifyLabel = new JLabel("Specify Name:");
		chkSpecifyIdentifier = new javax.swing.JCheckBox();
		chkSpecifyIdentifier.setToolTipText("Checking the box will allow you to choose the identifier for added nodes and edges.");
		pnlSpecifyIdentifier.setLayout(new java.awt.GridBagLayout());
		pnlSpecifyIdentifier.setBorder(BorderFactory.createTitledBorder(""));
		chkSpecifyIdentifier.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent evt) {
					CheckBoxSpecifyIdentifierStateChanged(evt);
				}
			});
		pnlSpecifyIdentifier.add(chkSpecifyLabel);
		pnlSpecifyIdentifier.add(chkSpecifyIdentifier);
		chkSpecifyIdentifier.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		chkSpecifyIdentifier.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		chkSpecifyIdentifier.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
		chkSpecifyIdentifier.setMargin(new java.awt.Insets(0, 0, 0, 0));
		java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		pnlSpecifyIdentifier.add(chkSpecifyIdentifier, gridBagConstraints);
		
		listModel = new DefaultListModel();
		dataList = new JList(listModel);
		dataList.setCellRenderer(new MyCellRenderer());
		dataList.setDragEnabled(true);

		dataList.setTransferHandler(new PaletteListTransferHandler());
		shapePane = new JPanel();
		shapePane.setBackground(Color.white); 
		shapePane.setLayout(new BoxLayout(shapePane, BoxLayout.Y_AXIS));

		scrollPane = new JScrollPane(shapePane);

		scrollPane.setBorder(BorderFactory.createEtchedBorder());
		dataList.setBackground(Color.white);
		scrollPane.setBackground(Color.white);
 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.setLayout(new java.awt.GridBagLayout());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(controlPane, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(pnlSpecifyIdentifier, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		this.add(scrollPane, gridBagConstraints);

		this.setBackground(Color.white);
		this.setVisible(true);
	}

	private javax.swing.JCheckBox chkSpecifyIdentifier;
	public void CheckBoxSpecifyIdentifierStateChanged(javax.swing.event.ChangeEvent evt){
		if (chkSpecifyIdentifier.isSelected()){
			specifyIdentifier = true;
		} else {
			specifyIdentifier = false;
		}
	}
    
	public static boolean specifyIdentifier = false;
    
	/**
	 * clear the ShapePalette by removing all its shape components
	 *
	 */
	public void clear() {
		shapePane.removeAll();
	}

	public void addGraphicalEntity(GraphicalEntity cytoShape, Map props) {
		BasicCytoShapeEntity shape = new BasicCytoShapeEntity(app,cytoShape);
        shapeMap.put(cytoShape.getTitle(), shape);
		int index = gravityTracker.add( shape, getDouble((String)(props.get("editorGravity"))) );
		logger.debug("adding " + cytoShape.getTitle() + " at index: " + index + " with gravity " + props.get("editorGravity"));
        shapePane.add( shape, index );
    }

	private double getDouble(String s) {
		if ( s == null )
			return 100.0;

		double d;
		try {
			d = Double.parseDouble(s);
		} catch (Exception e) {
			d = 100.0;
		}
		return d;
	}

	public void removeGraphicalEntity(GraphicalEntity cytoShape, Map props) {
		BasicCytoShapeEntity shape = shapeMap.remove(cytoShape.getTitle());
		shapePane.remove( shape );
		gravityTracker.remove( shape );
    }

	/**
	 * show the palette in the WEST cytopanel
	 *
	 */
	public void showPalette() {
		this.setVisible(true);
	}

    /**
     *
     * @param key the name of the shape to be returned
     * @return return the BasicCytoShapeEntity associated with the input shape name
     */
    public static BasicCytoShapeEntity getBasicCytoShapeEntity(String key) {
        Object val = shapeMap.get(key);

        if (val instanceof BasicCytoShapeEntity) {
            return ((BasicCytoShapeEntity) val);
        } else {
            return null;
        }
    }

	/**
	 * renders each cell of the ShapePalette
	 * @author Allan Kuchinsky
	 * @version 1.0
	 *
	 */
	class MyCellRenderer extends JLabel implements ListCellRenderer {
		// This is the only method defined by ListCellRenderer.
		// We just reconfigure the JLabel each time we're called.
		// MLC 08/06/06:
		private static final long serialVersionUID = -4704405703871398609L;

		public Component getListCellRendererComponent(JList list, Object value, // value to display
		                                              int index, // cell index
		                                              boolean isSelected, // is the cell selected
		                                              boolean cellHasFocus) // the list and the cell have the focus
		{
			if (value instanceof BasicCytoShapeEntity) {
				BasicCytoShapeEntity cytoShape = (BasicCytoShapeEntity) value;
				setText(cytoShape.getTitle());
				setIcon(cytoShape.getIcon());
				setToolTipText(cytoShape.getToolTipText());
			}

			if (isSelected) {
				//setBackground(list.getSelectionBackground());
				setBackground(Color.white);
				setForeground(list.getSelectionForeground());
			} else {
				//setBackground(list.getBackground());
				setBackground(Color.white);
				setForeground(list.getForeground());
			}

			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);

			return this;
		}
	}

	/**
	 * bundles up the name of the BasicCytoShapeEntity for export via drag/drop from the palette
	 * @author Allan Kuchinsky
	 * @version 1.0
	 *
	 */
	class PaletteListTransferHandler extends StringTransferHandler {
		// MLC 08/06/06:
		private static final long serialVersionUID = -3858539899491771525L;

		protected void cleanup(JComponent c, boolean remove) {
		}

		protected void importString(JComponent c, String str) {
		}

		//Bundle up the selected items in the list
		//as a single string, for export.
		protected String exportString(JComponent c) {
			JList list = (JList) c;
			Object val = list.getSelectedValue();

			if (val instanceof BasicCytoShapeEntity) {
				return ((BasicCytoShapeEntity) val).getTitle();
			} else {
				return null;
			}
		}
	}
}
