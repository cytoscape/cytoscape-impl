package org.cytoscape.internal.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.cytoscape.internal.prefs.lib.ColorBrewer;
import org.cytoscape.internal.prefs.lib.ColorPane;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.VBox;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

public class PrefsColor extends AbstractPrefsPanel {

	// http://colorbrewer2.org

	public static Border doubleRaised = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
			BorderFactory.createRaisedBevelBorder());
	HBox lineOfPalettes;
	HBox lineOfPalettes2;

	HashMap<String,String> paletteChoices = new HashMap<String, String>();
	static String[] categories = {"Sequential", "Divergent", "Qualitative" };
	static String[] defaults = {"Blue-Greens", "Red-White-Blue", "Darker" };
	
	protected PrefsColor(Cy3PreferencesPanel dlog) {
		super(dlog, "color");
		lineOfPalettes = new HBox();
		lineOfPalettes2 = new HBox();
		// lineOfPalettes.setBorder(Borders.red);
		// lineOfPalettes2.setBorder(Borders.magenta);
		for (int i=0; i<3; i++)
			paletteChoices.put("palette." + categories[i],  defaults[i]);
	}

	public ActionListener ctrlListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
			String name = e.getActionCommand();
			String currentPalette = paletteChoices.get(name);
			populate(currentPalette);
			setSchemeName(currentPalette);
		}
	};
	ButtonGroup buttonGroup = new ButtonGroup();

	@Override
	public void initUI() {
		super.initUI();
		Box page = Box.createVerticalBox();
		page.add(Box.createRigidArea(new Dimension(20, 10)));
		String s = "The Brewer Colors are a set of color palettes that are appropriate for visualizations.\n"
				+ "Sequential and Divergent palettes are used for continuous mapping functions. \n"
				+ "Use a divergent palette when the range of values spans negative to positive numbers. \n"
				+ "Qualitative palettes are suitable for discrete maps (i.e., for categorical data).";
		JTextArea text = new JTextArea(s);
		text.setOpaque(false);
		text.setFont(ital11);
		text.setMargin(new Insets(20, 30, 20, 10));
		page.add(text);

		VBox controlBox = new VBox("controls");
		controlBox.setBorder(doubleRaised);
		JRadioButton[] btns = new JRadioButton[3];
		
		for (int i=0; i<3;i++)
		{
			btns[i] = makeRadioButton(categories[i]);
			controlBox.add(new HBox(true, true, btns[i]));
		}
		btns[0].setSelected(true);

		controlBox.add(Box.createVerticalStrut(30));
		colorBlindSafeCheckBox = makeCheckBox("Color blind safe");
		controlBox.add(new HBox(true, true, colorBlindSafeCheckBox));
		controlBox.add(Box.createVerticalStrut(30));
		controlBox.setPreferredSize(new Dimension(160, 270));
		controlBox.setMaximumSize(new Dimension(160, 270));
		controlBox.add(Box.createVerticalGlue());
		HBox combined = new HBox(true, false, Box.createHorizontalStrut(18), controlBox, Box.createHorizontalStrut(30),
				makeColorLine(), Box.createHorizontalStrut(30));

		combined.setSpacer(new Dimension(30, 30));
		page.add(combined);
		populate(paletteChoices.get("Sequential"));
		add(page);
	}
	
	
	private JLabel schemeName = new JLabel("Blues");
	private void setSchemeName(String s)
	{
		schemeName.setText(s);
	}
	
	
	VBox makeColorLine() {
		String name = "Color scheme: ";
		HBox line = new HBox();
		JLabel label = new JLabel(name);
		HBox justLabel = new HBox(true, true, Box.createRigidArea(new Dimension(32, 7)), label, schemeName);
		VBox box = new VBox(true, true, justLabel, line);
		box.setAlignmentX(CENTER_ALIGNMENT);
		box.setBorder(doubleRaised);
		box.setMaximumSize(new Dimension(420, 270));
		box.add(lineOfPalettes);
		box.add(lineOfPalettes2);
		box.add(Box.createRigidArea(new Dimension(8, 20)));
		box.add(Box.createVerticalGlue());
		return box;
	}

	//
	JRadioButton makeRadioButton(String s) {
		JRadioButton btn = new JRadioButton(s);
		btn.addActionListener(ctrlListener);
		btn.setActionCommand(s);
		buttonGroup.add(btn);
		return btn;
	}

	JCheckBox makeCheckBox(String s) {
		JCheckBox box = new JCheckBox(s);
		box.addActionListener(ctrlListener);
		return box;
	}

	boolean colorBlindSafe = true;
	JCheckBox colorBlindSafeCheckBox = makeCheckBox("Color blind safe");
	int count = 0;
	int offset = 0;
	String divergentScheme = "Red-White-Blue";
	String sequentialScheme = "Blue-Violet";
	String qualScheme = "Pastels";
	//---------------------------------------------------------
	public void populate(String selection) {
		String state = buttonGroup.getSelection().getActionCommand();
		colorBlindSafe = colorBlindSafeCheckBox.isSelected();
		// System.out.println("populate " + state);
		lineOfPalettes.removeAll();
		lineOfPalettes2.removeAll();
		ColorBrewer[] brewer = null;
		if ("Divergent".equals(state))
			brewer = ColorBrewer.getDivergingColorPalettes(colorBlindSafe);
		if ("Sequential".equals(state))
			brewer = ColorBrewer.getSequentialColorPalettes(colorBlindSafe);
		if ("Qualitative".equals(state))
			brewer = ColorBrewer.getQualitativeColorPalettes(colorBlindSafe);
		if (selection == null) 
			selection = brewer[0].getPaletteDescription();
		setSchemeName(paletteChoices.get("palette." + state));

		buildPalettes(brewer, selection);
		lineOfPalettes.setVisible(false);
		lineOfPalettes.setVisible(true); // TODO REFRESH
	}

	void buildPalettes(ColorBrewer[] brewers, String selectionName) {
		int len = brewers.length;
		if (len < 9)
			for (ColorBrewer brew : brewers) {
				Color[] colors = brew.getColorPalette(5);
				lineOfPalettes.add(makeColorColumn(brew.getPaletteDescription(), colors, selectionName));
				lineOfPalettes.add(Box.createRigidArea(new Dimension(8, 4)));
			}
		else {			// make two rows
			int i = 0;
			for (; i < len / 2; i++) {
				Color[] colors = brewers[i].getColorPalette(5);
				lineOfPalettes.add(makeColorColumn(brewers[i].getPaletteDescription(), colors, selectionName));
				lineOfPalettes.add(Box.createRigidArea(new Dimension(8, 4)));

			}
			for (; i < len; i++) {
				Color[] colors = brewers[i].getColorPalette(5);
				lineOfPalettes2.add(makeColorColumn(brewers[i].getPaletteDescription(), colors, selectionName));
				lineOfPalettes2.add(Box.createRigidArea(new Dimension(8, 4)));

			}

		}
	}

	VBox makeColorColumn(String description, Color[] colors, String selection) {
		VBox box = new VBox(false, false);
		int row = 0;
		for (Color color : colors) {
			ColorPane c = new ColorPane(row++, 0, color, null);
			c.setEnabled(false);
			c.addMouseListener( new MouseAdapter(){
			    @Override public void mouseClicked(MouseEvent e) 	    
			    		{  select(box, description);    }	});
			box.add(c);
		}
		box.setBorder(description.equals(selection) ? red4 : orange4);
		box.addMouseListener(new MouseAdapter(){
		    @Override public void mouseClicked(MouseEvent e) 	    
		    {   		select(box, description);  	} 	 });
		allVisPalettes.put(description, box);
		return box;
	}
