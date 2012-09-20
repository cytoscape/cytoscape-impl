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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;

import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.FontEditor;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor.Value;

public class CyFontPropertyEditor extends AbstractPropertyEditor {

	private Component parent;
	private FontEditor fontEditor;

	private Font currentValue;

	public CyFontPropertyEditor(final FontEditor fontEditor) {
		this.fontEditor = fontEditor;
		this.editor = new JPanel();
		editor.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				selectFont();
			}

			public void focusLost(FocusEvent arg0) {
			}
		});
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	@Override
	public void setValue(Object value) {
		if (value == null)
			this.currentValue = null;
		else if (value instanceof Value)
			this.currentValue = (Font) value;
	}

	@Override
	public Object getValue() {
		return currentValue;
	}

	protected void selectFont() {
		final Font font = (Font) super.getValue();
		final Font selectedFont = fontEditor.showEditor(parent, font);

		if (selectedFont != null) {
			Font oldFont = font;
			Font newFont = selectedFont;

			super.setValue(newFont);
			this.currentValue = newFont;
			firePropertyChange(oldFont, newFont);
		}
	}
}
