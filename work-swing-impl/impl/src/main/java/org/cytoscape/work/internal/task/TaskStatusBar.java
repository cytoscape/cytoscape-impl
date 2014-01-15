package org.cytoscape.work.internal.task;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.cytoscape.work.swing.TaskStatusPanelFactory;

/**
 * Manages the task's status bar's UI at the bottom of the Cytoscape desktop.
 */
public class TaskStatusBar extends JPanel implements TaskStatusPanelFactory {

	public static final String TASK_HISTORY_CLICK = "task-history-click";

	private static final int CLEAR_DELAY_MS = 5000;

	final JLabel titleLabel = new JLabel();
	final Timer clearingTimer;

	public TaskStatusBar() {
		super.setOpaque(false);
		titleLabel.setOpaque(false);
		final JButton showBtn = new JButton(new ImageIcon(getClass().getResource("/images/tasks-icon.png")));
		showBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firePropertyChange(TASK_HISTORY_CLICK, null, null);
			}
		});
		showBtn.setToolTipText("Show tasks");
		showBtn.setPreferredSize(new Dimension(20, 20));
		showBtn.setMaximumSize(new Dimension(20, 20));

		clearingTimer = new Timer(CLEAR_DELAY_MS, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetStatusBar();
			}
		});
		clearingTimer.setRepeats(false);

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

	public void setTitle(final Icon icon, final String title) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setTitle(icon, title);
				}
			});
			return;
		}

		titleLabel.setText(title);
		titleLabel.setIcon((title == null || title.length() == 0) ? null : icon);
		clearingTimer.restart();
	}
	
	private void resetStatusBar() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				titleLabel.setIcon(null);
				titleLabel.setText("");
			}
		});;
	}

	public JPanel createTaskStatusPanel() {
		return this;
	}
}