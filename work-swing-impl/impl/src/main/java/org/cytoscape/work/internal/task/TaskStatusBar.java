package org.cytoscape.work.internal.task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class TaskStatusBar extends JPanel {
	final JLabel titleLabel = new JLabel();
	final RoundedProgressBar progressBar = new RoundedProgressBar();

	public TaskStatusBar(final TaskWindow window) {
		super.setOpaque(false);
		titleLabel.setOpaque(false);
		final JButton showBtn = new JButton("Show Tasks");
		showBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.show();
			}
		});

		progressBar.setProgress(-1.0f);
		progressBar.setPreferredSize(new Dimension(250, 7));
		progressBar.setVisible(false);

		super.setLayout(new FlowLayout(FlowLayout.LEFT));
		super.add(showBtn);
		super.add(titleLabel);
		super.add(progressBar);
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