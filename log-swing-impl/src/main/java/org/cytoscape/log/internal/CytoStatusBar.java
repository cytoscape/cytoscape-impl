package org.cytoscape.log.internal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.cytoscape.application.swing.CySwingApplication;

/**
 * @author Pasteur
 */
class CytoStatusBar extends JPanel {
	JPanel		panel;
	JButton		statusMessage;
//	JProgressBar	memoryAvailable;
	JButton		performGC;
//	Timer		updateUITimer;

	long		timeSinceLastMessage = 0;
	String		lastMessage = null;
	private final int uiUpdateDelay = 500;
	private final String trashIconPath;

	public CytoStatusBar(CySwingApplication app, String trashIconPath) {
		super();
		this.trashIconPath = trashIconPath;
		initGui();
		app.getStatusToolBar().add(panel);
	}

	private void initGui() {
		panel = new JPanel(new GridBagLayout());

		statusMessage = new JButton();
		statusMessage.setCursor(new Cursor(Cursor.HAND_CURSOR));
		setFontSize(statusMessage, 9);
		statusMessage.setToolTipText("Open Log Console");
		statusMessage.setBorderPainted(false);
		//statusMessage.setContentAreaFilled(false);
		statusMessage.setContentAreaFilled(true);
		statusMessage.setHorizontalTextPosition(SwingConstants.RIGHT);
		statusMessage.setHorizontalAlignment(SwingConstants.LEFT);

/*
		memoryAvailable = new JProgressBar();
		memoryAvailable.setToolTipText("Amount of memory available to Cytoscape");
		memoryAvailable.setStringPainted(true);
		setFontSize(memoryAvailable, 8);
		updateMemoryAvailable();
*/

		performGC = new JButton(new ImageIcon(getClass().getResource(trashIconPath)));
		performGC.setToolTipText("Try to get more memory by performing garbage collection");
		performGC.setBorderPainted(false);
		performGC.setContentAreaFilled(false);
		performGC.addActionListener(new PerformGCAction());

//		updateUITimer = new Timer(uiUpdateDelay, new UpdateUIAction());
//		updateUITimer.start();

		// status
		JPanel panel1 = new JPanel(new GridBagLayout());
		panel1.setBorder(new EtchedBorder());
		panel1.add(statusMessage, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(panel1, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 2, 1), 0, 0));

		// memory
		JPanel panel2 = new JPanel(new GridBagLayout());
		panel2.setBorder(new EtchedBorder());
		JPanel panel3 = new JPanel(new GridBagLayout());
//		panel3.add(memoryAvailable, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel3.add(performGC, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel2.add(panel3, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(panel2, new GridBagConstraints(1, 0, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 1, 2, 2), 0, 0));
	}

	static void setFontSize(Component component, int size) {
		Font font = component.getFont();
		component.setFont(new Font(font.getFontName(), font.getStyle(), size));
	}

	public void setMessage(String message, Icon icon) {
		statusMessage.setIcon(icon);
		lastMessage = message;
		timeSinceLastMessage = System.currentTimeMillis();
		updateStatusMessage();
	}

	public void addActionListener(ActionListener actionListener) {
		statusMessage.addActionListener(actionListener);
	}

	class PerformGCAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			performGC.setEnabled(false);
			for (int i = 0; i < 5; i++) {
				System.runFinalization();
				System.gc();
			}
			performGC.setEnabled(true);
		}
	}

	static final String[] MEMORY_SUFFIXES = { "B", "KiB", "MiB", "GiB" };
	static final double MEMORY_UNIT = 1024.0;
	static String formatMemory(long freeMemory, long totalMemory) {
		double free = (double) freeMemory;
		double total = (double) totalMemory;
		int suffix = 0;
		while ((total >= MEMORY_UNIT) && (suffix < MEMORY_SUFFIXES.length - 1)) {
			free /= MEMORY_UNIT;
			total /= MEMORY_UNIT;
			suffix++;
		}
		return String.format("%.2f of %.2f %s", free, total, MEMORY_SUFFIXES[suffix]);
	}

	static String formatTime(long totalMilliseconds) {
		long totalSeconds = totalMilliseconds / 1000;
		if (totalSeconds < 60)
			return formatTimeUnit(totalSeconds, "second");

		long totalMinutes = totalSeconds / 60;
		if (totalMinutes < 60)
			return formatTimeUnit(totalMinutes, "minute");

		long hours = totalMinutes / 60;
		long minutes = totalMinutes % 60;

		if (minutes == 0)
			return formatTimeUnit(hours, "hour");
		else
			return formatTimeUnit(hours, "hour") + ", " + formatTimeUnit(minutes, "minute");
	}

	static String formatTimeUnit(long time, String unit) {
		if (time == 1)
			return String.format("%d %s", time, unit);
		else
			return String.format("%d %ss", time, unit);
	}

	void updateStatusMessage() {
		if (lastMessage == null) {
			statusMessage.setText("");
		} else {
			long delta = System.currentTimeMillis() - timeSinceLastMessage;
			statusMessage.setText(String.format("%s (%s ago)", lastMessage, formatTime(delta))); 
		}
	}

/*
	void updateMemoryAvailable() {
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();

		memoryAvailable.setValue((int) (free * 100 / total));
		memoryAvailable.setString(formatMemory(free, total));
	}
*/
	class UpdateUIAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			updateStatusMessage();
//			updateMemoryAvailable();
		}
	}
}
