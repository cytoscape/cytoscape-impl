package org.cytoscape.cg.internal.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.slf4j.Logger;

public class ViewUtil {

	public static String getShortName(String pathName) {
		if (pathName == null)
			return null;
		
		return new File(pathName).getName();
	}
	
	public static ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
		var img = icon.getImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		var bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		var g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		
		return new ImageIcon(bi);
	}
	
	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable, Logger logger) {
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
	
	public static void recursiveDo(Component component, Consumer<JComponent> c) {
		if (component instanceof JComponent)
			c.accept((JComponent) component);
		
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents())
				recursiveDo(child, c);
		}
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
}
