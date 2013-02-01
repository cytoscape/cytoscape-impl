package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
