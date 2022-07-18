package org.cytoscape.view.table.internal.impl;

import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class MultiLineTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	
	public static final String UPDATE_BOUNDS = "UpdateBounds";
	
	public static Object lastValueUserEntered;
	
	private ResizableTextArea textArea;
	private int lastRow = -1;
	private int thisRow = -1;

	public MultiLineTableCellEditor() {
		textArea = new ResizableTextArea();
		textArea.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.disabledForeground")));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		makeSmall(textArea);
	}

	@Override
	public Object getCellEditorValue() {
		// return textArea.getText().trim();
		return textArea.getText(); // We don't want to trim because we'll lose any intentional newlines
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		if (e instanceof KeyEvent) {
			var ke = (KeyEvent) e;
			var keyCode = ke.getKeyCode();
			
			if (LookAndFeelUtil.isMac()) {
				// macOS: ignore the COMMAND and CTRL keys
				if (keyCode == KeyEvent.VK_META || keyCode == KeyEvent.VK_CONTROL)
					return false;
			} else {
				// Windows | Linux: ignore CTRL and WINDOWS keys
				if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_WINDOWS)
					return false;
			}
			
			// Also prevents CTRL-C or COMMAND-C (copy) from editing the cell, in case the previous checks don't work
			int modifier = LookAndFeelUtil.isAquaLAF() ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;
			
			if (ke.getKeyCode() == KeyEvent.VK_C && (ke.getModifiersEx() & modifier) == modifier)
				return false;
			
			return super.isCellEditable(e);
		}
		
		return !(e instanceof MouseEvent) || ((MouseEvent) e).getClickCount() >= 2;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		stopCellEditing();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		lastRow = row;
		thisRow = row;
		
		String text = value != null ? ((ValidatedObjectAndEditString) value).getEditString() : "";
		textArea.setTable(table);
		textArea.setText(text);

		return textArea;
	}

	class ResizableTextArea extends JTextArea implements KeyListener {
		
		private JTable table;

		private DocumentListener listener = new DocumentListener() {
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
		
		ResizableTextArea() {
			addKeyListener(this);
			
			addCellEditorListener(new CellEditorListener() {
				@Override
				public void editingStopped(ChangeEvent e) {
					resetSize();
					lastRow = -1;
				}
				@Override
				public void editingCanceled(ChangeEvent e) {
					resetSize();
					lastRow = -1;
				}
			});
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

		private void updateBounds() {
			if (table == null || !table.isEditing())
				return;

			validate();
			
			Rectangle cellRect = table.getCellRect(table.getEditingRow(), table.getEditingColumn(), false);
			Dimension prefSize = getPreferredSize();
			putClientProperty(UPDATE_BOUNDS, Boolean.TRUE);
			setBounds(getX(), getY(), Math.min(cellRect.width, prefSize.width),
					Math.max(cellRect.height, prefSize.height));
			putClientProperty(UPDATE_BOUNDS, Boolean.FALSE);
			
			validate();

			table.setRowHeight(table.getEditingRow(), getSize().height);
		}
		
		private void resetSize() {
			if (lastRow < 0 || lastRow > table.getRowCount())
				return;
		
			table.setRowHeight(lastRow, table.getRowHeight());
			super.setBounds(getX(), getY(), getSize().width, table.getRowHeight());
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
		public void transferFocus() {
			if ((thisRow + 1) >= table.getRowCount()) {
				return;
			}
			super.transferFocus();
		}

		@Override
		public void transferFocusBackward() {
			if ((thisRow - 1) < 0)
				return;
			super.transferFocus();
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

			final int modifiers = evt.getModifiersEx();

			// We want to move to the next cell if Enter and no modifiers have been pressed:
			if (modifiers == 0) {
				stopCellEditing();
				this.transferFocus();
				return;
			}

			// We want to move to the previous cell if Shift+Enter have been pressed:
			if (evt.isShiftDown()) {
				stopCellEditing();
				this.transferFocusBackward();
				return;
			}

			// We want to insert a newline if Enter+Alt or Enter+Option (macOS) have been pressed:
			if (evt.isAltDown()) {
				final int caretPosition = this.getCaretPosition();
				final StringBuilder text = new StringBuilder(this.getText());
				this.setText(text.insert(caretPosition, '\n').toString());
				this.setCaretPosition(caretPosition + 1);
			}

		}
	}
}
