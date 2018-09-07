package org.cytoscape.work.internal.tunables;

import static org.cytoscape.work.internal.tunables.utils.ViewUtil.invokeOnEDT;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

/**
 * Makes the species combo box searchable.
 */
public class JComboBoxDecorator {

	static List<?> previousEntries = new ArrayList<>();

	public static void decorate(final JComboBox<?> jcb, final List<?> entries, boolean begins) {
		Object selectedItem = jcb.getSelectedItem();
		jcb.setEditable(true);
		// jcb.setModel(new DefaultComboBoxModel(entries.toArray()));

		final JTextField textField = (JTextField)jcb.getEditor().getEditorComponent();
		jcb.setSelectedItem(selectedItem);

		textField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				invokeOnEDT(() -> {
					int currentCaretPosition = textField.getCaretPosition();
					comboFilter(textField.getText(), jcb, entries, begins);
					textField.setCaretPosition(currentCaretPosition);
				});
			}
		});
	}

	/**
	 * Create a list of entries that match the user's entered text.
	 */
	private static void comboFilter(String enteredText, JComboBox<?> jcb, List<?> entries, boolean begins) {
		List<Object> entriesFiltered = new ArrayList<>();
		boolean changed = true;
		DefaultComboBoxModel<?> jcbModel = (DefaultComboBoxModel<?>) jcb.getModel();

		if (enteredText == null) {
			return;
		}

		for (Object entry : entries) {
			if (begins) {
				if (entry.toString().toLowerCase().startsWith(enteredText.toLowerCase())) {
					entriesFiltered.add(entry);
					// System.out.println(jcbModel.getIndexOf(entry));
				}
			} else {
				if (entry.toString().toLowerCase().contains(enteredText.toLowerCase())) {
					entriesFiltered.add(entry);
					// System.out.println(jcbModel.getIndexOf(entry));
				}
			}
		}

		if (previousEntries.size() == entriesFiltered.size()
				&& previousEntries.containsAll(entriesFiltered)) {
			changed = false;
		}

		if (changed && entriesFiltered.size() > 1) {
			previousEntries = entriesFiltered;
			jcb.setModel(new DefaultComboBoxModel(entriesFiltered.toArray()));
			jcb.setSelectedItem(enteredText);
			jcb.showPopup();
		} else if (entriesFiltered.size() == 1) {
			if (entriesFiltered.get(0).toString().equalsIgnoreCase(enteredText)) {
				previousEntries = new ArrayList<>();
				jcb.setSelectedItem(entriesFiltered.get(0));
				jcb.hidePopup();
			} else {
				previousEntries = entriesFiltered;
				jcb.setModel(new DefaultComboBoxModel(entriesFiltered.toArray()));
				jcb.setSelectedItem(enteredText);
				jcb.showPopup();
			}
		} else if (entriesFiltered.size() == 0) {
			previousEntries = new ArrayList<>();
			jcb.hidePopup();
		}
	}

}
