package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.border.MatteBorder;

import org.cytoscape.internal.prefs.lib.ColorMenuButton.ColorMenu;

public class ColorPane extends JMenuItem implements MouseListener
{
	private static final long	serialVersionUID	= 1L;
	protected static final int PANE_SIZE = 16;
//	public static final Font	Serif12	= new Font(Font.SERIF, 0, 12);
	protected ColorMenu fMenu;
	protected Color fColor;
	protected int fX, fY, fSize;
	protected boolean fNoFill; // true in the top left pane, representing unfilled.  Some menus (text color, foregrounds, group color, etc.) prohibit kColorOfNoFill
	public boolean isFilled()	{		return !fNoFill;	}
	public boolean topIsNoFill = false;
	MatteBorder fUnselectedBorder = new MatteBorder(1, 1, 1, 1, Color.gray);
	MatteBorder fSelectedBorder = new MatteBorder(1, 1, 1, 1, Color.red);
	MatteBorder fActiveBorder = new MatteBorder(1, 1, 1, 1, Color.white);
	ColorPane fCurrent;
	
	public ColorPane(int row, int col, Color color, ColorMenu m)
	{
		fNoFill = (row == 0) && (col == 0) && topIsNoFill;
		fX = col * PANE_SIZE;
		fY = row * PANE_SIZE;
		fColor = color;
		fMenu = m;
		setBackground(fColor);
		setForeground(fColor);
		if (fMenu != null) 
			setBorder(fUnselectedBorder);
		String msg = "R " + fColor.getRed() + ", G " + fColor.getGreen() + ", B " + fColor.getBlue();
		setToolTipText(msg);
		addActionListener(m);
		addMouseListener(this);
	}

	public Color getColor()				{		return fColor;	}
	public void setColor(Color c)		{		fColor =c;			fNoFill = fColor.equals(Colors.kUnfilled);   }
	public Dimension getPreferredSize()	{		return new Dimension(PANE_SIZE, PANE_SIZE);	}
	public Dimension getMaximumSize()	{		return getPreferredSize();	}
	public Dimension getMinimumSize()	{		return getPreferredSize();	}

	protected boolean fIsSelected;
	public void setSelected(boolean selected)
	{
		fIsSelected = selected;
		if (fMenu != null) 
			setBorder((fIsSelected) ? fSelectedBorder : fUnselectedBorder);
	}
	public boolean isSelected()	{		return fIsSelected;	}

	public void mousePressed(MouseEvent e)	{	}
	public void mouseClicked(MouseEvent e)	{	}
	public void mouseReleased(MouseEvent e)	{	}
	public void mouseEntered(MouseEvent e)
	{
		if (fMenu != null) 
		{
			setBorder(fActiveBorder);
			fCurrent = this;
		}
	}

	public void mouseExited(MouseEvent e)
	{
		if (fMenu != null) 
		{
			setBorder(fIsSelected ? fSelectedBorder : fUnselectedBorder);
			fCurrent = null;
		}
	}

	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		int wid = getWidth(), hght = getHeight();
		g2.fillRect(0, 0, wid, hght);
		if (fNoFill)
		{
			Color svColor = g2.getColor();
			g2.setColor(Color.black);
			g.setFont(new Font(Font.SERIF, 0, 12));
			g.drawString("N", 4, PANE_SIZE - 3);
			g2.setColor(svColor);
		}
	}

}