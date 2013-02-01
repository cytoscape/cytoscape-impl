package org.cytoscape.internal.actions;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.command.AvailableCommands;

public class CommandListUI extends JDialog {

	private static final long serialVersionUID = -2813178727566532080L;

	private static final String TITLE = "Available CyCommands";
	private static final String[] COLUMNS = { "Namespace", "Command", "Arguments" };

	private final AvailableCommands availableCommands;

	CommandListUI(final AvailableCommands availableCommands) {
		this.availableCommands = availableCommands;

		initUI();
	}

	private void initUI() {
		this.setTitle(TITLE);
		final Container cp = this.getContentPane();
		cp.setBackground(Color.WHITE);
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		final JScrollPane pane = new JScrollPane();
		pane.setOpaque(false);
		pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		final JTable commandTable = new JTable();
		commandTable.setBackground(Color.white);
		final DefaultTableModel model = new DefaultTableModel() {
			private static final long serialVersionUID = -1338792823143882191L;

			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
			
			@Override
			public int getColumnCount() {
				return COLUMNS.length;
			}
			
			@Override
			public String getColumnName(final int columnIdx) {
				return COLUMNS[columnIdx];
			}
		};

		commandTable.getTableHeader().setPreferredSize(new Dimension(500, 30));
		commandTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));
		final JLabel rend = (JLabel) commandTable.getTableHeader().getDefaultRenderer();
		rend.setHorizontalAlignment(SwingConstants.CENTER);

		commandTable.getTableHeader().setReorderingAllowed(false);

		for (String namespace : availableCommands.getNamespaces()) {
			for (String command : availableCommands.getCommands(namespace)) {

				final Stack<String> row = new Stack<String>();
				row.add(namespace);
				row.add(command);

				final StringBuilder builder = new StringBuilder();
				for (String arg : availableCommands.getArguments(namespace, command)) {
					builder.append(arg);
					builder.append(", ");
				}
				final String argString = builder.toString();
				if (!argString.isEmpty())
					row.add(argString.substring(0, argString.length() - 2));

				model.addRow(row);
			}
		}

		commandTable.setModel(model);

		commandTable.getColumn(COLUMNS[0]).setPreferredWidth(120);
		commandTable.getColumn(COLUMNS[1]).setPreferredWidth(350);
		commandTable.getColumn(COLUMNS[2]).setPreferredWidth(350);

		this.setMinimumSize(new Dimension(800, 500));
		pane.setViewportView(commandTable);
		cp.add(pane);
		pack();
	}
}
