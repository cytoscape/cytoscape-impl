package org.cytoscape.filter.internal.widgets.autocomplete.view;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


import javax.swing.*;

import org.cytoscape.filter.internal.widgets.autocomplete.index.TextIndex;


/**
 * Factory for creating TextIndexComboBoxes.
 *
 * @author Ethan Cerami
 */
public class ComboBoxFactory {
	private ComboBoxFactory() {
	}

	/**
	 * Creates a new TextIndex Combo Box.
	 *
	 * @param textIndex               Text Index Object.
	 * @param popupWindowSizeMultiple Indicates the size multiple used
	 *                                to resize the popup window.
	 * @return TextIndexComboBox Object.
	 * @throws IllegalAccessException Could not set Cross Platform
	 *                                Look and Feel.
	 * @throws UnsupportedLookAndFeelException
	 *                                Could not set Cross Platform
	 *                                Look and Feel.
	 * @throws InstantiationException Could not set Cross Platform
	 *                                Look and Feel.
	 * @throws ClassNotFoundException Could not set Cross Platform
	 *                                Look and Feel.
	 */
	public static TextIndexComboBox createTextIndexComboBox(TextIndex textIndex,
	                                                        double popupWindowSizeMultiple)
	    throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
	               ClassNotFoundException {
		// Obtain the current L & F
		LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();

		//  Set to Default Java Cross Platform L & F
		//  UIManager.setLookAndFeel
		// ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

		//  Create the ToDelete
		TextIndexComboBox comboBox = new TextIndexComboBox(textIndex, popupWindowSizeMultiple);

		//  Return to original L & F
		UIManager.setLookAndFeel(currentLookAndFeel);

		return comboBox;
	}
}
