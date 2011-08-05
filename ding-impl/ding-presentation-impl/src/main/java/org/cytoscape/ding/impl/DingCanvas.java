/*
  File: CytoscapeCanvas.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.ding.impl;

import javax.swing.*;
import java.awt.*;


/**
 * This class is meant to be extended by a class which
 * is meant to exist within the InternalFrameComponent class.
 * It provides the services required to draw onto it.
 *
 * Currently (9/7/06), two classes will extend DingCanves, org.cytoscape.ding.impl.InnerCanvas
 * and org.cytoscape.ding.impl.ArbitraryGraphicsCanvas.
 */
public abstract class DingCanvas extends JComponent {
	/**
	 * ref to image we maintain
	 */
	protected Image m_img;

	/**
	 * ref to our background color
	 */
	protected Color m_backgroundColor;

	/**
	 * ref to visibility boolean
	 */
	protected boolean m_isVisible;

	/**
	 * ref to opaque boolean
	 */
	protected boolean m_isOpaque;

	/**
	 * Show or hides this component depending on value of parameter.
	 *
	 * @param isVisible boolean
	 */
	public void setVisible(boolean isVisible) {
		m_isVisible = isVisible;
	}

	/**
	 * Sets opacity of component
	 *
	 * @param isOpaque boolean
	 */
	public void setOpaque(boolean isOpaque) {
		m_isOpaque = isOpaque;
	}

	/**
	 * Returns the background color of this component.
	 *
	 * @return Color
	 */
	public Color getBackground() {
		return m_backgroundColor;
	}

	/**
	 * Set the component background color.
	 *
	 * backgroundColor Color
	 */
	public void setBackground(Color backgroundColor) {
		m_backgroundColor = backgroundColor;
	}

	/**
	 * Returns the image maintained by the canvas.
	 *
	 * @return Image
	 */
	public Image getImage() {
		return m_img;
	}


	/**
	 * Method used to print canvas without using image imposter.
	 */
	public void printNoImposter(Graphics g) {
		print(g);
	}
}
