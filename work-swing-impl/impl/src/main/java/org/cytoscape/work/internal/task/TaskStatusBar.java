package org.cytoscape.work.internal.task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.cytoscape.work.swing.TaskStatusPanelFactory;

/**
 * Manages the task's status bar's UI at the bottom of the Cytoscape desktop.
 */
public class TaskStatusBar extends JPanel implements TaskStatusPanelFactory {
	final JLabel titleLabel = new JLabel();

	public TaskStatusBar() {
		super.setOpaque(false);
		titleLabel.setOpaque(false);
		final JButton showBtn = new JButton(new ImageIcon(getClass().getResource("/images/tasks-icon.png")));
		showBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//window.show();
			}
		});
		showBtn.setToolTipText("Show tasks");
		showBtn.setPreferredSize(new Dimension(20, 20));
		showBtn.setMaximumSize(new Dimension(20, 20));


		final SpringLayout layout = new SpringLayout();
		super.setLayout(layout);

		super.add(showBtn);
		super.add(titleLabel);

		layout.putConstraint(SpringLayout.WEST, showBtn, 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, titleLabel, 10, SpringLayout.EAST, showBtn);
		layout.putConstraint(SpringLayout.WEST, this, 10, SpringLayout.EAST, titleLabel);

		layout.putConstraint(SpringLayout.NORTH, showBtn, 10, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.NORTH, titleLabel, 12, SpringLayout.NORTH, this);

		super.setPreferredSize(new Dimension(100, 40));
	}

	public void setTitleIcon(final Icon icon) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setTitleIcon(icon);
				}
			});
			return;
		}

		titleLabel.setIcon(icon);
	}

	public void setTitle(final String title) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setTitle(title);
				}
			});
			return;
		}

		titleLabel.setText(title);
	}
	
	public void resetStatusBar() {
		titleLabel.setIcon(null);
		titleLabel.setText("");
	}

	public JPanel createTaskStatusPanel() {
		return this;
	}
}
