/*
 * Created on May 31, 2005
 *
 */
package org.cytoscape.editor.internal.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureListener;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 


/**
 * transfer handler for shapes that are dragged from the palette onto the canvas.
 * Creates appropriate data flavor and transferrable.
 * part of drag/drop editor framework.
 *
 * @author Allan Kuchinsky
 * @version 1.0
 * @see cytoscape.editor.GraphicalEntity 
 * @see cytoscape.editor.impl.BasicCytoShapeEntity
 *
 */
public class BasicCytoShapeTransferHandler extends TransferHandler {
	// MLC 09/14/06:
	private static final long serialVersionUID = 778042405547689517L;
	private static final Logger logger = LoggerFactory.getLogger(BasicCytoShapeTransferHandler.class); 
	DataFlavor basicCytoShapeFlavor;
	DragGestureListener _cytoShape;

	/**
	 * @return Returns the _attributeName.
	 */
	public String get_attributeName() {
		return _attributeName;
	}

	/**
	 * @return Returns the _attributeValue.
	 */
	public String get_attributeValue() {
		return _attributeValue;
	}

	String _attributeName;
	String _attributeValue;
	Object[] _args;

	/**
	 * creates a DataFlavor for the BasicCytoShapeEntity class
	 *
	 */
	public BasicCytoShapeTransferHandler() {
		try {
			basicCytoShapeFlavor = new DataFlavor(BasicCytoShapeEntity.class, "BasicCytoShapeEntity");
		} catch (Exception e) {
			logger.warn("Unable to create data flavor for BasicCytoShapeEntity",e);
		}
	}

	/**
	 * creates a DataFlavor and sets instance variables for a BasicCytoShapeEntity that is
	 * added to the palette
	 * @param cytoShape shape that is added to the palette
	 * @param args arbitrary list of arguments that can be passed in
	 */
	public BasicCytoShapeTransferHandler(BasicCytoShapeEntity cytoShape, Object[] args) {
		try {
			basicCytoShapeFlavor = new DataFlavor(BasicCytoShapeEntity.class, "BasicCytoShapeEntity");
		} catch (Exception e) {
			logger.warn("Unable to create data flavor for BasicCytoShapeEntity",e);
		}

		_cytoShape = cytoShape;
		_args = args;
		_attributeName = cytoShape.getAttributeName();
		_attributeValue = cytoShape.getAttributeValue();
	}

	/**
	 * @return Returns the _args.
	 */
	public Object[] get_args() {
		return _args;
	}

	/**
	 * sets the _args instance variable
	 * @param _args The _args to set.
	 */
	public void set_args(Object[] _args) {
		this._args = _args;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param c DOCUMENT ME!
	 * @param t DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean importData(JComponent c, Transferable t) {

		if (canImport(c, t.getTransferDataFlavors())) {
		}

		return false;
	}

	// AJK: 11/13/05 BEGIN
	/**
	 *  DOCUMENT ME!
	 *
	 * @param val DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String exportString(Object val) {
		if (val instanceof BasicCytoShapeEntity) {
			return ((BasicCytoShapeEntity) val).getTitle();
		} else {
			return null;
		}
	}

	// AJK: 11/13/05 END
	/**
	 *  DOCUMENT ME!
	 *
	 * @param c DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Transferable createTransferable(JComponent c) {
		return new BasicCytoShapeTransferable(c);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param c DOCUMENT ME!
	 * @param flavors DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			if (basicCytoShapeFlavor.equals(flavors[i])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * packages the BasicCytoShapeEntity for transfer upon a drag/drop operation
	 * @author Allan Kuchinsky
	 * @version 1.0
	 *
	 *
	 */
	class BasicCytoShapeTransferable implements Transferable {
		private BasicCytoShapeEntity _cytoShape;

		BasicCytoShapeTransferable(JComponent obj) {
			if (obj instanceof BasicCytoShapeEntity) {
				_cytoShape = (BasicCytoShapeEntity) obj;
				_attributeName = _cytoShape.getAttributeName();
				_attributeValue = _cytoShape.getAttributeValue();
			}
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}

			return exportString(_cytoShape);
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { basicCytoShapeFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return basicCytoShapeFlavor.equals(flavor);
		}
	}

	/**
	 * @return Returns the _cytoShape.
	 */
	public DragGestureListener get_cytoShape() {
		return _cytoShape;
	}

	/**
	 * sets the instance variable for a BasicCytoShapeEntity
	 * @param shape The _cytoShape to set.
	 */
	public void set_cytoShape(DragGestureListener shape) {
		_cytoShape = shape;
	}
}
