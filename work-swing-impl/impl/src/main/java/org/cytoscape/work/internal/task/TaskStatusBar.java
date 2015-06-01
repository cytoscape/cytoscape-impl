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

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.TaskStatusPanelFactory;

/**
 * Manages the task's status bar's UI at the bottom of the Cytoscape desktop.
 */
public class TaskStatusBar extends JPanel implements TaskStatusPanelFactory {

	public static final String TASK_HISTORY_CLICK = "task-history-click";

	private static final int CLEAR_DELAY_MS = 5000;

	final JLabel titleLabel = new JLabel();
	final Timer clearingTimer;
	final JButton showBtn;
	final Icon defaultIcon;

	public TaskStatusBar() {
		super.setOpaque(false);
		titleLabel.setOpaque(false);
		defaultIcon = new ImageIcon(getClass().getResource("/images/tasks-icon.png"));
		showBtn = new JButton(defaultIcon);
		showBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showBtn.setIcon(defaultIcon);
				firePropertyChange(TASK_HISTORY_CLICK, null, null);
			}
		});
		showBtn.setToolTipText("Show tasks");
		showBtn.setPreferredSize(new Dimension(20, 20));
		showBtn.setMaximumSize(new Dimension(20, 20));

		clearingTimer = new Timer(CLEAR_DELAY_MS, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearStatusBar();
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

	public void setTitle(final FinishStatus.Type finishType, final String title) {
		String type = null;
		if (finishType != null) {
			switch (finishType) {
			case SUCCEEDED: type = "finished"; break;
			case FAILED:    type = "error"; break;
			case CANCELLED: type = "cancelled"; break;
			}
		}
		this.setTitle(type, title);
	}


	public void setTitle(final TaskMonitor.Level level, final String title) {
		String type = null;
		if (level != null) {
			switch (level) {
			case INFO:  type = "info"; break;
			case WARN:  type = "warn"; break;
			case ERROR: type = "error"; break;
			}
		}
		this.setTitle(type, title);
	}
	
	public void setTitle(final String type, final String title) {
		Icon icon = null;
		if (type != null) {
			icon = TaskDialog.ICONS.get(type);
			// set button icon based on error/warning status
			if((type.equals("error") && showBtn.getIcon() != icon || 
					type.equals("warn") && showBtn.getIcon() == defaultIcon) ) {
				showBtn.setIcon(icon);
			}
		}
		this.setTitle(icon, title);
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
	
	private void clearStatusBar() {
		titleLabel.setIcon(null);
		titleLabel.setText("");
	}

	public JPanel createTaskStatusPanel() {
		return this;
	}
}