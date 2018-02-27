package org.cytoscape.internal.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.cytoscape.internal.prefs.lib.ColorBrewer;
import org.cytoscape.internal.prefs.lib.ColorPane;
import org.cytoscape.internal.prefs.lib.Colors;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.VBox;

public class PrefsColors extends AbstractPrefsPanel {

	public static Border doubleRaised = BorderFactory.createCompoundBorder( BorderFactory.createRaisedBevelBorder(), BorderFactory.createRaisedBevelBorder());
	HBox lineOfPalettes;
	HBox lineOfPalettes2;
	protected PrefsColors(Cy3PreferencesRoot dlog) {
		super(dlog, "color");
		lineOfPalettes = new HBox();
		lineOfPalettes2 = new HBox();
//		lineOfPalettes.setBorder(Borders.red);
//		lineOfPalettes2.setBorder(Borders.magenta);
	}
	public ActionListener ctrlListener = new ActionListener() { public void actionPerformed(ActionEvent e)  { populate(); } };
    ButtonGroup buttonGroup = new ButtonGroup();
   @Override public void initUI()
    {
        super.initUI();
		Box page = Box.createVerticalBox();
//	    page.add(Box.createRigidArea(new Dimension(20,10)));
//	    page.add(new JLabel("Reserved for Colors"));
	    page.add(Box.createRigidArea(new Dimension(20,10)));
	    String s = "The Brewer Colors* are a set of color palettes that are appropriate for visualizations.\n" +
	    "The proper choice of palette is dependent on the type of attribute being mapped, \n" +
	    		"and may be further constrained by how the results will be handled downstream. \n"+
	
	  "";
	    JTextArea text = new JTextArea(s);
	    text.setOpaque(false);
	    text.setMargin(new Insets(20,30,20, 10));
	    page.add(text);
	    
	    VBox controlBox = new VBox("controls");
	    controlBox.setBorder(doubleRaised);
	    JRadioButton a = makeRadioButton("Sequential"); 
	    JRadioButton b = makeRadioButton("Divergent"); 
	    JRadioButton c = makeRadioButton("Qualitative");
	    a.setSelected(true);
	    
	    controlBox.add(new HBox(true, true, a));
	    controlBox.add(new HBox(true, true, b));
	    controlBox.add(new HBox(true, true, c));
	    
	    controlBox.add(Box.createVerticalStrut(30));
	    colorBlindSafeCheckBox = makeCheckBox("Color blind safe");
	    controlBox.add(new HBox(true, true, colorBlindSafeCheckBox));
//	    controlBox.add(new HBox(true, true, makeCheckBox("Print friendly")));
//	    controlBox.add(new HBox(true, true, makeCheckBox("Photocopy safe")));
	    controlBox.add(Box.createVerticalStrut(30));
	    controlBox.setPreferredSize(new Dimension(160, 270));
	    controlBox.setMaximumSize(new Dimension(160, 270));
	    controlBox.add(Box.createVerticalGlue());
	    HBox combined = new HBox(true, false, Box.createHorizontalStrut(18), controlBox,  
	    		Box.createHorizontalStrut(30), makeColorLine(), Box.createHorizontalStrut(30));
//	    combined.setBorder(Borders.doubleRaised);
    
//	    HBox line = new HBox(true, true, makeColorLine("Sequential"), makeColorLine("Divergent"), makeColorLine("Qualitative"));
	    combined.setSpacer(new Dimension(30,30));
	    page.add(combined);
	    populate();
		add(page);   
	}
   
   VBox makeColorLine()
   {
   		String name = "Pick a color scheme";
   		HBox line = new HBox();
	    
	    JLabel label = new JLabel(name);
	    HBox justLabel = new HBox(Box.createRigidArea(new Dimension(32,7)), label);
	    VBox box = new VBox(true, true, justLabel, line);
	    box.setAlignmentX(CENTER_ALIGNMENT);
//	    line.setPreferredSize(new Dimension(220, 100));
		box.setBorder(doubleRaised);
		box.setMaximumSize(new Dimension(420, 270));
		
		box.add(lineOfPalettes);
//		box.add(Box.createRigidArea(new Dimension(8,4)));
		box.add(lineOfPalettes2);
		box.add(Box.createRigidArea(new Dimension(8,20)));

		box.add(Box.createVerticalGlue());
	    return box;
   }
   
