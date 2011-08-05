package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.ThumbRenderer;


/**
 * DOCUMENT ME!
 *
 * @author $author$
  */
public class TriangleThumbRenderer extends JComponent
    implements ThumbRenderer {
	private final static long serialVersionUID = 1202339877445372L;
    private static final Color SELECTED_COLOR = Color.red;
    private static final Color DEFAULT_COLOR = Color.DARK_GRAY;
    private static final Color BACKGROUND_COLOR = Color.white;
    private JXMultiThumbSlider slider;
    private boolean selected;

    /**
     * Creates a new TriangleThumbRenderer object.
     *
     * @param slider DOCUMENT ME!
     */
    public TriangleThumbRenderer(JXMultiThumbSlider slider) {
        super();

        this.slider = slider;
        setBackground(BACKGROUND_COLOR);
    }

    protected void paintComponent(Graphics g) {
        /*
         * Enable anti-aliasing
         */
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        /*
         * Draw small triangle
         */
        if (selected) {
           
            final Polygon outline = new Polygon();
            outline.addPoint(0, 0);
            outline.addPoint(0, 4);
            outline.addPoint(4, 9);
            outline.addPoint(8, 4);
            outline.addPoint(8, 0);
            g.fillPolygon(outline);
            g.setColor(Color.blue);
            ((Graphics2D) g).setStroke(new BasicStroke(1.0f));
            g.drawPolygon(outline);
        } else {
            final Polygon thumb = new Polygon();

            thumb.addPoint(0, 0);
            thumb.addPoint(10, 0);
            thumb.addPoint(5, 10);
            g.fillPolygon(thumb);

            final Polygon outline = new Polygon();
            outline.addPoint(0, 0);
            outline.addPoint(9, 0);
            outline.addPoint(5, 9);
            g.setColor(Color.DARK_GRAY);
            ((Graphics2D) g).setStroke(new BasicStroke(1.0f));
            g.drawPolygon(outline);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param slider DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @param selected DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getThumbRendererComponent(JXMultiThumbSlider slider,
        int index, boolean selected) {
        this.selected = selected;

        final Object obj = slider.getModel()
                                 .getThumbAt(index)
                                 .getObject();

        if (obj.getClass() == Color.class)
            this.setForeground((Color) obj);
        else {
            if (selected)
                this.setForeground(SELECTED_COLOR);
            else
                this.setForeground(DEFAULT_COLOR);
        }

        return this;
    }
    
    
}
