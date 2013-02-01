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

import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Graphical User Interface (GUI) Utiltities.
 *
 * @author Ethan Cerami
 */
public class GuiUtils {

    /**
     * Creates a Titled Border with appropriate font settings.
     * @param title Title.
     * @return TitledBorder Object.
     */
    public static TitledBorder createTitledBorder (String title) {
        TitledBorder border = new TitledBorder(title);
        Font font = border.getTitleFont();
        Font newFont = new Font (font.getFamily(), Font.BOLD, font.getSize()+2);
        border.setTitleFont(newFont);
        border.setTitleColor(new Color(102,51,51));
        return border;
    }
}
