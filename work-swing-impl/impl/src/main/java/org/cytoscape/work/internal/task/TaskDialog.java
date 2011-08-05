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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

class TaskDialog extends JDialog {

	private static final long serialVersionUID = 5121051715524541940L;

	/**
	 * How much time between updating the "Estimated Time Remaining" field,
	 * stored in <code>timeLabel</code>.
	 */
	static final int TIME_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

	/**
	 * Description and status messages are stored in <code>JTextArea</code>s;
	 * this specifies the number of columns <code>JTextArea</code>s should have.
	 * This value has a big impact on the size of the dialog.
	 */
	static final int TEXT_AREA_COLUMNS = 30;

	/**
	 * Constant used for <code>CardLayout.show()</code> and
	 * <code>CardLayout.add()</code> to switch between normal mode and exception
	 * mode.
	 */
	static final String NORMAL_MODE = "normal";

	/**
	 * Constant used for <code>CardLayout.show()</code> and
	 * <code>CardLayout.add()</code> to switch between normal mode and exception
	 * mode.
	 */
	static final String EXCEPTION_MODE = "exception";

	/**
	 * If the progress bar is indeterminate, the string format to use for
	 * <code>timeLabel</code>.
	 */
	static final String ELAPSED_FORMAT = "%s elapsed";

	/**
	 * If the progress bar is determinate, the string format to use for
	 * <code>timeLabel</code>.
	 */
	static final String ELAPSED_AND_REMAINING_FORMAT = "%s elapsed, %s remaining";

	static final String CANCEL_LABEL = "Cancel";
	static final String CANCELLING_LABEL = "   Cancelling...   ";
	static final String CLOSE_LABEL = "Close";

	// State variables
	boolean haltRequested = false;
	boolean errorOccurred = false;
	SwingTaskMonitor parentTaskMonitor = null;

	// Swing components
	JTextArea descriptionLabel = new JTextArea();
	JTextArea descriptionLabel2 = new JTextArea();
	JTextArea statusLabel = new JTextArea();
	JProgressBar progressBar = new JProgressBar();
	JTextArea timeLabel = new JTextArea();
	JButton cancelButton = new JButton(CANCEL_LABEL);
	JButton closeButton = new JButton(CLOSE_LABEL);
	JPanel exceptionPanel = new JPanel();

	// Specific for the timer
	Timer timer = null;
	Date startTime = new Date();

	public TaskDialog(final Window parentFrame, final SwingTaskMonitor parentTaskMonitor) {
		super(parentFrame);
		this.parentTaskMonitor = parentTaskMonitor;
		initComponents();
		initTimer();
		initLayout();
	}

