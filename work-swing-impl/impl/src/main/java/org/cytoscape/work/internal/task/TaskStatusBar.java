package org.cytoscape.work.internal.task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Manages the task's status bar's UI at the bottom of the Cytoscape desktop.
 */
class TaskStatusBar extends JPanel {
	final JLabel titleLabel = new JLabel();
	final RoundedProgressBar progressBar = new RoundedProgressBar();

	public TaskStatusBar(final TaskWindow window) {
		super.setOpaque(false);
		titleLabel.setOpaque(false);
		final JButton showBtn = new JButton(new ImageIcon(getClass().getResource("/images/tasks-icon.png")));
		showBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.show();
			}
		});
		showBtn.setToolTipText("Show tasks");
		showBtn.setPreferredSize(new Dimension(20, 20));
		showBtn.setMaximumSize(new Dimension(20, 20));


		progressBar.setProgress(-1.0f);
		progressBar.setPreferredSize(new Dimension(150, 7));
		progressBar.setVisible(false);

        final SpringLayout layout = new SpringLayout();
		super.setLayout(layout);

		super.add(showBtn);
		super.add(titleLabel);
		super.add(progressBar);

		progressBar.setMaximumSize(progressBar.getPreferredSize()); // don't make the progress bar stretchable

        layout.putConstraint(SpringLayout.WEST, showBtn, 10, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.WEST, titleLabel, 10, SpringLayout.EAST, showBtn);
        layout.putConstraint(SpringLayout.WEST, progressBar, 10, SpringLayout.EAST, titleLabel);
        layout.putConstraint(SpringLayout.EAST, this, 10, SpringLayout.EAST, progressBar);

        layout.putConstraint(SpringLayout.NORTH, showBtn, 10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.NORTH, titleLabel, 12, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.NORTH, progressBar, 16, SpringLayout.NORTH, this);

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

	public void setProgress(final float progress) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setProgress(progress);
				}
			});
			return;
		}

		if (!progressBar.isVisible())
			progressBar.setVisible(true);

		if (0.0f <= progress && progress <= 1.0f)
			progressBar.setProgress(progress);
		else
			progressBar.setIndeterminate();
	}

	public void hideProgress() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					hideProgress();
				}
			});
			return;
		}

		if (progressBar.isVisible())
			progressBar.setVisible(false);
	}

	public void resetStatusBar() {
		titleLabel.setIcon(null);
		titleLabel.setText("");
		progressBar.setProgress(-1.0f);
		progressBar.setVisible(false);
	}
}
