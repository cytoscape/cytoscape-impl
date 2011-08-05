/**
 * @PROJECT.FULLNAME@ @VERSION@ License.
 *
 * Copyright @YEAR@ L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

/**
 * ComboBoxPropertyEditor. <br>
 * 
 */
public class CyComboBoxPropertyEditor extends AbstractPropertyEditor {

	private final static long serialVersionUID = 120233986911049L;

	/*
	 * Color & Font theme
	 */
	private static final Color BACKGROUND = Color.white;
	private static final Color NOT_SELECTED = new Color(51, 51, 255, 150);
	private static final Color SELECTED = Color.red;
	private static final Font SELECTED_FONT = new Font("SansSerif", Font.BOLD, 12);
	private Object oldValue;
	private Icon[] icons;

	/**
	 * Creates a new CyComboBoxPropertyEditor object.
	 */
	public CyComboBoxPropertyEditor() {
		editor = new JComboBox() {
			private final static long serialVersionUID = 1213748837100875L;

			public void setSelectedItem(Object anObject) {
				oldValue = getSelectedItem();
				super.setSelectedItem(anObject);
			}
		};

		final JComboBox combo = (JComboBox) editor;

		combo.setRenderer(new Renderer());
		
		combo.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

				try {
					if ((combo.getSelectedItem() == null)
							&& (combo.getItemCount() != 0)) {
						combo.setSelectedIndex(0);
						CyComboBoxPropertyEditor.this.firePropertyChange(
								oldValue, combo.getItemAt(0));
					} else
						CyComboBoxPropertyEditor.this.firePropertyChange(
								oldValue, combo.getSelectedItem());

					System.out
							.println("!!!!!!!!Event fired from Combobox Editor: "
									+ combo.getSelectedItem());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});
		combo.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					CyComboBoxPropertyEditor.this.firePropertyChange(oldValue,
							combo.getSelectedItem());
			}
		});
		combo.setSelectedIndex(-1);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getValue() {
		Object selected = ((JComboBox) editor).getSelectedItem();

		if (selected instanceof Value)
			return ((Value) selected).value;
		else

			return selected;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param value
	 *            DOCUMENT ME!
	 */
	public void setValue(Object value) {
		JComboBox combo = (JComboBox) editor;
		Object current = null;
		int index = -1;

		for (int i = 0, c = combo.getModel().getSize(); i < c; i++) {
			current = combo.getModel().getElementAt(i);

			if ((value == current)
					|| ((current != null) && current.equals(value))) {
				index = i;

				break;
			}
		}

		((JComboBox) editor).setSelectedIndex(index);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param values
	 *            DOCUMENT ME!
	 */
	public void setAvailableValues(Object[] values) {
		((JComboBox) editor).setModel(new DefaultComboBoxModel(values));

		if (((JComboBox) editor).getItemCount() != 0) {
			((JComboBox) editor).setSelectedIndex(0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param icons
	 *            DOCUMENT ME!
	 */
	public void setAvailableIcons(Icon[] icons) {
		this.icons = icons;
	}

	public class Renderer extends DefaultListCellRenderer {
		private final static long serialVersionUID = 1213748837110925L;

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list,
					(value instanceof Value) ? ((Value) value).visualValue
							: value, index, isSelected, cellHasFocus);

			if ((icons != null) && (index >= 0) && component instanceof JLabel)
				((JLabel) component).setIcon(icons[index]);

			if ((index >= 0) && component instanceof JLabel) {
			}

			if (value == null) {
				component = new JLabel("Select Value!");
				component.setForeground(SELECTED);
				((JLabel) component).setFont(new Font("SansSerif", Font.BOLD,
						12));
			} else if ((value == null)
					&& (((JComboBox) editor).getItemCount() != 0)) {
				component = new JLabel(((JComboBox) editor).getItemAt(0)
						.toString());
				component.setForeground(SELECTED);
				((JLabel) component).setFont(new Font("SansSerif", Font.BOLD,
						12));
			}

			if (isSelected) {
				component.setForeground(SELECTED);
				((JLabel) component).setFont(SELECTED_FONT);
			} else
				component.setForeground(NOT_SELECTED);

			component.setBackground(BACKGROUND);

			return component;
		}
	}

	public static final class Value {
		private Object value;
		private Object visualValue;

		public Value(Object value, Object visualValue) {
			this.value = value;
			this.visualValue = visualValue;
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;

			if ((value == o) || ((value != null) && value.equals(o)))
				return true;

			return false;
		}

		public int hashCode() {
			return (value == null) ? 0 : value.hashCode();
		}
	}
}
