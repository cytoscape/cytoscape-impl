package org.cytoscape.work.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Timer;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults.TaskIcon;
import org.cytoscape.work.swing.StatusBarPanelFactory;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Manages the task's status bar's UI at the bottom of the Cytoscape desktop.
 */
@SuppressWarnings("serial")
public class TaskStatusBar extends JPanel implements StatusBarPanelFactory {

	public static final String TASK_HISTORY_CLICK = "task-history-click";

	private static final int CLEAR_DELAY_MS = 5000;

	private final JLabel titleIconLabel;
	private final JLabel titleLabel;
	private final JButton showBtn;
	
	private final Timer clearingTimer;

	public TaskStatusBar(final CyServiceRegistrar serviceRegistrar) {
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
		titleIconLabel = new JLabel();
		titleIconLabel.setFont(iconManager.getIconFont(14.0f));
		
		titleLabel = new JLabel();
		LookAndFeelUtil.makeSmall(titleLabel);
		
		showBtn = new JButton(GUIDefaults.TaskIcon.TASKS.getText());
		showBtn.setFont(iconManager.getIconFont(14.0f));
		
		if (LookAndFeelUtil.isAquaLAF())
			showBtn.putClientProperty("JButton.buttonType", "gradient");
		
		showBtn.setToolTipText("Show Tasks");
		showBtn.setFocusPainted(false);
		showBtn.addActionListener(evt -> {
			showBtn.setText(GUIDefaults.TaskIcon.TASKS.getText());
			showBtn.setForeground(GUIDefaults.TaskIcon.TASKS.getForeground());
			firePropertyChange(TASK_HISTORY_CLICK, null, null);
		});
		
		clearingTimer = new Timer(CLEAR_DELAY_MS, evt -> clearStatusBar());
		clearingTimer.setRepeats(false);
		
		final int w = Math.max(48, showBtn.getPreferredSize().width);

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(showBtn, w, w, w)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(titleIconLabel)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(titleLabel)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(showBtn, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(titleIconLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		setPreferredSize(new Dimension(100, getPreferredSize().height));
	}

	public void setTitle(final FinishStatus.Type finishType, final String title) {
		TaskIcon icon = GUIDefaults.getIcon(finishType);
		String type = null;

		if (finishType != null) {
			switch (finishType) {
				case SUCCEEDED:
					type = "finished";
					break;
				case FAILED:
					type = "error";
					break;
				case CANCELLED:
					type = "cancelled";
					break;
			}
		}
		
		setTitle(type, icon, title);
	}

	public void setTitle(final Level level, final String title) {
		TaskIcon icon = GUIDefaults.getIcon(level);
		String type = null;

		if (level != null) {
			switch (level) {
				case INFO:
					type = "info";
					break;
				case WARN: 
					type = "warn";
					break;
				case ERROR:
					type = "error";
					break;
			}
		}
		
		setTitle(type, icon, title);
	}
	
	public void setTitle(final String type, final TaskIcon icon, final String title) {
		String iconText = null;
		Color iconColor = null;
		
		if (icon != null) {
			iconText = icon.getText();
			iconColor = icon.getForeground();
			
			// Set button icon based on error/warning status
			if ( (type.equals("error") && !icon.getText().equalsIgnoreCase(showBtn.getText()) || 
					type.equals("warn") && TaskIcon.TASKS.getText().equalsIgnoreCase(showBtn.getText())) ) {
				showBtn.setText(iconText);
				showBtn.setForeground(iconColor);
			}
		}
		
		titleIconLabel.setText(title == null || title.isEmpty() ? null : iconText);
		titleLabel.setText(title);
		
		if (iconColor != null)
			titleIconLabel.setForeground(iconColor);
		
		clearingTimer.restart();
	}
	
	private void clearStatusBar() {
		titleIconLabel.setText(null);
		titleLabel.setText("");
	}

	@Override
	public JPanel createTaskStatusPanel() {
		return this;
	}
}
