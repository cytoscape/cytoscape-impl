/*
 * Created on Jun 1, 2005
 *
 */
package org.cytoscape.editor.internal.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;


/**
 * base class for the PaletteListTransferHandler of the ShapePalette
 * taken from Java drag and drop tutorial, see http://java.sun.com/docs/books/tutorial/uiswing/misc/dnd.html
 */
public abstract class StringTransferHandler extends TransferHandler {
	protected abstract String exportString(JComponent c);

	protected abstract void importString(JComponent c, String str);

	protected abstract void cleanup(JComponent c, boolean remove);

	protected Transferable createTransferable(JComponent c) {
		return new StringSelection(exportString(c));
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param c DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
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
			try {
				String str = (String) t.getTransferData(DataFlavor.stringFlavor);
				importString(c, str);

				return true;
			} catch (UnsupportedFlavorException ufe) {
			} catch (IOException ioe) {
			}
		}

		return false;
	}

	protected void exportDone(JComponent c, Transferable data, int action) {
		cleanup(c, action == MOVE);
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
			if (DataFlavor.stringFlavor.equals(flavors[i])) {
				return true;
			}
		}

		return false;
	}
}
