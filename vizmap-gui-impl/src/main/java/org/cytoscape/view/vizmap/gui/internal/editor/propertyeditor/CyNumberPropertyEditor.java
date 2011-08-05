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
package org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.cytoscape.view.vizmap.gui.internal.VizMapperMainPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.beans.editor.NumberPropertyEditor;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

/**
 *
 */
public class CyNumberPropertyEditor<T extends Number> extends NumberPropertyEditor {

    private static final Logger logger = LoggerFactory.getLogger(CyNumberPropertyEditor.class);

    private final VizMapperMainPanel panel;

    private Object currentValue;
    private Object selected;

    /**
     * Creates a new CyStringPropertyEditor object.
     */
    public CyNumberPropertyEditor(Class<T> type, final VizMapperMainPanel vmp) {
	super(type);
	panel = vmp;

	((JTextField) editor).addFocusListener(new FocusListener() {
	    public void focusGained(FocusEvent e) {

		logger.debug("Number Editor got val: " + currentValue);

		final Item item = (Item) panel.getSelectedItem();
		selected = item.getProperty().getDisplayName();
		setCurrentValue();
	    }

	    public void focusLost(FocusEvent arg0) {
		checkChange();
	    }
	});
    }

    private void setCurrentValue() {
	this.currentValue = super.getValue();
    }

    private void checkChange() {
	Number newValue = (Number) super.getValue();

	if (newValue.doubleValue() <= 0) {
	    newValue = 0;
	    currentValue = 0;
	    ((JTextField) editor).setText("0");
	    editor.repaint();
	}

	firePropertyChange(selected, newValue);
    }
}
