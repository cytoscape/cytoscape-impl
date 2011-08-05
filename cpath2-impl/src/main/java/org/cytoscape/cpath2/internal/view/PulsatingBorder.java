package org.cytoscape.cpath2.internal.view;

import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PulsatingBorder implements Border {
        private float thickness = 0.5f;
        private JComponent c;

        public PulsatingBorder(JComponent c) {
            this.c = c;
        }

        public void paintBorder(Component c1, Graphics g,
                int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle2D r = new Rectangle2D.Double(x, y, width - 1, height - 1);
            g2.setStroke(new BasicStroke(2.0f * getThickness()));

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getThickness());
            g2.setComposite(ac);
            g2.setColor(new Color(0x54A4DE));
            g2.draw(r);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public float getThickness() {
            return thickness;
        }

        public void setThickness(float thickness) {
            this.thickness = thickness;
            c.repaint();
        }
}
