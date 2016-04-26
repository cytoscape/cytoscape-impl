package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

@SuppressWarnings("serial")
class MemStatusPanel extends JPanel {
	
	static final int MEM_UPDATE_DELAY_MS = 2000;
	static final int MEM_STATE_ICON_DIM_PX = 14;
	
	static enum MemState {
		MEM_OK       (0.00f, 0.75f, LookAndFeelUtil.getSuccessColor(), "OK"),
		MEM_LOW      (0.75f, 0.85f, LookAndFeelUtil.getWarnColor(), "Low"),
		MEM_VERY_LOW (0.85f, 1.00f, LookAndFeelUtil.getErrorColor(), "Very Low");

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
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(color);
			g2d.fillOval(1, 1, MEM_STATE_ICON_DIM_PX - 2, MEM_STATE_ICON_DIM_PX - 2);
			
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

	static enum MemUnit {
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

	final JLabel memAmountLabel;
	final JToggleButton memStatusBtn;

	public MemStatusPanel() {
		memAmountLabel = new JLabel();
		makeSmall(memAmountLabel);
		memAmountLabel.setVisible(false);

		final JButton gcBtn = new JButton("Free Unused Memory");
		gcBtn.putClientProperty("JButton.buttonType", "gradient");
		makeSmall(gcBtn);
		gcBtn.addActionListener(e -> {
            gcBtn.setEnabled(false);
            gcBtn.setText("Freeing Memory...");
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    performGC();
                    updateMemStatus();
                    gcBtn.setText("Free Unusued Memory");
                    gcBtn.setEnabled(true);
                }
            });
        });
		gcBtn.setToolTipText("<html>Try to free memory&mdash;may temporarily freeze Cytoscape</html>");
		gcBtn.setVisible(false);

		memStatusBtn = new JToggleButton("Memory", MemState.MEM_OK.getIcon());
		memStatusBtn.setHorizontalTextPosition(JButton.RIGHT);
		memStatusBtn.putClientProperty("JButton.buttonType", "gradient");
		makeSmall(memStatusBtn);
		memStatusBtn.addActionListener(e -> {
            memAmountLabel.setVisible(memStatusBtn.isSelected());
            gcBtn.setVisible(memStatusBtn.isSelected());
        });
		memStatusBtn.setHorizontalTextPosition(SwingConstants.LEFT);
		memStatusBtn.setFocusPainted(false);

		final Timer updateTimer = new Timer(MEM_UPDATE_DELAY_MS, e -> updateMemStatus());
		updateTimer.setRepeats(true);
		updateTimer.start();

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(memAmountLabel)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(gcBtn)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(memStatusBtn)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(memAmountLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(gcBtn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(memStatusBtn, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}

	private void updateMemStatus() {
		final long memTotal = Runtime.getRuntime().maxMemory();
		final String memTotalFmt = MemUnit.format(memTotal);

		final float memUsed = getMemUsed();
		final MemState memState = MemState.which(memUsed);
		
		memStatusBtn.setIcon(memState.getIcon());
		memStatusBtn.setHorizontalTextPosition(JButton.RIGHT);
		memStatusBtn.setToolTipText(memState.getName());
		
		memAmountLabel.setText(String.format("%.1f%% used of %s", memUsed * 100.0f, memTotalFmt));
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