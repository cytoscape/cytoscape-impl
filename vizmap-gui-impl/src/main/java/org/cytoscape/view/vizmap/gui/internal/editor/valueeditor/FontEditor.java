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
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public class FontEditor extends JDialog implements ValueEditor<Font> {
	private final static long serialVersionUID = 1202339876814272L;

	private Font font;
	private final FontChooser chooser;
	
	public FontEditor() {
		super();
		this.setModal(true);
		this.chooser = new FontChooser();
		init();
	}

	/**
	 * Display editor for the given data type.
	 */
	public Font showEditor(final Component parent, final Font initialValue) {
		this.setLocationRelativeTo(parent);
		setModal(true);
		font = initialValue;
		if(font != null)
			chooser.setSelectedFont(font.deriveFont(1F));
		setVisible(true);
		return getThisFont();
	}

	private void init() {
		this.setTitle("Please select new font...");

		if (font != null)
			chooser.setSelectedFont(font.deriveFont(1F));

		JPanel butPanel = new JPanel(false);

		// buttons - OK/Cancel
		JButton okBut = new JButton("OK");
		okBut.addActionListener(new AbstractAction() {
			private final static long serialVersionUID = 1202339876795625L;

			public void actionPerformed(ActionEvent e) {
				font = chooser.getSelectedFont().deriveFont(12F);
				dispose();
			}
		});

		JButton cancelBut = new JButton("Cancel");
		cancelBut.addActionListener(new AbstractAction() {
			private final static long serialVersionUID = 1202339876804773L;

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		butPanel.add(okBut);
		butPanel.add(cancelBut);

		Container content = getContentPane();
		content.setLayout(new BorderLayout());

		content.add(chooser, BorderLayout.CENTER);
		content.add(butPanel, BorderLayout.SOUTH);
		pack();

	}

	private Font getThisFont() {
		return font;
	}

	public Class<Font> getValueType() {
		return Font.class;
	}

}
