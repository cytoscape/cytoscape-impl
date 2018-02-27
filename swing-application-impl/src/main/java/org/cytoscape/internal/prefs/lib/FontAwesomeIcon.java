/*
 * Copyright 2013 Mario Torre.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.Icon;

/**
 * Creates an {@link Icon} from a give FontAwesome unicode identifier.
 * 
 * @see http://fortawesome.github.io/Font-Awesome/cheatsheet/
 */
public class FontAwesomeIcon implements Icon {

    private static final String AWESOME_SET = "fontawesome-webfont.ttf";
    
    private int size;
    private BufferedImage buffer;
    
    private char iconID;
    private static final Font awesome;
    
    private Font font;
    
    static {
        try {
            InputStream stream =
                    FontAwesomeIcon.class.getResourceAsStream(AWESOME_SET);
            awesome = Font.createFont(Font.TRUETYPE_FONT, stream);

        } catch (FontFormatException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public FontAwesomeIcon(char iconID, int size) {
        this.iconID = iconID;
        this.size = size;
        font = awesome.deriveFont(Font.PLAIN, size);
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        
        if (buffer == null) {
            buffer = new BufferedImage(getIconWidth(), getIconHeight(),
                                       BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D graphics = (Graphics2D) buffer.getGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
            
            graphics.setFont(font);
            graphics.setColor(Color.BLACK);
            
            int stringY = getIconHeight() - (getIconHeight()/4) + 1;
            graphics.drawString(String.valueOf(iconID), 0, stringY);
            
            graphics.dispose();
        }
        
        g.drawImage(buffer, x, y, null);
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }
}