   //
   JRadioButton makeRadioButton(String s)
   {
	   JRadioButton btn = new JRadioButton(s);
	   btn.addActionListener(ctrlListener);
	   btn.setActionCommand(s);
	   buttonGroup.add(btn); 
	   return btn;
   }
   JCheckBox makeCheckBox(String s)
   {
	   JCheckBox box = new JCheckBox(s);
	   box.addActionListener(ctrlListener);
	   return box;
   }
	

    boolean colorBlindSafe = true;
    JCheckBox colorBlindSafeCheckBox = makeCheckBox("Color blind safe");
    int count = 0;
    int offset = 0;

    public void populate()
	{
		String state = buttonGroup.getSelection().getActionCommand();
		colorBlindSafe = colorBlindSafeCheckBox.isSelected();
	//	System.out.println("populate " + state);
		lineOfPalettes.removeAll();
		lineOfPalettes2.removeAll();
		ColorBrewer[] brewer = null;
		if ("Divergent".equals(state))  		brewer = ColorBrewer.getDivergingColorPalettes(colorBlindSafe);
		if ("Sequential".equals(state)) 		brewer = ColorBrewer.getSequentialColorPalettes(colorBlindSafe);
		if ("Qualitative".equals(state)) 	brewer = ColorBrewer.getQualitativeColorPalettes(colorBlindSafe);
	    
	    buildPalettes(brewer);
	    lineOfPalettes.setVisible(false);
	    lineOfPalettes.setVisible(true);			// TODO REFRESH
    }


void buildPalettes(int[] starts)
{
	for (int i : starts)
	{
		lineOfPalettes.add(makeColorColumn(i + offset));
	    lineOfPalettes.add(Box.createRigidArea(new Dimension(8,4)));
	}
	for (int i : starts)
	{
		lineOfPalettes2.add(makeColorColumn(i + offset));
	    lineOfPalettes2.add(Box.createRigidArea(new Dimension(8,4)));
	}

}

void buildPalettes(ColorBrewer[] brewers)
{
	int len = brewers.length;
	if (len < 9)
	for (ColorBrewer brew : brewers)
	{
		Color [] colors = brew.getColorPalette(5);
		lineOfPalettes.add(makeColorColumn(colors));
	    lineOfPalettes.add(Box.createRigidArea(new Dimension(8,4)));
	}
	else
	{
		int i=0;
		for (; i < len / 2; i++)
		{
			Color [] colors = brewers[i].getColorPalette(5);
			lineOfPalettes.add(makeColorColumn(colors));
		    lineOfPalettes.add(Box.createRigidArea(new Dimension(8,4)));
			
		}
		for (; i < len; i++)
		{
			Color [] colors = brewers[i].getColorPalette(5);
			lineOfPalettes2.add(makeColorColumn(colors));
		    lineOfPalettes2.add(Box.createRigidArea(new Dimension(8,4)));
		
		}
	
	}
}

VBox makeColorColumn(int start)
{
	VBox box = new VBox(false, false);
	for (int row = 0; row < 5; row++)
	{
		Color color = Colors.colorFromIndex(start + row);
		ColorPane c = new ColorPane(row, 0, color, null);
		box.add(c);
	}
	box.setBorder(BorderFactory.createLineBorder(Color.orange, 3));
	return box;
}

VBox makeColorColumn(Color[] colors)
{
	VBox box = new VBox(false, false);
	int row = 0;
	for (Color color : colors)
	{
		ColorPane c = new ColorPane(row++, 0, color, null);
		box.add(c);
	}
	box.setBorder(BorderFactory.createLineBorder(Color.orange, 3));
	return box;
}
    
    
 class ClickableBox extends VBox implements MouseListener {

	public ClickableBox(boolean useSpacing, boolean useGlue)
	{
		super(useSpacing, useGlue);
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("Set Selected");

	}

	@Override	public void mousePressed(MouseEvent e) {	}
	@Override	public void mouseReleased(MouseEvent e) {}
	@Override	public void mouseEntered(MouseEvent e) {	}
	@Override	public void mouseExited(MouseEvent e) {	}

}

 //   http://colorbrewer2.org
}
