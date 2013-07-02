package org.cytoscape.work.internal.task;

import javax.swing.*;
import java.awt.event.*;

class TaskStatusBar extends JPanel {
	public TaskStatusBar(final TaskWindow window) {
		final JButton btn = new JButton("Show Tasks");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.show();
			}
		});

		super.add(btn);
	}
}