package org.cytoscape.view.vizmap.gui.internal.view.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.slf4j.Logger;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public final class ViewUtil {

	public static final String TEXT_EDITOR_LABEL = "Enter a new text value:";
	
	private static Set<Font> fonts;

	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(final Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable, final Logger logger) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (Exception e) {
				if (logger != null)
					logger.error("Unexpected error", e);
				else
					e.printStackTrace();
			}
		}
	}
	
	public static String showMultiLineTextEditor(Component parent, String initialValue) {
		return showMultiLineTextEditor(parent, initialValue, null);
	}
	
	public static String showMultiLineTextEditor(Component parent, String initialValue, VisualProperty<String> vp) {
		JLabel label = new JLabel(TEXT_EDITOR_LABEL);
		JTextArea ta = new JTextArea(initialValue, 5, 30);
		JScrollPane scrollPane = new JScrollPane(ta);
		JPanel panel = new JPanel();
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
                .addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
	            .addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
	            .addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
		
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
				parent,
				panel,
				vp != null ? vp.getDisplayName() : "Text Editor",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null))
			return ta.getText();
		
		return initialValue;
	}
	
	public static void styleToolBarButton(AbstractButton btn) {
		styleToolBarButton(btn, null, true);
	}
	
	public static void styleToolBarButton(AbstractButton btn, boolean addPadding) {
		styleToolBarButton(btn, null, addPadding);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font) {
		styleToolBarButton(btn, font, true);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font, boolean addPadding) {
		int hPad = addPadding ? 5 : 0;
		int vPad = addPadding ? 4 : 0;
		styleToolBarButton(btn, font, hPad, vPad);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font, int hPad, int vPad) {
		if (font != null)
			btn.setFont(font);
		
		// Decrease the padding, because it will have a border
//		if (btn instanceof JToggleButton) {
//			hPad = Math.max(0, hPad - 4);
//			vPad = Math.max(0, vPad - 4);
//		}
		
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		btn.setContentAreaFilled(false);
		btn.setOpaque(true);
		btn.setHorizontalAlignment(SwingConstants.CENTER);
		btn.setVerticalAlignment(SwingConstants.CENTER);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.TOP);
		
		if (hPad > 0 || vPad > 0) {
			Dimension d = btn.getPreferredSize();
			d = new Dimension(d.width + 2 * hPad, d.height + 2 * vPad);
			btn.setPreferredSize(d);
			btn.setMinimumSize(d);
			btn.setMaximumSize(d);
			btn.setSize(d);
		}
		
		if (btn instanceof JToggleButton) {
			btn.addItemListener(evt -> updateToolBarStyle((JToggleButton) btn));
			updateToolBarStyle((JToggleButton) btn);
		}
	}
	
	public static void updateToolBarStyle(JToggleButton btn) {
		updateToolBarStyle(btn, true);
	}
	
	public static void updateToolBarStyle(JToggleButton btn, boolean showSelectionBorder) {
		Border defBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		
		if (btn.isEnabled()) {
			Border selBorder = showSelectionBorder ?
					BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("CyToggleButton[Selected].borderColor")),
							BorderFactory.createEmptyBorder(1, 1, 1, 1))
					: defBorder;
			
			btn.setBorder(btn.isSelected() ? selBorder : defBorder);
			btn.setBackground(
					btn.isSelected() ?
					UIManager.getColor("CyToggleButton[Selected].background") :
					UIManager.getColor("CyToggleButton.background"));
			btn.setForeground(
					btn.isSelected() ?
					UIManager.getColor("CyToggleButton[Selected].foreground") :
					UIManager.getColor("CyToggleButton.foreground"));
		} else {
			btn.setBorder(defBorder);
			btn.setForeground(UIManager.getColor("ToggleButton.disabledForeground"));
			btn.setBackground(UIManager.getColor("CyToggleButton.unselectedBackground"));
		}
	}
	
	public static Set<Font> getAvailableFonts() {
		if (fonts == null) {
			fonts = new LinkedHashSet<>();
			
			var sysFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

			if (sysFonts != null) {
				for (var f : sysFonts)
					fonts.add(f);
			}
		}
		
		return new LinkedHashSet<>(fonts);
	}
	
	private ViewUtil() {
	}
}
