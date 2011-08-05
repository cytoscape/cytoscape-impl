/*
  File: FontChooser.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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

//--------------------------------------------------------------------------
// $Revision: 12968 $
// $Date: 2008-02-06 15:34:25 -0800 (Wed, 06 Feb 2008) $
// $Author: mes $
//--------------------------------------------------------------------------
package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.cytoscape.view.vizmap.gui.internal.cellrenderer.FontCellRenderer;

/**
 * Defines a generalized font chooser class. FontChooser contains three
 * components to display font face selection.
 */
public class FontChooser extends JPanel {

	private final static long serialVersionUID = 1202339876728781L;

	private Font selectedFont;

	protected DefaultComboBoxModel fontFaceModel;
	protected JComboBox fontSelector;

	protected static final int DEF_SIZE = 12;
	private static final Dimension SIZE = new Dimension(350, 40);
	protected static Font[] scaledFonts;
	protected static final Font DEF_FONT = new Font("SansSerif", Font.PLAIN, DEF_SIZE);

	static {
		scaledFonts = scaleFonts(GraphicsEnvironment
				.getLocalGraphicsEnvironment().getAllFonts(), DEF_SIZE);
	}

	/**
	 * Create a FontChooser to choose between all fonts available on the system.
	 */
	public FontChooser() {
		this(scaledFonts, DEF_FONT);
	}

	/**
	 * Create a FontChooser to choose between the given array of fonts.
	 */
	public FontChooser(Font[] srcFonts, Font def) {
		this.setLayout(new BorderLayout());
		Font[] displayFonts = scaledFonts;

		if (srcFonts != scaledFonts)
			displayFonts = scaleFonts(srcFonts, DEF_SIZE);

		this.fontFaceModel = new DefaultComboBoxModel(displayFonts);
		
		this.fontSelector = new JComboBox(fontFaceModel);
		this.fontSelector.setRenderer(new FontCellRenderer());
		this.fontSelector.setPreferredSize(SIZE);
		this.fontSelector.setMinimumSize(SIZE);
		this.setPreferredSize(SIZE);
		this.setMinimumSize(SIZE);
		

		// set the prototype display for the combo box
		fontSelector.addItemListener(new FontFaceSelectionListener());

		// set the currently selected face, default if null
		if (def == null)
			this.selectedFont = DEF_FONT;
		else
			this.selectedFont = def.deriveFont(1F);

		fontSelector.setEditable(true); // so that we may select the default font
		fontSelector.setSelectedItem(this.selectedFont);
		fontSelector.setEditable(false); // so that users aren't allowed to edit the list

		add(fontSelector, BorderLayout.CENTER);
	}

	
	public JComboBox getFaceComboBox() {
		return fontSelector;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Font getSelectedFont() {
		return selectedFont;
	}

	public void setSelectedFont(Font font) {
		this.selectedFont = font;
	}

	private class FontFaceSelectionListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				JComboBox source = (JComboBox) e.getItemSelectable();
				selectedFont = (Font) source.getSelectedItem();
			}
		}
	}

	private static Font[] scaleFonts(Font[] inFonts, float size) {
		Font[] outFonts = new Font[inFonts.length];
		int i = 0;

		for (Font f : inFonts)
			outFonts[i++] = f.deriveFont(size);

		return outFonts;
	}
}
