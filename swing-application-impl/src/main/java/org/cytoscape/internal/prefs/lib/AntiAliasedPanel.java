package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AntiAliasedPanel extends JPanel
{
	public AntiAliasedPanel()
	{
		super( );
	}
	public AntiAliasedPanel(String title)
	{
		super( );
		if (title != null)
			setBorder(BorderFactory.createTitledBorder(title));
	}
	public AntiAliasedPanel(String title, boolean useSpacer)
	{
		super( );
		if (title != null)
			setBorder(BorderFactory.createTitledBorder(title));
	}
	public AntiAliasedPanel(LayoutManager inLayout)
    {
		super( inLayout);
    }
	
	@Override
	public void paint(final Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setBackground(Color.black);
		super.paint(g);
	}
	
	public void addSpacer()	{add(Box.createRigidArea(BoxComponent.COMPONENT_SPACE));}
	public void addHalfSpacer()	{add(Box.createRigidArea(BoxComponent.HALF_SPACE));}
	public void addLeading()	{add(Box.createRigidArea(BoxComponent.LEADING));}
	
	public static void setSizes(Component component, Dimension dim) {
		if (component == null)
			return;
		component.setSize(dim);
		component.setMaximumSize(dim);
		component.setMinimumSize(dim);
		component.setPreferredSize(dim);
	}
}