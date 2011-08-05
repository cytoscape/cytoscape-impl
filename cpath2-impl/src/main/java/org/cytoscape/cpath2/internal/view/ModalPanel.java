// $Id$
//------------------------------------------------------------------------------
/** Copyright (c) 2007 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander, Benjamin Gross
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.cpath2.internal.view;

// imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JComponent;

/**
 * Modal Panel provides modal support for popup panel.
 *
 * @author Benjamin Gross
 */
public class ModalPanel extends JPanel implements MouseListener {

	/**
	 * ref to color
	 */
	private static Color m_backgroundColor = Color.DARK_GRAY;

	/**
	 * image that we used to draw into.
	 */
	private BufferedImage m_img;

	/**
	 * Constructor.
	 */
	public ModalPanel() {
		addMouseListener(this);
		setVisible(false);
	}

	/**
	 * Our implementation of set bounds.
	 */
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		m_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Our implementation of paint component.
	 */
	public void paintComponent(Graphics g) {

		// only paint if we have an image
		if (m_img != null) {

			// we need a g2d ref
			Graphics2D g2d = (Graphics2D)g;

			// clear our image
			clearImage(((BufferedImage) m_img).createGraphics());

			// setup composite
			Composite origComposite = g2d.getComposite();
			Composite newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)0.050);

			// draw
			g2d.setComposite(newComposite);
			g2d.drawImage(m_img, 0, 0, null);

			// restore composite
			g2d.setComposite(origComposite);
		}
	}

	// kill mouse events - fixes bug where user can 
	// click on gradient header and select things underneath
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	/**
	 * Utility function to clean the background of the image,
	 * using m_backgroundColor
	 *
	 * image2D Graphics2D
	 */
	private void clearImage(Graphics2D image2D) {

		// set the alpha composite on the image, and clear its area
		Composite origComposite = image2D.getComposite();
		image2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		image2D.setPaint(m_backgroundColor);
		image2D.fillRect(0, 0, m_img.getWidth(null), m_img.getHeight(null));
		image2D.setComposite(origComposite);
	}

}