Map<String, VBox> allVisPalettes = new HashMap<String, VBox>();

static Border orange4 = BorderFactory.createLineBorder(Color.orange, 4);
static Border red4 = BorderFactory.createLineBorder(Color.red, 4);
	
	private	void select(JComponent box, String description)
	{
		for (String str : allVisPalettes.keySet())
		{	
			VBox v = allVisPalettes.get(str);
			v.setBorder((v == box) ? red4 : orange4);
		}
		setSchemeName(description);
		setCurrentPalette(description);
	}
	
	private void setCurrentPalette(String description) {
		String prop = "palette." + buttonGroup.getSelection().getActionCommand();
		paletteChoices.put(prop, description);
		
	}


	@Override public void install(Properties properties)
	{
	    	for (String name : categories)
	    	{
	    		String prop = "palette." + name;
	    		String extant = properties.getProperty(prop);
	    		if (extant != null)
	    			paletteChoices.put(prop, extant);
	    	}
	}

	   
	protected String getPropFileName()	{ return "cytoscape 3";	}
	@Override  public void extract(Properties properties)
    {
	   System.out.println("--  " + getName());
		for (String name : categories)
		{
			String prop = "palette." + name;
			String cur = paletteChoices.get(prop);
			if (cur != null)
				properties.put(prop, cur);
		}
		dump(properties, "palette");
    }

}
