package org.cytoscape.log.internal;

/*
 * #%L
 * Cytoscape Log Swing Impl (log-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Map;
import java.util.HashMap;

import org.cytoscape.application.swing.CySwingApplication;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Handles the status bar at the bottom of the Cytoscape desktop
 */
class CyStatusBar extends JPanel {
    static final Logger logger = LoggerFactory.getLogger("CyUserMessages");
	static final int MEM_UPDATE_DELAY_MS = 2000;
	static final int MEM_STATE_ICON_DIM_PX = 14;
	enum MemState {
		MEM_OK       (0.00f, 0.75f, new Color(0x32C734), "OK"),
		MEM_LOW      (0.75f, 0.85f, new Color(0xE7F20A), "Low"),
		MEM_VERY_LOW (0.85f, 1.00f, new Color(0xC73232), "Very Low");

		final float minRange;
		final float maxRange;
		final Icon icon;
		final String name;

		MemState(final float minRange, final float maxRange, final Color color, final String name) {
			this.minRange = minRange;
			this.maxRange = maxRange;
			this.name = name;

			final BufferedImage image = new BufferedImage(MEM_STATE_ICON_DIM_PX, MEM_STATE_ICON_DIM_PX, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = (Graphics2D) image.getGraphics();
			final RenderingHints hints = g2d.getRenderingHints();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(color);
			g2d.fillOval(1, 1, MEM_STATE_ICON_DIM_PX - 2, MEM_STATE_ICON_DIM_PX - 2);
            g2d.setColor(Color.DARK_GRAY);
			g2d.drawOval(1, 1, MEM_STATE_ICON_DIM_PX - 3, MEM_STATE_ICON_DIM_PX - 3);
			this.icon = new ImageIcon(image);
		}

		public boolean isInRange(final float memUsed) {
			return (this.minRange < memUsed) && (memUsed <= this.maxRange);
		}

		public Icon getIcon() {
			return icon;
		}

		public String getName() {
			return name;
		}

		public static MemState which(final float memUsed) {
			for (final MemState state : MemState.values())
				if (state.isInRange(memUsed))
					return state;
			return null;
		}
	}

	enum MemUnit {
		GB(30L, "Gb"),
		MB(20L, "Mb"),
		KB(10L, "Kb");

		final long bytes;
		final String name;
		
		MemUnit(final long exp2, final String name) {
			this.bytes = 1L << exp2;
			this.name = name;
		}

		public static String format(final long bytes) {
			for (final MemUnit unit : MemUnit.values())
				if (bytes >= unit.bytes)
					return String.format("%.2f %s", bytes / (double) unit.bytes, unit.name);
			return String.format("%d b", bytes);
		}
	}

	static void setFontSize(final Component component, final int size) {
		final Font font = component.getFont();
		final Font newFont = new Font(font.getFontName(), font.getStyle(), size);
		component.setFont(newFont);
	}

	static float getMemUsed() {
		final Runtime runtime = Runtime.getRuntime();
		final long freeMem = runtime.freeMemory();
		final long totalMem = runtime.totalMemory();
        final long usedMem = totalMem - freeMem;
		final long maxMem = runtime.maxMemory();
		final double usedMemFraction = usedMem / (double) maxMem;
		return (float) usedMemFraction;
	}

	static void performGC() {
		for (int i = 0; i < 5; i++) {
			System.runFinalization();
			System.gc();
		}
	}

	final Map<String,Icon> levelToIconMap;
	final Icon defaultIcon;
	final JLabel messageLabel;
	final JToggleButton memStatusBtn;
	final JLabel memAmountLabel;

	public CyStatusBar(
			final CySwingApplication app,
			final String consoleIconPath,
			final UserMessagesDialog userMessagesDialog,
			final Map<String,String> levelToIconPathMap)
	{
		super();

		levelToIconMap = new HashMap<String,Icon>();
		for (final Map.Entry<String,String> levelToIconPath : levelToIconPathMap.entrySet()) {
			final Icon icon = new ImageIcon(getClass().getResource(levelToIconPath.getValue()));
			levelToIconMap.put(levelToIconPath.getKey(), icon);
		}
		defaultIcon = levelToIconMap.get("INFO");

		final JPanel statusBarPanel = new JPanel(new GridBagLayout());

		final JButton showConsoleBtn = new JButton(new ImageIcon(getClass().getResource(consoleIconPath)));
		showConsoleBtn.setBorder(null);
		showConsoleBtn.setBorderPainted(false);
		showConsoleBtn.setContentAreaFilled(false);
		showConsoleBtn.setOpaque(false);
		showConsoleBtn.setToolTipText("Open User Messages");
		
		showConsoleBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userMessagesDialog.open();
			}
		});

		messageLabel = new JLabel();

		memAmountLabel = new JLabel();
		setFontSize(memAmountLabel, 9);
		memAmountLabel.setVisible(false);

		final JButton gcBtn = new JButton("Free Unused Memory");
		gcBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gcBtn.setEnabled(false);
				gcBtn.setText("Freeing Memory...");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						performGC();
						updateMemStatus();
						gcBtn.setText("Free Unusued Memory");
						gcBtn.setEnabled(true);
					}
				});
			}
		});
		gcBtn.setVisible(false);
		gcBtn.setToolTipText("<html>Try to free unused memory.<br><br><i>Warning:</i> freeing memory may freeze Cytoscape for several seconds.</html>");
		setFontSize(gcBtn, 9);

		memStatusBtn = new JToggleButton();
		setFontSize(memStatusBtn, 9);
		memStatusBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				memAmountLabel.setVisible(memStatusBtn.isSelected());
				gcBtn.setVisible(memStatusBtn.isSelected());
			}
		});
		memStatusBtn.setHorizontalTextPosition(SwingConstants.LEFT);

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 4);
		c.gridx = 0;		c.gridy = 0;
		c.gridwidth = 1;	c.gridheight = 1;
		c.weightx = 0.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		statusBarPanel.add(showConsoleBtn, c);

		c.gridx++;
		statusBarPanel.add(new JSeparator(JSeparator.VERTICAL), c);

		c.gridx++;
		c.weightx = 1.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		statusBarPanel.add(messageLabel, c);

		c.gridx++;
		c.weightx = 0.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		statusBarPanel.add(memAmountLabel, c);

		c.gridx++;
		statusBarPanel.add(gcBtn, c);

		c.insets = new Insets(0, 0, 0, 0);
		c.gridx++;
		statusBarPanel.add(memStatusBtn, c);

		app.getStatusToolBar().add(statusBarPanel);

		final Timer updateTimer = new Timer(MEM_UPDATE_DELAY_MS, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateMemStatus();
			}
		});
		updateTimer.setRepeats(true);
		updateTimer.start();
	}

	private void updateMemStatus() {
		final long memTotal = Runtime.getRuntime().maxMemory();
		final String memTotalFmt = MemUnit.format(memTotal);

		final float memUsed = getMemUsed();
		final MemState memState = MemState.which(memUsed);
		memStatusBtn.setIcon(memState.getIcon());
		memStatusBtn.setText("Memory: " + memState.getName());
		memAmountLabel.setText(String.format("%.1f%% used of %s", memUsed * 100.0f, memTotalFmt));
	}

	public void setMessage(final String level, final String message)
	{
		Icon icon = defaultIcon;
		if (level != null && levelToIconMap.containsKey(level))
			icon = levelToIconMap.get(level);
		messageLabel.setIcon(icon);
		messageLabel.setText(message);
	}
}
