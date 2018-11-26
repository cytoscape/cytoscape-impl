package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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


import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import org.cytoscape.browser.internal.util.ValidatedObjectAndEditString;
import org.cytoscape.util.swing.LookAndFeelUtil;


@SuppressWarnings("serial")
public class MultiLineTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	
	private final static int CLICK_COUNT_TO_START = 2;
	private ResizableTextArea textArea;
	public static Object lastValueUserEntered;

	public MultiLineTableCellEditor() {
		textArea = new ResizableTextArea(this);
		textArea.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.disabledForeground")));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		Font f = textArea.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()); 		//   #4145
		textArea.setFont(f);
	}

	@Override
	public Object getCellEditorValue() {
		return textArea.getText().trim();
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		return !(e instanceof MouseEvent)
		       || (((MouseEvent) e).getClickCount() >= CLICK_COUNT_TO_START);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		stopCellEditing();
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
			final int row, final int column) {
		final String text = (value != null) ? ((ValidatedObjectAndEditString)value).getEditString() : "";
		textArea.setTable(table);
		textArea.setText(text);

		return textArea;
	}

	public static final String UPDATE_BOUNDS = "UpdateBounds";

	class ResizableTextArea extends JTextArea implements KeyListener {
		private JTable table;
		private MultiLineTableCellEditor parent;

		ResizableTextArea(final MultiLineTableCellEditor parent) {
			this.parent = parent;
			addKeyListener(this);
		}

		public void setTable(JTable t) {
			table = t;
		}

		@Override
		public void setText(String text) {
			super.setText(text);
			updateBounds();
		}

		@Override
		public void setBounds(int x, int y, int width, int height) {
			if (Boolean.TRUE.equals(getClientProperty(UPDATE_BOUNDS)))
				super.setBounds(x, y, width, height);
		}

		@Override
		public void addNotify() {
			super.addNotify();
			getDocument().addDocumentListener(listener);
		}

		@Override
		public void removeNotify() {
			getDocument().removeDocumentListener(listener);
			super.removeNotify();
		}

		DocumentListener listener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateBounds();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateBounds();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateBounds();
			}
		};

		private void updateBounds() {
			if (table == null) {
				System.err.println("table is null");
				return;
			}

			if (table.isEditing()) {
				Rectangle cellRect = table.getCellRect(table.getEditingRow(),
				                                       table.getEditingColumn(), false);
				Dimension prefSize = getPreferredSize();
				putClientProperty(UPDATE_BOUNDS, Boolean.TRUE);
				setBounds(getX(), getY(), Math.min(cellRect.width, prefSize.width),
				          Math.max(cellRect.height + prefSize.height, prefSize.height));
				putClientProperty(UPDATE_BOUNDS, Boolean.FALSE);
				validate();
			}
		}

		//
		// KeyListener Interface
		//

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			lastValueUserEntered = getCellEditorValue();
		}

		@Override
		public void keyPressed(final KeyEvent evt) {
			if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
				cancelCellEditing();
				this.transferFocus();
				return;
			}
			
			if (evt.getKeyCode() != KeyEvent.VK_ENTER)
				return;

			final int modifiers = evt.getModifiers();

			// We want to move to the next cell if Enter and no modifiers have been pressed:
			if (modifiers == 0) {
				stopCellEditing();
				this.transferFocus();
				return;
			}

			// We want to move to the previous cell if Shift+Enter have been pressed:
			if (modifiers == KeyEvent.VK_SHIFT) {
				stopCellEditing();
				this.transferFocusBackward();
				return;
			}

			// We want to insert a newline if Enter+Alt or Enter+Option (macOS) have been pressed:
			if (modifiers == KeyEvent.VK_ALT || (isMac() && evt.isAltDown())) {
				final int caretPosition = this.getCaretPosition();
				final StringBuilder text = new StringBuilder(this.getText());
				this.setText(text.insert(caretPosition, '\n').toString());
				this.setCaretPosition(caretPosition + 1);
			}
		}
	}
}
