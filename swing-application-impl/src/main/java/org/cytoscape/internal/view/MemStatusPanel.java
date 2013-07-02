package org.cytoscape.internal.view;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

class MemStatusPanel extends JPanel {
	static final int MEM_UPDATE_DELAY_MS = 2000;
	static final int MEM_STATE_ICON_DIM_PX = 14;
	static enum MemState {
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

	final JLabel memAmountLabel;
	final JToggleButton memStatusBtn;

	public MemStatusPanel() {
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
		gcBtn.setToolTipText("<html>Try to free memory&mdash;may temporarily freeze Cytoscape</html>");
		setFontSize(gcBtn, 9);
		gcBtn.setVisible(false);

		memStatusBtn = new JToggleButton();
		setFontSize(memStatusBtn, 9);
		memStatusBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				memAmountLabel.setVisible(memStatusBtn.isSelected());
				gcBtn.setVisible(memStatusBtn.isSelected());
			}
		});
		memStatusBtn.setHorizontalTextPosition(SwingConstants.LEFT);

		final Timer updateTimer = new Timer(MEM_UPDATE_DELAY_MS, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateMemStatus();
			}
		});
		updateTimer.setRepeats(true);
		updateTimer.start();

		super.setLayout(new FlowLayout(FlowLayout.RIGHT));
		super.add(memAmountLabel);
		super.add(gcBtn);
		super.add(memStatusBtn);
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
}