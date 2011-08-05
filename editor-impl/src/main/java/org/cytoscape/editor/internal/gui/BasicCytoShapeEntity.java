/* -*-Java-*-
********************************************************************************
*
* File:         BasicCytoShapeEntity.java
* RCS:          $Header: $
* Description:
* Author:       Allan Kuchinsky
* Created:      Sun May 29 11:22:33 2005
* Modified:     Sun Dec 17 05:29:24 2006 (Michael L. Creech) creech@w235krbza760
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
* Sat Dec 16 14:50:40 2006 (Michael L. Creech) creech@w235krbza760
*  Completely rewrote TestDragSourceListener (now is EntityDragSourceListener) to
*  allow for intelligent setting of the drag cursor. Changed constructor to
*  take a DragSourceContextCursorSetter.
*  Changed all instance variables to be private.
* Tue Dec 05 04:39:09 2006 (Michael L. Creech) creech@w235krbza760
*  Changed computation of BasicCytoShapeEntity size to allow for
*  larger CytoShapeIcons.
* Sun Aug 06 11:22:50 2006 (Michael L. Creech) creech@w235krbza760
*  Added generated serial version UUID for serializable classes.
********************************************************************************
*/
package org.cytoscape.editor.internal.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.dnd.GraphicalEntity;


/**
 *
 * The <b>BasicCytoShapeEntity</b> class defines draggable/droppable visual components in the
 * Cytoscape editor framework.  The framework provides for dragging and dropping graphical
 * entities from palette onto the canvas.   BasicCytoShapeEntity objects are associated with
 * semantic objects, i.e. nodes and edges, that are created when the graphical entities
 * are dropped onto the canvas.
 * @author Allan Kuchinsky
 * @version 1.0
 *
 */
public class BasicCytoShapeEntity extends JComponent implements DragGestureListener, GraphicalEntity {
	// MLC 07/27/06:
	private static final long serialVersionUID = -5229827235046946347L;

	// MLC 12/16/06 BEGIN:
	private static DragSourceContextCursorSetter defaultCursorSetter = new DragSourceContextCursorSetter() {
		// The default shows that a drop is possible anywhere on the netView:
		public Cursor computeCursor(Point netViewLoc, DragSourceDragEvent dsde) {
			return DragSource.DefaultCopyDrop;
		}
	};

	/**
	* used for setting tooltip text
	*/
	private JLabel _cytoShape;

	/**
	 * the title of the shape
	 */
	private String title;

	/**
	 * attribute name for the shape
	 * should be one of "NodeType" or "EdgeType"
	 */
	private String attributeName;

	/**
	 * value for the attribute assigned to the shape
	 * for example a "NodeType" of "protein"
	 */
	private String attributeValue;

	/**
	 * the icon associated with the shape
	 */
	private Icon _image;

	/**
	 * the source of a drag event
	 */
	private DragSource myDragSource;

	private BasicCytoShapeTransferHandler handler;

	/**
	 * the image associated with the Icon for the shape
	 */
	private Image _img;

	private final CySwingApplication app;

