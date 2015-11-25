package org.cytoscape.work.internal.task;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults.TaskIcon;
import org.cytoscape.work.swing.TaskStatusPanelFactory;

/**
 * Manages the task's status bar's UI at the bottom of the Cytoscape desktop.
 */
@SuppressWarnings("serial")
public class TaskStatusBar extends JPanel implements TaskStatusPanelFactory {

	public static final String TASK_HISTORY_CLICK = "task-history-click";

	private static final int CLEAR_DELAY_MS = 5000;

	final Timer clearingTimer;
	final JLabel titleIconLabel;
	final JLabel titleLabel;
	final JButton showBtn;

	public TaskStatusBar(final CyServiceRegistrar serviceRegistrar) {
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
		titleIconLabel = new JLabel();
		titleIconLabel.setFont(iconManager.getIconFont(14.0f));
		
		titleLabel = new JLabel();
		makeSmall(titleLabel);
		
		showBtn = new JButton(GUIDefaults.TaskIcon.TASKS.getText());
		showBtn.setFont(iconManager.getIconFont(14.0f));
		showBtn.putClientProperty("JButton.buttonType", "gradient"); // Aqua LAF only
		showBtn.putClientProperty("JComponent.sizeVariant", "small");
		showBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showBtn.setText(GUIDefaults.TaskIcon.TASKS.getText());
				showBtn.setForeground(GUIDefaults.TaskIcon.TASKS.getForeground());
				firePropertyChange(TASK_HISTORY_CLICK, null, null);
			}
		});
		showBtn.setToolTipText("Show Tasks...");
		showBtn.setFocusPainted(false);
		
		if (!LookAndFeelUtil.isAquaLAF())
			showBtn.setPreferredSize(new Dimension(32, showBtn.getPreferredSize().height));
		
		clearingTimer = new Timer(CLEAR_DELAY_MS, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearStatusBar();
			}
		});
		clearingTimer.setRepeats(false);

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(showBtn)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(titleIconLabel)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(titleLabel)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(showBtn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(titleIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		setPreferredSize(new Dimension(100, getPreferredSize().height));
	}

	public void setTitle(final FinishStatus.Type finishType, final String title) {
		TaskIcon icon = null;
		String type = null;

		if (finishType != null) {
			switch (finishType) {
				case SUCCEEDED:
					type = "finished";
					icon = TaskIcon.FINISHED;
					break;
				case FAILED:
					type = "error";
					icon = TaskIcon.ERROR;
					break;
				case CANCELLED:
					type = "cancelled";
					icon = TaskIcon.CANCELLED;
					break;
			}
		}
		
		this.setTitle(type, icon, title);
	}

	public void setTitle(final Level level, final String title) {
		TaskIcon icon = null;
		String type = null;

		if (level != null) {
			switch (level) {
				case INFO:
					type = "info";
					icon = TaskIcon.INFO;
					break;
				case WARN: 
					type = "warn";
					icon = TaskIcon.WARN;
					break;
				case ERROR:
					type = "error";
					icon = TaskIcon.ERROR;
					break;
			}
		}
		
		this.setTitle(type, icon, title);
	}
	
	public void setTitle(final String type, final TaskIcon icon, final String title) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setTitle(type, icon, title);
				}
			});
			return;
		}
		
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
	
	private static void makeSmall(final JComponent component) {
		if (LookAndFeelUtil.isAquaLAF()) {
			component.putClientProperty("JComponent.sizeVariant", "small");
		} else {
			final Font font = component.getFont();
			final Font newFont = new Font(font.getFontName(), font.getStyle(), (int)LookAndFeelUtil.getSmallFontSize());
			component.setFont(newFont);
		}
	}
}