	public void setTaskTitle(final String taskTitle) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setTitle(taskTitle);
				descriptionLabel.setText(taskTitle);
				descriptionLabel2.setText(taskTitle);
				pack();
			}
		});
	}


	public void setPercentCompleted(final int percent) {
		if (haltRequested)
			return;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (percent < 0) {
					if (!progressBar.isIndeterminate()) {
						progressBar.setIndeterminate(true);
					}
				} else {
					if (progressBar.isIndeterminate()) {
						progressBar.setIndeterminate(false);
					}

					progressBar.setValue(percent);
				}
			}
		});
	}

	public void setException(final Throwable t, final String userErrorMessage) {
		this.errorOccurred = true;
		stopTimer();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Create Error Panel
				ErrorPanel errorPanel = new ErrorPanel(TaskDialog.this, t,
						userErrorMessage, t.getMessage());
				exceptionPanel.add(errorPanel);
				CardLayout cardLayout = (CardLayout) getContentPane()
						.getLayout();
				cardLayout.show(getContentPane(), EXCEPTION_MODE);
				pack();

				// Make sure TaskDialog is actually visible
				if (!TaskDialog.this.isShowing()) {
					TaskDialog.this.setVisible(true);
				}
			}
		});
	}

	/**
	 * Sets the Status Message. Called by a child task thread. Safely queues
	 * changes to the Swing Event Dispatch Thread.
	 * 
	 * @param message
	 *            status message.
	 */
	public void setStatus(final String message) {
		if (!haltRequested) {
			// Update the UI
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// statusLabel.setText(StringUtils.truncateOrPadString(message));
					statusLabel.setText(message);
					pack();
				}
			});
		}
	}

	public void setTimeMessage(final String message) {
		// Update the UI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				timeLabel.setText(message);
			}
		});
	}

	/**
	 * Returns true if Task Has Encountered An Error.
	 * 
	 * @return boolean value.
	 */
	public boolean errorOccurred() {
		return errorOccurred;
	}

	/**
	 * Returns true if User Has Requested to Halt the Task.
	 * 
	 * @return boolean value.
	 */
	public boolean haltRequested() {
		return haltRequested;
	}

	synchronized void cancel() {
		if (haltRequested)
			return;

		haltRequested = true;
		cancelButton.setText(CANCELLING_LABEL);
		cancelButton.setEnabled(false);
		progressBar.setIndeterminate(true);
		parentTaskMonitor.cancel();
	}

	synchronized void close() {
		stopTimer();
		parentTaskMonitor.close();
	}

	void initComponents() {
		initTextArea(descriptionLabel);
		initTextArea(descriptionLabel2);
		initTextArea(statusLabel);
		initTextArea(timeLabel);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (errorOccurred)
					close();
				else
					cancel();
			}
		});
	}

	JLabel initLabel(JLabel label) {
		label.setHorizontalAlignment(JLabel.LEFT);
		label.setFont(new Font(null, Font.PLAIN, 13));
		return label;
	}

	JTextArea initTextArea(JTextArea textArea) {
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setColumns(TEXT_AREA_COLUMNS);
		// textArea.setBorder(new EmptyBorder(5, 5, 5, 5));

		textArea.setBackground((Color) UIManager.get("Label.background"));
		textArea.setForeground((Color) UIManager.get("Label.foreground"));
		textArea.setFont(new Font(null, Font.PLAIN, 13));
		return textArea;
	}

	void initLayout() {
		getContentPane().setLayout(new CardLayout());
		JPanel panel1 = new JPanel(new GridBagLayout());
		JLabel element0 = initLabel(new JLabel("Description: "));
		panel1.add(element0, new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(10, 10, 0, 0), 0, 0));
		panel1.add(descriptionLabel, new GridBagConstraints(1, 0, 1, 1, 1, 0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 10), 0, 0));
		JLabel element1 = initLabel(new JLabel("Status: "));
		panel1.add(element1, new GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(10, 10, 0, 0), 0, 0));
		panel1.add(statusLabel, new GridBagConstraints(1, 1, 1, 1, 1, 0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 10), 0, 0));
		JLabel element2 = initLabel(new JLabel("Progress: "));
		panel1.add(element2, new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
						10, 10, 0, 0), 0, 0));
		panel1.add(progressBar, new GridBagConstraints(1, 2, 1, 1, 1, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 10), 0, 0));
		panel1.add(timeLabel, new GridBagConstraints(1, 3, 1, 1, 1, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 10), 0, 0));
		JSeparator element3 = new JSeparator();
		panel1.add(element3, new GridBagConstraints(0, 4, 2, 1, 1, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 10), 0, 0));
		panel1.add(cancelButton, new GridBagConstraints(0, 5, 2, 1, 1, 1,
				GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 10, 10), 0, 0));
		getContentPane().add(panel1, NORMAL_MODE);

		JPanel panel2 = new JPanel(new GridBagLayout());
		JLabel element4 = initLabel(new JLabel("Description: "));
		panel2.add(element4, new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(10, 10, 0, 0), 0, 0));
		panel2.add(descriptionLabel2, new GridBagConstraints(1, 0, 1, 1, 1, 0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 0), 0, 0));
		panel2.add(exceptionPanel, new GridBagConstraints(0, 1, 2, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 0, 10), 0, 0));
		JSeparator element6 = new JSeparator();
		panel2.add(element6, new GridBagConstraints(0, 2, 2, 1, 1, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 10), 0, 0));
		panel2.add(closeButton, new GridBagConstraints(0, 3, 2, 1, 1, 0,
				GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 10, 10), 0, 0));
		getContentPane().add(panel2, EXCEPTION_MODE);

		exceptionPanel.setLayout(new GridLayout(1, 1));

		setResizable(false);
		pack();
	}

	/**
	 * Initialize the Timer Object. Note that timer events are sent to the Swing
	 * Event-Dispatch Thread.
	 */
	void initTimer() {
		// Create Auto-Timer
		timer = new Timer(TIME_UPDATE_INTERVAL_IN_MILLISECONDS,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Date currentTime = new Date();
						long timeElapsed = currentTime.getTime()
								- startTime.getTime();
						String timeElapsedString = StringUtils
								.getTimeString(timeElapsed);
						if (!progressBar.isIndeterminate()
								&& progressBar.getValue() != 0) {
							long timeRemaining = (long) ((100.0 / progressBar
									.getValue() - 1.0) * timeElapsed);
							String timeRemainingString = StringUtils
									.getTimeString(timeRemaining);
							timeLabel.setText(String.format(
									ELAPSED_AND_REMAINING_FORMAT,
									timeElapsedString, timeRemainingString));
						} else {
							timeLabel.setText(String.format(ELAPSED_FORMAT,
									timeElapsedString));
						}
						pack();
					}
				});
		timer.start();
	}

	/**
	 * Stops the Internal Timer.
	 */
	void stopTimer() {
		if ((timer != null) && timer.isRunning()) {
			timer.stop();
		}
	}
}
