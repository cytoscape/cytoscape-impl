package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;

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
	
	static final int UPDATE_DELAY_MS = 2000;
	static final int ICON_SIZE = 14;
	static final float ICON_FONT_SIZE = 14.0f;
	
	static enum MemState {
		MEM_OK       (0.00f, 0.75f, LookAndFeelUtil.getSuccessColor(), "OK"),
		MEM_LOW      (0.75f, 0.85f, LookAndFeelUtil.getWarnColor(), "Low"),
		MEM_VERY_LOW (0.85f, 1.00f, LookAndFeelUtil.getErrorColor(), "Very Low");

		final float minRange;
		final float maxRange;
		final Color color;
		final String name;
		Icon icon;

		MemState(final float minRange, final float maxRange, final Color color, final String name) {
			this.minRange = minRange;
			this.maxRange = maxRange;
			this.color = color;
			this.name = name;
		}

		public boolean isInRange(final float memUsed) {
			return (this.minRange < memUsed) && (memUsed <= this.maxRange);
		}

		public Icon getIcon(IconManager iconManager) {
			if (icon == null) {
				icon = new TextIcon(IconManager.ICON_MICROCHIP, iconManager.getIconFont(ICON_FONT_SIZE), color,
						ICON_SIZE, ICON_SIZE);
			}

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
	
	private final CyServiceRegistrar serviceRegistrar;

	public MemStatusPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		memAmountLabel = new JLabel();
		makeSmall(memAmountLabel);
		memAmountLabel.setVisible(false);

		final JButton gcBtn = new JButton("Free Unused Memory");
		
		if (LookAndFeelUtil.isAquaLAF())
			gcBtn.putClientProperty("JButton.buttonType", "gradient");
		
		makeSmall(gcBtn);
		gcBtn.addActionListener(evt -> {
			gcBtn.setEnabled(false);
			gcBtn.setText("Freeing Memory...");
			
			SwingUtilities.invokeLater(() -> {
				performGC();
				updateMemStatus();
				gcBtn.setText("Free Unusued Memory");
				gcBtn.setEnabled(true);
			});
		});
		gcBtn.setToolTipText("<html>Try to free memory&mdash;may temporarily freeze Cytoscape</html>");
		gcBtn.setVisible(false);

		memStatusBtn = new JToggleButton(MemState.MEM_OK.getIcon(serviceRegistrar.getService(IconManager.class)));
		memStatusBtn.setHorizontalTextPosition(JButton.RIGHT);
		
		if (LookAndFeelUtil.isAquaLAF())
			memStatusBtn.putClientProperty("JButton.buttonType", "gradient");
		
		makeSmall(memStatusBtn);
		memStatusBtn.addActionListener(evt -> {
			memAmountLabel.setVisible(memStatusBtn.isSelected());
			gcBtn.setVisible(memStatusBtn.isSelected());
		});
		memStatusBtn.setHorizontalTextPosition(SwingConstants.LEFT);
		memStatusBtn.setFocusPainted(false);

		final Timer updateTimer = new Timer(UPDATE_DELAY_MS, evt -> updateMemStatus());
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
		
		try {
			memStatusBtn.setIcon(memState.getIcon(serviceRegistrar.getService(IconManager.class)));
		} catch (RuntimeException ex) {
			// Ignore...
			// When starting up Cytoscape, the IconManager service may not have been registered yet,
			// but it should be available on the next update call.
		}
		
		memStatusBtn.setHorizontalTextPosition(JButton.RIGHT);
		memStatusBtn.setToolTipText("Memory " + memState.getName());
		
		memAmountLabel.setText(String.format("%.1f%% used of %s", memUsed * 100.0f, memTotalFmt));
	}
}