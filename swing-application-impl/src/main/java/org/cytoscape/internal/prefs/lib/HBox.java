package org.cytoscape.internal.prefs.lib;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class HBox extends AntiAliasedPanel
{
	private static final long serialVersionUID = 1L;
	Dimension spacer = BoxComponent.COMPONENT_SPACE;
	public void setSpacer(Dimension spaceDim) { spacer = spaceDim;	}
	public HBox(String label, Component comp) { this(new JLabel(label), comp);}
	
	public HBox(Dimension sz, Component ... components)
	{
		this(components);
		setSizes(this, sz);
	}
	
	public HBox()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
	
	public HBox(Component ... components)	{		this(true, components);			}
	
	public HBox(boolean useSpacer, boolean useGlue, Component ... components)
	{
		this(useSpacer, useGlue, false, components);
	}
	
	public HBox(boolean useSpacer, boolean useGlue, boolean center, Component ... components)
	{
		this();
		if(center)		add(Box.createHorizontalGlue());
		for(Component comp : components)
		{
			if ((comp instanceof JTextField) && (((JTextField)comp).getColumns() > 0))
				comp.setMaximumSize(comp.getPreferredSize());
			
			if (comp != null) add(comp);
			if (comp instanceof Box.Filler) {}
			else if(useSpacer) add(Box.createRigidArea(spacer));

		}
		if(useGlue || center)		add(Box.createHorizontalGlue());
		setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
	}
	
	public HBox(boolean useSpacer, Component ... components)
	{
		this(useSpacer, false, components);
	}
	//----------------------------------------------------------------------------------------------
	public void add(Component ... components){		for(Component c : components){	super.add(c);	}	}

	public void addWithSpace(Component comp, int spacer)
	{
		super.add(comp);
		add(Box.createRigidArea(new Dimension(spacer,spacer)));
	}

}
