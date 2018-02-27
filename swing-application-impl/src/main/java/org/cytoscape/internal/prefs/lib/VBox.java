package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;

public class VBox extends AntiAliasedPanel
{
	public enum ALIGNMENT{LEFT, RIGHT, CENTER;	}
	
	private static final long	serialVersionUID	= 1L;
	private boolean useSpacer = false;
 
	public VBox() 				{ this(false);}
	public VBox(String title) 	{ this(title, true, true);}
	public VBox(String str, Dimension size) 	{ this(str, true, false);		if (size != null) setSizes(this, size); }
	public VBox(Dimension size) 				{ this(false);			if (size != null) setSizes(this, size); }
	public VBox(boolean spacer)		
	{
		super();
		useSpacer = spacer;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}
	public VBox(boolean spacer, Component ... components)
	{
		this(spacer);
		add(components);
	}
	public VBox(Component ... components)
	{
		this(false, false, components);
	}
	public VBox(String title, boolean useGlue, boolean spacer, Component ... components)
		{
		super();
		useSpacer = spacer;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for(Component comp : components)
		{
			if ((comp instanceof JTextField) && (((JTextField)comp).getColumns() > 0))
				comp.setMaximumSize(comp.getPreferredSize());
			
			add(comp);
			if (useSpacer) add(Box.createRigidArea(BoxComponent.COMPONENT_SPACE));

		}
		if (useGlue) add(Box.createVerticalGlue());
		if (title != null)
			setBorder(makeSubpanelBorder2(title));
	}

	public VBox(boolean useGlue, boolean spacer, Component ... components)
	{
		super();
		useSpacer = spacer;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		for(Component comp : components) {
			if (comp == null)
				continue;
			if ((comp instanceof JTextField) && (((JTextField)comp).getColumns() > 0))
				comp.setMaximumSize(comp.getPreferredSize());
			add(comp);
		}
		if (useGlue) add(Box.createVerticalGlue());
	}
	
	
	//Build a VBox with aligned components
	public VBox(Dimension d, ALIGNMENT a, Component ... components){
		super();
		boolean useSpacer = d!=null;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for(Component c : components){
			if(c instanceof JComponent){
				switch(a){
					case RIGHT:		((JComponent) c).setAlignmentX(Component.RIGHT_ALIGNMENT);		break;											
					case CENTER:	((JComponent) c).setAlignmentX(Component.CENTER_ALIGNMENT);		break;		
					case LEFT:																		// same as default case
					default:		((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);		break;					
				}
			}
			add(c);
			if(useSpacer)		
				add(Box.createRigidArea(d));	
		}
		if(useSpacer)		remove(getComponentCount()-1);  //chop off the last spacer
		
	}
	public VBox(ALIGNMENT a, Component ... components){
		this(null, a, components);
	}
	
	public static AbstractBorder makeSubpanelBorder2(String s)
	{
		TitledBorder border = new TitledBorder(s);
		border.setTitleColor(Color.blue);
		border.setTitleFont(new Font("Dialog", Font.BOLD, 10));
		return border;
	}
	
	public void addLine(Component comp1, Component comp2)
	{
		Box line = Box.createHorizontalBox();
		line.setMaximumSize(new Dimension(300, 28));
		line.add(comp1);
		if (comp2 != null)
			line.add(comp2);
		if (useSpacer)
			addSpacer();
		line.add(Box.createHorizontalGlue());
		super.add(line);
	}

	@Override	public Component add(Component comp)
	{
		if (useSpacer)
			addSpacer();
		return super.add(comp);
	}
	public void addWithSpace(Component comp, int spacer)
	{
		super.add(comp);
		add(Box.createRigidArea(new Dimension(spacer,spacer)));
		}
//	@Override
//	public  void add(Component comp, int space)
//	{
//		add(Box.createRigidArea(new Dimension(space,space));
//		add(comp);
//	}
	
	public void add(Component ... components)
	{
		for(Component comp : components) 
		{
			if ((comp instanceof JTextField) && (((JTextField)comp).getColumns() > 0))
				comp.setMaximumSize(comp.getPreferredSize());
			add(comp);
		}
	}
	
	public void addSpacer()	{super.add(Box.createRigidArea(BoxComponent.COMPONENT_SPACE));}
	public void addHalfSpacer()	{super.add(Box.createRigidArea(BoxComponent.HALF_SPACE));}
	public void addLeading()	{super.add(Box.createRigidArea(BoxComponent.LEADING));}
	
	//---------------------------------------------------------------------------------------------------
//	public void equalizeHeight(VBox other)
//	{
//		int myHeight = getPreferredSize().height;
//		int otherHeight = other.getPreferredSize().height;
//		if (otherHeight > myHeight)
//		{
//			setPreferredSize(new Dimension(getPreferredSize().width, otherHeight));
//			setMinimumSize(getPreferredSize());
//		}
//		else 
//		{
//			other.setPreferredSize(new Dimension(other.getPreferredSize().width, myHeight));
//			other.setMinimumSize(other.getPreferredSize());
//		}
//	}
////	
//	public void equalizeWidth(VBox other)
//	{
//		int myWidth = getPreferredSize().width;
//		int otherWidth = other.getPreferredSize().width;
//		if (otherWidth > myWidth)
//		{
//			setPreferredSize(new Dimension(otherWidth, getPreferredSize().height));
//			setMinimumSize(getPreferredSize());
//		}
//		else
//		{
//			other.setPreferredSize(new Dimension(myWidth, other.getPreferredSize().height));
//			other.setMinimumSize(other.getPreferredSize());
//		}
//		other.invalidate();
//		invalidate();
//	}
	
	//---------------------------------------------------------------------------------------------------
//	public JCheckBox addCheckBox(String text, String toolTip, Font f)
//	{
//		JCheckBox cb = new JCheckBox( text);
//		add(cb);
//		return cb;
//	}
//	
//	public JCheckBox addCheckBox(String text)
//	{
//		JCheckBox cb = new JCheckBox(text);
//		add(cb);
//		return cb;
//	}
//	
	//---------------------------------------------------------------------------------------------------
//	static public JCheckBox makeCheckBox(String text, String toolTip, Font f)
//	{
//		JCheckBox cb = new JCheckBox(text);
//		if (f != null) cb.setFont(f);
//		return cb;
//	}
//	
//	static public JCheckBox makeCheckBox(String text, Font f)
//	{
//		JCheckBox cb = new JCheckBox(text); 
//		if (f != null) cb.setFont(f);
//		return cb;
//	}
//	
//	static public JCheckBox makeCheckBox(String text, int width)
//	{
//		JCheckBox cb = makeCheckBox(text); 
//		GuiFactory.setSizes(cb, new Dimension(width, 26));
//		return cb;
//	}
//	
//	static public JCheckBox makeCheckBox(String text)	{	return makeCheckBox(text, (Font) null);	}
}
