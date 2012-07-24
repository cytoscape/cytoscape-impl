package org.cytoscape.internal.actions;

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
