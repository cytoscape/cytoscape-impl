package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id$
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


import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.LookAndFeelUtil;


public class DeletionDialog extends JDialog {
	
	private static final long serialVersionUID = -3913969667219654220L;
	
	private JList<String> columnList;
	private JButton cancelButton;
	private JButton deleteButton;
	private JScrollPane deletionPane;
	
	private final CyTable table;

	protected DeletionDialog(final Frame parent, final CyTable table) {
		super(parent, "Delete Columns", ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.table = table;

		initComponents();
	}

	@SuppressWarnings("serial")
	private void initComponents() {
		final JLabel deleteLabel = new JLabel("Select columns to be deleted:");
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		final TreeSet<CyColumn> deletableColumns = new TreeSet<>(new Comparator<CyColumn>() {
			@Override
			public int compare(CyColumn c1, CyColumn c2) {
				return collator.compare(c1.getName(), c2.getName());
			}
		});
		
		for (CyColumn col : table.getColumns()) {
			if (!col.isImmutable())
				deletableColumns.add(col);
		}
		
		final DefaultListModel<String> listModel = new DefaultListModel<>();
		
		for (CyColumn col : deletableColumns)
			listModel.addElement(col.getName());
		
		columnList = new JList<>(listModel);
		deletionPane = new JScrollPane(columnList);

		deleteButton = new JButton(new AbstractAction("Delete") {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteButtonActionPerformed(e);
			}
		});
		deleteButton.getAction().setEnabled(false);
		
		cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(deleteButton, cancelButton);
		
		columnList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					final int[] selectedIndices = columnList.getSelectedIndices();
					deleteButton.getAction().setEnabled(selectedIndices != null && selectedIndices.length > 0);
				}
			}
		});
		
		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(deleteLabel)
				.addComponent(deletionPane, DEFAULT_SIZE, 320, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(deleteLabel)
				.addComponent(deletionPane, DEFAULT_SIZE, 240, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
		
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), deleteButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(deleteButton);
		
		pack();
	}

	private void deleteButtonActionPerformed(ActionEvent e) {
		final List<String> selected = columnList.getSelectedValuesList();
		
		for (String name : selected)
			table.deleteColumn(name);
		
		dispose();
	}
}