	/**
	 *
	 * @param attributeName attribute name for the shape, should be one of "NodeType" or "EdgeType"
	 * @param attributeValue value for the attribute assigned to the shape, for example a "NodeType" of "protein"
	 * @param image the icon for the shape
	 * @param title the title of the shape
	 * @param cursorSetter a possibly null DragSourceContextCursorSetter used to specify
	 *                     the cursor so show when dragging over the current network view.
	 *                     If null, a default cursor setter is used shows its ok
	 *                     to drop anywhere on the network view.
	 */
	public BasicCytoShapeEntity(CySwingApplication app, String attributeName, String attributeValue, Icon image, String title) {
		super();
		this.setTitle(title);
		this.app = app;
		_image = image;

		this.attributeName = attributeName;
		this.attributeValue = attributeValue;

		if (image instanceof ImageIcon) {
			_img = ((ImageIcon) image).getImage();
		}

		_cytoShape = new JLabel(image);

		if (this.attributeName != null) {
			if (this.attributeName.equals("NODE_TYPE")) {
				_cytoShape.setToolTipText("<html>To add a node to a network,<br>"
				                              + "drag and drop a shape<br>"
										      + "from the palette onto the canvas<br>"
											  + "OR<br>"
										      + "simply CTRL-click on the canvas.</html>");
			} else if (this.attributeName.equals("EDGE_TYPE")) {
				_cytoShape.setToolTipText("<html>To connect two nodes with an edge<br>"
				                          + "drag and drop the arrow onto a node<br>"
										  + "on the canvas, then move the cursor<br>"
										  + "over a second node and click the mouse.<br>"
										  + "OR<br>"
										  + "CTRL-click on the first node and then<br>"
										  + "click on the second node. </html>");
			} else if (this.attributeName.equals("NETWORK_TYPE")) {
				_cytoShape.setToolTipText("<html>To create a nested network<br>"
				                          + "drag and drop the network onto a node<br>"
										  + "to assign a nested network,<br>" 
										  + "or on the canvas to create a new node and<br>"
										  + "assign a nested network. </html>");
			}
		}

		this.setLayout(new GridLayout(1, 1));

		TitledBorder t2 = BorderFactory.createTitledBorder(title);
		this.add(_cytoShape);
		this.setBorder(t2);

		myDragSource = new DragSource();
		myDragSource.addDragSourceListener(new EntityDragSourceListener());
		myDragSource.createDefaultDragGestureRecognizer(_cytoShape, DnDConstants.ACTION_COPY, this);
		handler = (new BasicCytoShapeTransferHandler(this, null));
		this.setTransferHandler(handler);
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title The title to set.
	 *
	 */
	private void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Returns the DragSource.
	 */
	public DragSource getMyDragSource() {
		return myDragSource;
	}

	/**
	 * @param myDragSource The DragSource to set.
	 */
	private void setMyDragSource(DragSource myDragSource) {
		this.myDragSource = myDragSource;
	}

	/**
	 * @return Returns the icon associated with the shape
	 */
	public Icon getIcon() {
		return _image;
	}

	/**
	 * @param _image the icon to set for the shape
	 *
	 */
	private void setIcon(Icon _image) {
		this._image = _image;
	}

	/**
	 * @return Returns the image associated with the shape's icon
	 */
	public Image getImage() {
		return _img;
	}

	/**
	 * @param _img The _img to set.
	 */
	private void setImage(Image _img) {
		this._img = _img;
	}

	/**
	 * @return Returns the attributeName.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @param attributeName The attributeName to set.
	 */
	private void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * @return Returns the attributeValue.
	 */
	public String getAttributeValue() {
		return attributeValue;
	}

	/**
	 * @param attributeValue The attributeValue to set.
	 */
	private void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void dragGestureRecognized(DragGestureEvent e) {
		e.startDrag(DragSource.DefaultCopyDrop, handler.createTransferable(this));
	}

	private class EntityDragSourceListener extends DragSourceAdapter {
		public void dragEnter(DragSourceDragEvent dsde) {
			determineCursor(dsde);
		}

		public void dragOver(DragSourceDragEvent dsde) {
			determineCursor(dsde);
		}

		private void determineCursor(DragSourceDragEvent dsde) {
			DragSourceContext dsc = dsde.getDragSourceContext();
			Point compLoc = getLocationInsideComponent(app.getJFrame(), dsde.getLocation());

			if (compLoc != null) {
				Cursor newCursor = defaultCursorSetter.computeCursor(compLoc, dsde);

				if (newCursor == null) {
					newCursor = DragSource.DefaultCopyNoDrop;
				}

				dsc.setCursor(newCursor);
			} else {
				// check if on the drag source. We want to show ok cursor
				// for it:
				// sourceLoc now now in source component coordinates:
				Point paletteLoc = getLocationInsideComponent(dsc.getComponent(), dsde.getLocation());

				if (paletteLoc != null) {
					dsc.setCursor(DragSource.DefaultCopyDrop);
				} else {
					dsc.setCursor(DragSource.DefaultCopyNoDrop);
				}
			}
		}

		// return the point in component coordinates of a given point
		// in screen ccordinates only if the point is within the component.
		// Otherwise return null;
		private Point getLocationInsideComponent(Component desiredComp, Point screenLoc) {
			// loc now component location
			Point compLoc = new Point(screenLoc);
			SwingUtilities.convertPointFromScreen(compLoc, desiredComp);

			if (desiredComp.contains(compLoc)) {
				// the point is in desiredComp:
				return compLoc;
			}

			return null;
		}

		public void dragExit(DragSourceEvent dse) {
			dse.getDragSourceContext().setCursor(DragSource.DefaultCopyNoDrop);
		}
	}
}
