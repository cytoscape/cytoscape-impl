package org.cytoscape.internal.prefs.lib;

/**
 ColorMenuButton
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;


public class ColorMenuButton extends JButton implements MouseListener, ColorMenuListener //, ActionListener
{
	private static final long	serialVersionUID	= 1L;
	boolean fUseNoFill = true; 
	public ColorMenuButton(boolean useNoFill)
    {
        super();
        fUseNoFill = useNoFill;
        addMouseListener(this);
        setBorder(new BorderPainter());
        AntiAliasedPanel.setSizes(this, new Dimension(16, 16));
    }
	public ColorMenuButton()
	{
		this(true);
	}
   	
	
	public ColorMenuButton(Dimension dim, boolean useNoFill)
	{
		this(useNoFill);
		AntiAliasedPanel.setSizes(this, dim);
	}
   	
   	public ColorMenuButton(String name, boolean useNoFill)
	{
   		this(useNoFill);
	    setText(name);
	    setToolTipText(name + ".tooltip");
	}
   	
   	public ColorMenuButton(String name, Color c, boolean useNoFill)
	{
   		this(name, useNoFill);
	    setColor(c);
	}

   	public ColorMenuButton(boolean useNoFill, Color c)
    {
        this(useNoFill);
        setColor(c);
    }

    @Override public void paintComponent(Graphics g)
    {
    	g.drawRect(0,0,35,54);
		super.paintComponent(g);
 		boolean noFill = Colors.kUnfilled.equals(fColor);
  		if (noFill) {
			g.setColor(Color.black);
			g.drawRect(0,0,35,54);
		}
    }
    //--------------------------------------------------------------------------------------
    class BorderPainter extends AbstractBorder
	{
    	private static final long serialVersionUID = 1L;

		// private ColorMenu fMenu;
		public BorderPainter()		{		}		//  ColorMenu menu  fMenu = menu;	

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(getColor());
			g2.fillRect(0, 0, c.getWidth(), c.getHeight());
			g2.setColor(Color.BLACK);
			g2.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
			boolean noFill = Colors.kUnfilled.equals(fColor);
		  	if (noFill) {
				g.setColor(Color.black);
				g.setFont(BoldSerif12);
				g.drawString("N", (getWidth() / 2 - 4), getHeight() / 2 + 3);
			}
		}
	}
	public static final Font	BoldSerif12	= new Font(Font.SERIF, Font.BOLD, 12);
    //--------------------------------------------------------------------------------------
    public static final String COLOR_CHANGED = "Color Changed";
    public void colorSelected()			// called as a result of the color being set
    {
        setColor(getColor());
        fireActionPerformed(new ActionEvent(this, 0, COLOR_CHANGED));
    }
    //--------------------------------------------------------------------------------------
    private Color fColor = null; 
    public Color getColor()    {        return fColor;    }

    public void setColor(Color c)
    {
        if (c == null)    	c = Color.red;
        fColor = c;
        setBackground(c);
        setForeground(c);
    }

	@Override	public void mouseClicked(MouseEvent arg0) {}
	@Override	public void mouseEntered(MouseEvent arg0) {}
	@Override	public void mouseExited(MouseEvent arg0) {}
	@Override	public void mousePressed(MouseEvent arg0) 
	{
		if (isEnabled()) {
			ColorMenu cm = sColorMenu;
			cm.setOwner(this);
			cm.setColor(getBackground());
			cm.setUseNoFill(fUseNoFill);
			Point location = cm.getSelectedLocation();
			cm.show(this, -location.x, -location.y);
		}
	}
	@Override	public void mouseReleased(MouseEvent arg0) {}
	private ColorMenu sColorMenu = new ColorMenu();			// one copy for getInstance() to return	

	public class ColorMenu extends JPopupMenu  implements ActionListener
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;

//		public interface ColorMenuListener
//		{
//			public void colorSelected();
//		}


//		protected Border fUnselectedBorder, fSelectedBorder, fActiveBorder;
//		protected Hashtable<Color, ColorPane> fPanes;
		protected ColorPane fSelected, fCurrent;
		protected ColorMenuListener fOwner;
		public void setOwner(ColorMenuListener cml)	{ fOwner = cml;	}
//		public static ColorMenu getInstance()	{ return sColorMenu;	}
		private int rows = 13, cols = 10;
		private ColorMenu()
		{
			setBorder(new EmptyBorder(0, 1, 1, 1));
			int rows = 13, cols = 10;
			setLayout(new GridLayout(rows, cols));
			makeColorPanes();
		}
		private ColorPane[] panes = null;

		private void makeColorPanes()
		{
			int i = 0;
			panes = new ColorPane[rows*cols];
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
				{
					Color color = Colors.colorFromIndex(i);
					panes[i] = new ColorPane(r, c, color, this);
					add(panes[i]);
					i++;
				}

		}
//		private ColorMenu(ColorMenuListener owner)
//		{
//			this();
//			fOwner = owner;
//		}
		public void setUseNoFill(boolean use)
		{
			if (panes == null) makeColorPanes();
			ColorPane pane0 = panes[0];
			pane0.setColor(use ? Colors.kUnfilled : Color.white);
		}
	//----------------------------------------------------------------------------
		public void setColor(Color c)
		{
	        if (c == null)            return;
//			ColorPane pane = fPanes.get(c);
//			if (pane == null)
			ColorPane	pane = findClosestPane(c);
			if (fSelected != null)
				fSelected.setSelected(false);
			fSelected = fCurrent = pane;
			if (fSelected != null) fSelected.setSelected(true);
		}
		
		//---------------------------------------------------------------------
		float COLOR_THRESHOLD = 0.00001f;
		private ColorPane findClosestPane(Color c)
		{
			if (panes == null) makeColorPanes();
			//For each pane in fPanes, get the color and compute the color distance.
			float[] targetColors = c.getRGBColorComponents(null);
			ColorPane minPane = null;
			float minDistance = -1;
			float[] sampleColors = new float[3];
			for (ColorPane currPane : panes)
			{
				Color color = currPane.getColor();
				color.getRGBColorComponents(sampleColors);
				float distance = 0;
				for (int i = 0; i < 3; i++)
				{
					float diff = targetColors[i] - sampleColors[i];
					distance += (diff * diff);
				}
				if (distance < COLOR_THRESHOLD) return currPane;		
				if (minPane == null || distance < minDistance)
				{
					minPane = currPane;
					minDistance = distance;
				}
			}
			return minPane;
		}
		//---------------------------------------------------------------------
		public Color getColor()
		{
			if (fCurrent != null)
			{
				if (!fCurrent.isFilled())	return Colors.kUnfilled;
				return fCurrent.getColor();
			}
			return (fSelected != null) ? fSelected.getColor() : null;
		}

		public Point getSelectedLocation()	{		return (fSelected == null)  ? null : fSelected.getLocation();	}
		
		public void actionPerformed(ActionEvent e)
		{
			ColorPane pn = (ColorPane) e.getSource();
			if (fSelected != null)
				fSelected.setSelected(false);
			fSelected = pn;
			pn.setSelected(true);
			if (fOwner == null)		System.err.println("Owner not assigned to ColorMenu");
			else  {
				fOwner.colorSelected();
				fOwner = null;
			}
		}
	}


}