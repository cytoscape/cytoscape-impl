package org.cytoscape.cpath2.internal.view;

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
