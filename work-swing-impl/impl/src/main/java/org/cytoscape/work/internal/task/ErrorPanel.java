
/*
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

package org.cytoscape.work.internal.task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyledEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;


/**
 * Common UI element for displaying errors and stack traces.
 */
class ErrorPanel extends JPanel {
	private static final long serialVersionUID = 333614801L;

	/**
	 * The Error Object
	 */
	private Throwable t;

	/**
	 * A Human Readable Error Message
	 */
	private String userErrorMessage;

	/**
	 * Hint to user as to how to fix the error.
	 */
	private String tip;

	/**
	 * Flag to Show/Hide Error Details.
	 */
	private boolean showDetails = false;

	/**
	 * Show/Hide Details Button.
	 */
	private JButton detailsButton;

	/**
	 * Scroll Pane used to display Stack Trace Elements.
	 */
	private JScrollPane detailsPane;

	/**
	 * JDialog Owner.
	 */
	private Window owner;
    private static final String SHOW_TEXT = "Show Error Details";
	private static final String HIDE_TEXT = "Hide Error Details";

	/**
	 * Private Constructor.
	 *
	 * @param owner            Window owner.
	 * @param t                Throwable Object. May be null.
	 * @param userErrorMessage User Readable Error Message. May be null.
	 */
	ErrorPanel(Window owner, Throwable t, String userErrorMessage) {
		if (owner == null) {
			throw new IllegalArgumentException("owner parameter is null.");
		}

		this.owner = owner;
		this.t = t;
		this.userErrorMessage = userErrorMessage;
		initUI();
	}

	/**
	 * Private Constructor.
	 * 
	 * @param owner
	 *            Window owner.
	 * @param t
	 *            Throwable Object. May be null.
	 * @param userErrorMessage
	 *            User Readable Error Message. May be null.
	 * @param tip
	 *            Tip for user on how to recover from the error. May be null.
	 */
	ErrorPanel(final Window owner, final Throwable t, final String userErrorMessage, final String tip) {
		if (owner == null)
			throw new IllegalArgumentException("owner parameter is null.");
		
		this.owner = owner;
		this.t = t;
		this.userErrorMessage = userErrorMessage;
		this.tip = tip;
		initUI();
	}

    /**
	 * Initializes UI.
	 */
	private void initUI() {
		//  Use  Border Layout
		setLayout(new BorderLayout());

		//  Create North Panel with Error Message and Button.
		JPanel northPanel = createNorthPanel();
		add(northPanel, BorderLayout.NORTH);

		//  Create Center Panel with Error Details.
		JScrollPane centerPanel = createCenterPanel();
		add(centerPanel, BorderLayout.CENTER);

		//  Repack and validate the owner
		owner.pack();
		owner.validate();
	}

	/**
	 * Creates North Panel with Error Message and Details Button.
	 *
	 * @return JPanel Object.
	 */
	private JPanel createNorthPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());

		if (userErrorMessage == null) {
			userErrorMessage = new String("An Error Has Occurred.  " + "Please try again.");
		}

		//  Add an Error Icon
        Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        JLabel l = new JLabel(icon);
        l.setVerticalAlignment(SwingConstants.TOP);
        panel.add(l, BorderLayout.WEST);

		//  Add Error Message with Custom Font Properties
        JPanel textPanel = new JPanel();
        textPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JTextArea errorArea = createErrorTextArea(userErrorMessage);
        textPanel.add(errorArea);
        if (tip != null) {
            JTextPane tipPane = createTipTextPane(tip);
            textPanel.add(tipPane);
        }
        panel.add(textPanel, BorderLayout.CENTER);

        //  Conditionally Add Details Button
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 1));
		buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		conditionallyAddDetailsButton(buttonPanel);

        JPanel enclosingPanel = new JPanel();
        enclosingPanel.add(buttonPanel);
        enclosingPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.add(enclosingPanel, BorderLayout.EAST);

		return panel;
	}

    private JTextArea createErrorTextArea(String msg) {
        msg = StringWrap.wrap(msg, 50, "\n", false);
        JTextArea errorArea = new JTextArea(msg);
        errorArea.setEditable(false);
        errorArea.setOpaque(false);

        Font font = errorArea.getFont();
        errorArea.setFont(new Font(font.getFamily(), Font.BOLD, font.getSize()-1));
        errorArea.setBorder(new EmptyBorder(10, 10, 0, 10));
        return errorArea;
    }

    private JTextPane createTipTextPane(String tip) {
        //tip = StringWrap.wrap(tip, 50, "\n", false);
        JTextPane tipPane = new JTextPane();
	tipPane.setEditorKit(new StyledEditorKit());
	tipPane.setContentType("text/html");
	tipPane.setText(tip);
        tipPane.setEditable(false);
        tipPane.setOpaque(false);

        Font font = tipPane.getFont();
        //tipPane.setFont(new Font(font.getFamily(), Font.PLAIN, font.getSize()));
        tipPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return tipPane;
    }

	private void convertThrowable(final Throwable exception, DefaultMutableTreeNode root) {
		if (exception == null || exception.getStackTrace() == null)
			return;

		final String message;
		if (exception.getMessage() != null && exception.getMessage().length() != 0)
			message = String.format("%s: %s", exception.getClass().getName(), exception.getMessage());
		else
			message = exception.getClass().getName();

		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(message);
		root.add(node);

		StackTraceElement[] st = exception.getStackTrace();
		if (st != null)
			for (int i = 0; i < st.length; i++)
				node.add(new DefaultMutableTreeNode(st[i]));

		// Dig until the root of this exception.
		convertThrowable(exception.getCause(), root);
	}

    /**
	 * Creates Center Panel with Error Details.
	 *
	 * @return JScrollPane Object.
	 */
	private JScrollPane createCenterPanel() {
		detailsPane = new JScrollPane();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		convertThrowable(t, root);
		//  Create a JTree Object
		final JTree tree = new JTree(root);
		tree.setRootVisible(false);

		//  Open all Nodes
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
		tree.setBorder(new EmptyBorder(4, 10, 10, 10));
		detailsPane.setViewportView(tree);
		detailsPane.setPreferredSize(new Dimension(10, 150));

		//  By default, do not show
		detailsPane.setVisible(false);

		return detailsPane;
	}

	/**
	 * Adds a Show/Hide Details Button.
	 *
	 * @param panel JPanel Object.
	 */
	private void conditionallyAddDetailsButton(JPanel panel) {
		if ((t != null) && (t.getStackTrace() != null)) {
			detailsButton = new JButton(SHOW_TEXT);
			detailsButton.addActionListener(new ActionListener() {
					/**
					 * Toggle Show/Hide Error Details.
					 *
					 * @param e ActionEvent.
					 */
					public void actionPerformed(ActionEvent e) {
						showDetails = !showDetails;
						detailsPane.setVisible(showDetails);
						((Dialog) owner).setResizable(showDetails);

						if (showDetails) {
							detailsButton.setText(HIDE_TEXT);
						} else {
							detailsButton.setText(SHOW_TEXT);
						}

						owner.pack();
						owner.validate();
					}
				});
			detailsButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
			panel.add(detailsButton);
		}
	}
}
