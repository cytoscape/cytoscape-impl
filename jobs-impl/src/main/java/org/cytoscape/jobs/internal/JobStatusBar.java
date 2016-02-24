package org.cytoscape.jobs.internal;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.StatusBarPanelFactory;

/**
 * Manages the job's status bar's UI at the bottom of the Cytoscape desktop.
 */
@SuppressWarnings("serial")
public class JobStatusBar extends JPanel implements StatusBarPanelFactory {

	public static final String TASK_HISTORY_CLICK = "job-status-click";

	private static final int CLEAR_DELAY_MS = 5000;

	public static final String ICON_RUNNING = IconManager.ICON_HOURGLASS_1;
	public static final String ICON_WARN = IconManager.ICON_EXCLAMATION_TRIANGLE;
	public static final String ICON_ERROR = IconManager.ICON_MINUS_CIRCLE;
	public static final String ICON_CANCELLED = IconManager.ICON_BAN;
	public static final String ICON_FINISHED = IconManager.ICON_CLOUD_DOWNLOAD;
						  
	public static enum JobsIcon {
		RUNNING(ICON_RUNNING),
		WARN(ICON_WARN),
		ERROR(ICON_ERROR),
		CANCELLED(ICON_CANCELLED),
		FINISHED(ICON_FINISHED),
		JOBS(IconManager.ICON_CLOUD);

		private final String text;

		private JobsIcon(final String text) {
			this.text = text;
		}

		public Color getForeground() {
			// We have to do this (rather than cache the colors) to make sure they have been initialized
			switch (this) {
				case RUNNING:   return LookAndFeelUtil.getInfoColor();
				case WARN:      return LookAndFeelUtil.getWarnColor();
				case ERROR:     return LookAndFeelUtil.getErrorColor();
				case CANCELLED: return LookAndFeelUtil.getErrorColor();
				case FINISHED:  return LookAndFeelUtil.getSuccessColor();
				default:        return UIManager.getColor("Label.foreground");
			}
		}

		public String getText() {
			return text;
		}
	}

	final JButton showBtn;
	JDialog statusDialog = null;
	JobsIcon currentStatus = JobsIcon.JOBS;

	public JobStatusBar(final CyServiceRegistrar serviceRegistrar) {
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
		showBtn = new JButton();
		showBtn.setFont(iconManager.getIconFont(14.0f));
		showBtn.setText(JobsIcon.JOBS.getText());
		showBtn.putClientProperty("JButton.buttonType", "gradient"); // Aqua LAF only
		showBtn.putClientProperty("JComponent.sizeVariant", "small");
		showBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showBtn.setText(JobsIcon.JOBS.getText());
				showBtn.setForeground(JobsIcon.JOBS.getForeground());
				if (statusDialog != null)
					statusDialog.setVisible(true);
			}
		});
		showBtn.setToolTipText("Show Job Status...");
		showBtn.setFocusPainted(false);
		
		if (!LookAndFeelUtil.isAquaLAF())
			showBtn.setPreferredSize(new Dimension(45, showBtn.getPreferredSize().height));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);

		layout.setHorizontalGroup(layout.createSequentialGroup()
						.addComponent(showBtn)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(showBtn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		setPreferredSize(new Dimension(showBtn.getPreferredSize().width, getPreferredSize().height));
	}

	public void setDialog(JDialog dialog) { this.statusDialog = dialog; }

	public void updateIcon(Collection<CyJobStatus> values) {
		JobsIcon icon = JobsIcon.JOBS;
		// Figure out status to show.  The priority order is:
		// 	FINISHED
		// 	ERROR/FAILED
		// 	CANCELED/PURGED/TERMINATED/UNKNOWN
		// 	QUEUED/SUBMITTTED/RUNNING
		if (values != null || values.size() > 0) {
			for (CyJobStatus status: values) {
				CyJobStatus.Status jobStatus = status.getStatus();
				if (jobStatus.equals(CyJobStatus.Status.FINISHED)) {
					icon = JobsIcon.FINISHED;
					break;
				}
				if (jobStatus.equals(CyJobStatus.Status.ERROR) ||
				    jobStatus.equals(CyJobStatus.Status.FAILED)) {
					icon = JobsIcon.ERROR;
				} else if (jobStatus.equals(CyJobStatus.Status.CANCELED) ||
				           jobStatus.equals(CyJobStatus.Status.PURGED) ||
				           jobStatus.equals(CyJobStatus.Status.TERMINATED) ||
				           jobStatus.equals(CyJobStatus.Status.UNKNOWN)) {
					if (!icon.equals(JobsIcon.ERROR))
						icon = JobsIcon.WARN;
				} else if (jobStatus.equals(CyJobStatus.Status.QUEUED) ||
				           jobStatus.equals(CyJobStatus.Status.SUBMITTED) ||
				           jobStatus.equals(CyJobStatus.Status.RUNNING)) {
					if (!icon.equals(JobsIcon.ERROR) && !icon.equals(JobsIcon.WARN))
						icon = JobsIcon.RUNNING;
				}
			}
		}

		if (!currentStatus.equals(JobsIcon.RUNNING) && currentStatus.equals(icon))
			return;

		setIcon(icon);
	}

	public void setIcon(final JobsIcon icon) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setIcon(icon);
				}
			});
			return;
		}

		String iconText = null;

		if (icon != null) {
			if (currentStatus.equals(JobsIcon.RUNNING) && icon.equals(JobsIcon.RUNNING)) {
				String text = showBtn.getText();
				// Move to the next phase
				if (text.equals(IconManager.ICON_HOURGLASS_1))
					iconText = IconManager.ICON_HOURGLASS_2;
				else if (text.equals(IconManager.ICON_HOURGLASS_2))
					iconText = IconManager.ICON_HOURGLASS_3;
				else if (text.equals(IconManager.ICON_HOURGLASS_3))
					iconText = IconManager.ICON_HOURGLASS_1;
			} else {
				iconText = icon.getText();
			}

			Color iconColor = icon.getForeground();

			// Set button icon based on error/warning status
			showBtn.setText(iconText);
			showBtn.setForeground(iconColor);
			currentStatus = icon;
		}
		
	}
	
	@Override
	public JPanel createTaskStatusPanel() {
		return this;
	}
	
}
