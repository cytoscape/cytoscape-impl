package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Button with drop down menu.
 * 
 * @version 0.7
 * @since Cytoscape 2.5
 * @author kono
 */
public class DropDownMenuButton extends JButton {
	private final static long serialVersionUID = 1202339868695691L;
	private final Icon buttonIcon = new MenuArrowIcon();

	/**
	 * Creates a new DropDownMenuButton object.
	 * 
	 * @param action
	 *            DOCUMENT ME!
	 */
	public DropDownMenuButton(final AbstractAction action) {
		super(action);
		this.setFocusPainted(false);

		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4 + buttonIcon
				.getIconWidth()));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param g
	 *            DOCUMENT ME!
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Dimension dim = getSize();
		Insets ins = getInsets();
		int x = dim.width - ins.right;
		int y = ins.top
				+ ((dim.height - ins.top - ins.bottom - buttonIcon
						.getIconHeight()) / 2);
		buttonIcon.paintIcon(this, g, x, y);
	}

	class MenuArrowIcon implements Icon {
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			// Turn AA on
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(Color.black);
			g2.translate(x, y);
			g2.drawLine(2, 3, 6, 3);
			g2.drawLine(3, 4, 5, 4);
			g2.drawLine(4, 5, 4, 5);
			g2.translate(-x, -y);
		}

		public int getIconWidth() {
			return 9;
		}

		public int getIconHeight() {
			return 9;
		}
	}
}
