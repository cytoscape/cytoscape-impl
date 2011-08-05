/* File: RowTableCellEditor.java

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


package org.cytoscape.network.merge.internal.ui;

import java.util.HashMap;
import java.util.EventObject;

import java.awt.event.MouseEvent;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;

/**
 *
 * @author gjj
 */
// support different editors for each row in a column
class RowTableCellEditor implements TableCellEditor {
  protected HashMap<Integer, TableCellEditor> editors;

  protected TableCellEditor editor, defaultEditor;

  JTable table;

  /**
   * Constructs a EachRowEditor. create default editor
   *
   * @see TableCellEditor
   * @see DefaultCellEditor
   */
  public RowTableCellEditor(JTable table) {
    this.table = table;
    editors = new HashMap<Integer, TableCellEditor>();
    defaultEditor = new DefaultCellEditor(new JTextField());
  }

  /**
   * @param row
   *            table row
   * @param editor
   *            table cell editor
   */
  public void setEditorAt(int row, TableCellEditor editor) {
    editors.put(new Integer(row), editor);
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    return editor.getTableCellEditorComponent(table, value, isSelected,
        row, column);
  }

  public Object getCellEditorValue() {
    return editor.getCellEditorValue();
  }

  public boolean stopCellEditing() {
    return editor.stopCellEditing();
  }

  public void cancelCellEditing() {
    editor.cancelCellEditing();
  }

  public boolean isCellEditable(EventObject anEvent) {
    selectEditor((MouseEvent) anEvent);
    return editor.isCellEditable(anEvent);
  }

  public void addCellEditorListener(CellEditorListener l) {
    editor.addCellEditorListener(l);
  }

  public void removeCellEditorListener(CellEditorListener l) {
    editor.removeCellEditorListener(l);
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    selectEditor((MouseEvent) anEvent);
    return editor.shouldSelectCell(anEvent);
  }

  protected void selectEditor(MouseEvent e) {
    int row;
    if (e == null) {
      row = table.getSelectionModel().getAnchorSelectionIndex();
    } else {
      row = table.rowAtPoint(e.getPoint());
    }
    editor = (TableCellEditor) editors.get(new Integer(row));
    if (editor == null) {
      editor = defaultEditor;
    }
  }
}
