package org.cytoscape.cpath2.internal.view;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

/**
 * Gradient Header Panel.
 *
 * @author Ethan Cerami
 */
public class GradientHeader extends JPanel {

    /**
     * Constructor.
     * @param header Header Title.
     */
    public GradientHeader(String header) {
        this.setLayout(new BorderLayout());
        JLabel label = new JLabel(header);
        label.setBorder(new EmptyBorder(0,0,0,0));
        Font font = label.getFont();
        Font newFont = new Font (font.getFamily(), Font.BOLD, font.getSize()-1);
        label.setFont(newFont);
        label.setForeground(new Color(102,51,51));
        label.setOpaque(false);
        this.add (label, BorderLayout.WEST);
        this.setBorder (new EmptyBorder(2,2,2,2));
    }

    /**
     * Constructor.
     * @param header Header Title.
     */
    public GradientHeader(String header, JButton button) {
        button.setBorder(new EmptyBorder (0,0,0,0));
        this.setLayout(new BorderLayout());
        JLabel label = new JLabel(header);
        label.setBorder(new EmptyBorder(0,0,0,0));
        Font font = label.getFont();
        Font newFont = new Font (font.getFamily(), Font.BOLD, font.getSize()-1);
        label.setFont(newFont);
        label.setForeground(new Color(102,51,51));
        label.setOpaque(false);
        this.add (label, BorderLayout.WEST);
        this.add (button, BorderLayout.EAST);
        this.setBorder (new EmptyBorder(2,2,2,2));
    }

    /**
     * Override paintComponent() with gradient painting code.
     * @param graphics Graphics Object.
     */
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;

        //  Create the gradient
        GradientPaint p = new GradientPaint (0,0, new Color (0xFFFFFF),
                0, getHeight(), new Color (0xC8D2DE));

        //  Save the state
        Paint oldPaint = g2.getPaint();

        //  Paint the background
        g2.setPaint(p);
        g2.fillRect(0, 0, getWidth(), getHeight());

        //  Restore the state
        g2.setPaint(oldPaint);
    }
}
