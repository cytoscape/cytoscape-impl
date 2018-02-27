package org.cytoscape.internal.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.internal.prefs.lib.BoxComponent;
import org.cytoscape.internal.prefs.lib.ColorMenuButton;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.TextTraits;
import org.cytoscape.internal.prefs.lib.VBox;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

public class PrefsText extends AbstractPrefsPanel {

	protected PrefsText(Cy3PreferencesPanel dlog) {
		super(dlog, "text");
		setBorder(BorderFactory.createEmptyBorder(20,32,0,0));
	}
//    private final Font panelFont = new Font("Dialog", Font.PLAIN, 10);
//	private int fCurrentTextTraitIndex = 0; // Text trait radio buttons correspond 0-5
	private TextTraits fCurrentTextTrait; 
	final private static String kDefaultFont = "SansSerif";
	final private static String kStringListTypeStyles[] = { "plain", "bold", "italic", "bold-italic" };
	final private static String kTraitNames[] = { "Nodes", "Edges", "Annotations", "Legend" };  //"Tables",  , "Charts"

	final private static int kTablesIndex = 0;
//	final private static int kLayoutAxesIndex = 2;
//	final private static int kLayoutAxesNumIndex = 3;
//	final private static int kStatTableIndex = 6;
	

	public final static int kNumTextTraits = kTraitNames.length;
	private JComboBox<String> fCbTextSize;
//	private JComboBox fCbTextJustification;
//	private JLabel fLblTextJustification;
	private JComboBox<String> fCbTextStyle;
	private JLabel fLblTextStyle;
	private JComboBox<String> fCbTextFont;
	private ColorMenuButton fBtnTextColor;
	private JLabel fLblTextColor;
	private JEditorPane fSamplePane;
	public static List<String> sFontFamilies = null;
	
	private JButton fBtnSetAllTT;
	private final JRadioButton[] fRadioButtons = new JRadioButton[kNumTextTraits];
	private ButtonGroup btnGroup = new ButtonGroup();
	private String getSelectedTextName()
	{
		for (JRadioButton btn : fRadioButtons)
			if (btn.isSelected())
				return btn.getName();
		return kTraitNames[0];
	}
	
	private AntiAliasedPanel fFunctionPanel;
	private AntiAliasedPanel fTextSettingsPanel;
//	private BoxSubPanel fSamplePanel;
	private Map<String, TextTraits> fontMap;

	@Override public void initUI()
	{
		super.initUI();
		fontMap = new HashMap<String, TextTraits>();
		Box line = Box.createHorizontalBox();
		line.add(getFunctionPanel());
		line.add(initTextSettingsPanel());
		
		
		Box page = Box.createVerticalBox();
	    JLabel line0 = new JLabel("Here you can set the font selection used in your default styles.");
	    line0.setFont(ital11);
		page.add(new HBox(line0));
		JLabel line1 = new JLabel("Note that changing styles or attribute mapping may overwrite default fonts.");
	    line1.setFont(ital11);
		page.add(new HBox(line1));
		page.add(Box.createRigidArea(new Dimension(20,20)));
		page.add(line);
		Box line2 = Box.createHorizontalBox();
		line2.add(getExampleTextPanel());
		page.add(line2);
//		addLeftSpacer();
//		addRightSpacer();
		add(page);
	}
	
	private AntiAliasedPanel getFunctionPanel()
	{
		if (fFunctionPanel == null)
		{
			VBox p = new VBox("Components", true, false);
			
			AntiAliasedPanel.setSizes(p, new Dimension(240, 220));
			for (int currTrait = 0; currTrait < kNumTextTraits; currTrait++)
			{
				fRadioButtons[currTrait] = new JRadioButton(kTraitNames[currTrait]);
				final int trait = currTrait;
				fRadioButtons[currTrait].addActionListener(new ActionListener()	
				{	public void actionPerformed(ActionEvent e)		{		changeCurrentTarget(trait);	}			});
//				fRadioButtons[currTrait].setFont(panelFont);
				btnGroup.add(fRadioButtons[currTrait]);
				p.add(new HBox(true, true, fRadioButtons[currTrait]));
			}
			fRadioButtons[0].setSelected(true);
			fBtnSetAllTT = new JButton("Use For All");
			fBtnSetAllTT.addActionListener(new ActionListener()		{	public void actionPerformed(ActionEvent e)		{		updateAllTraits();		}	});
//			fBtnSetAllTT.setFont(panelFont);
			p.addSpacer();
			p.add(fBtnSetAllTT);
			p.addSpacer();
			p.add(Box.createVerticalGlue());
			p.addSpacer();
			fFunctionPanel = p;
		}
		return fFunctionPanel;
	}
	ActionListener act = new ActionListener()	
	{		public void actionPerformed(ActionEvent e)	{		textSettingChanged();	}			};
	
	private AntiAliasedPanel initTextSettingsPanel()
	{
		if (fTextSettingsPanel == null)
		{
			VBox settingsPanel = new VBox("Text Settings", true, true);
			AntiAliasedPanel.setSizes(settingsPanel, new Dimension(310, 220));
			fCbTextFont = new JComboBox<String>();
			fCbTextFont.setMaximumRowCount(32);
			AntiAliasedPanel.setSizes(fCbTextFont, new Dimension(230, 27));
			fCbTextFont.addItem(kDefaultFont);
			if (sFontFamilies == null) initFonts();
			for(String familyName : sFontFamilies)
				fCbTextFont.addItem(familyName);
			fCbTextFont.addActionListener(new ActionListener()	
			{		public void actionPerformed(ActionEvent e)	{		textSettingChanged();	}			});
//			settingsPanel.addLeading();
		
			fCbTextSize = new JComboBox<String>(new String[] { "8", "9", "10", "11", "12", "14", "18", "24"} );
			fCbTextSize.setSelectedIndex(4);
			fCbTextSize.addActionListener(act);
			fCbTextSize.setMaximumSize(new Dimension(72, 27));
			fBtnTextColor = new ColorMenuButton();
			fBtnTextColor.setText("Color");
			fBtnTextColor.setColor(Color.black);
			fBtnTextColor.addActionListener(act);
			fLblTextColor = new JLabel("Color:");
			
			fCbTextStyle = new JComboBox<String>(kStringListTypeStyles);
			fCbTextStyle.addActionListener(act);
			fLblTextStyle = new JLabel("Style:");

			settingsPanel.add(new HBox(new JLabel("Font:"), fCbTextFont));
			settingsPanel.add(new HBox( new JLabel("Size:"), fCbTextSize));
			settingsPanel.add(new HBox(fLblTextColor, fBtnTextColor));
			settingsPanel.add(new HBox( fLblTextStyle, fCbTextStyle));
			settingsPanel.add(Box.createVerticalGlue());
			fTextSettingsPanel = settingsPanel;
		}
		
		return fTextSettingsPanel;
	}
//	SElement fElement;
	
	private AntiAliasedPanel getExampleTextPanel()
	{
		VBox examplePanel = new VBox("Sample Text", true, true);
		examplePanel.setBackground(Color.white);
		AntiAliasedPanel.setSizes(examplePanel, new Dimension(540, 120));
		fSamplePane = new JEditorPane();
		fSamplePane.setBackground(Color.white);
		AntiAliasedPanel.setSizes(fSamplePane, new Dimension(520, 100));
		fSamplePane.setText("Lazy evaluation showed excessive use of Cytoscape will make her just be quicker in graphs.");  
		examplePanel.add(fSamplePane);
		examplePanel.add(Box.createVerticalGlue());
		//Note: the following CENTER_ALIGNMENT is needed to prevent the label 
		//from resizing and leaving a gap on the left or right side
		//when the font size is changed.
//		fSamplePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		return examplePanel;
	}
	
	static public void initFonts()
	{
		if (sFontFamilies != null) return;
		sFontFamilies = new ArrayList<String>();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font fa[] = ge.getAllFonts();
		for (int i = 0; i < fa.length; i++)
		{
			String family = fa[i].getFamily();
			if (!sFontFamilies.contains(family))
				sFontFamilies.add(family);
		}
		String[] predefined = { "Dialog", "Monospaced", "Serif" };			//, "SansSerif"
		for (String s : predefined)
		{
			sFontFamilies.remove(s);
			sFontFamilies.add(0,s);
		}
	}
//
//	private TextTraits getCurrentTextTraits()
//	{
//		TextTraits deftrait = makeDefaultTextTraits();
//		return deftrait;
//	}

	private void extractCurrentTrait()
	{
		if (fCurrentTextTrait == null)
			fCurrentTextTrait = makeDefaultTextTraits(getSelectedTextName());
		
		String fontName = (String) fCbTextFont.getSelectedItem();
		if (fontName == null) fontName = ""; 
		fCurrentTextTrait.setFontName(fontName);
		fCurrentTextTrait.setSize(Integer.parseInt((String) fCbTextSize.getSelectedItem()));
		fCurrentTextTrait.setColor(fBtnTextColor.getColor());
//		fontPref.setAttribute("just", (String) fCbTextJustification.getSelectedItem());
		fCurrentTextTrait.setStyle(TextTraits.stringToStyle((String) fCbTextStyle.getSelectedItem()));
//		if (verbose)
//			System.out.println("extract: " + fCurrentTextTrait.getTTName() + " = " +  fCurrentTextTrait);
		fontMap.put(fCurrentTextTrait.getTTName(), fCurrentTextTrait);
	}

	private TextTraits makeDefaultTextTraits(String name)
	{
		fCurrentTextTrait = new TextTraits();
		fCurrentTextTrait.setTTName(name);
		return fCurrentTextTrait;
	}

	private void updateAllTraits()
	{
//		int savedCurrentTextTrait = fCurrentTextTraitIndex;
//		for (int i = 0; i < kNumTextTraits; i++)
//		{
//			fCurrentTextTraitIndex = i;
//			extractCurrentTrait();
//		}
//		fCurrentTextTraitIndex = savedCurrentTextTrait;
	}

	// respond to radio buttons changing the trait we're editing
	private void changeCurrentTarget(int newTrait)
	{
		extractCurrentTrait();			
		fCurrentTextTrait = fontMap.get(kTraitNames[newTrait]);
		installCurrentTrait();
		installSampleText(fCurrentTextTrait); 
	}

	
	private void textSettingChanged()
	{
		if (setting) return; 
		extractCurrentTrait();
		installSampleText(fCurrentTextTrait); 
	}
	
	
	private void installSampleText(TextTraits t)
	{
		if (t == null) return;
		String font = t.getFontName();
		int size = t.getSize();
		Color color = t.getColor();
		String style = t.getStyleString();
		int styleInt = TextTraits.stringToStyle(style);
		Font newFont = new Font(font, styleInt, size);
		fSamplePane.setFont(newFont);
		fSamplePane.setForeground(color);
	}
		
	boolean setting = false;
	private void installCurrentTrait()
	{
		if (setting) return;
		setting = true;
		if (fCurrentTextTrait == null)
		{	System.out.println("fCurrentTextTrait == null"); return;  }

//		System.out.println("install: " + fCurrentTextTrait.getTTName() + " = " +  fCurrentTextTrait);
		fCbTextFont.setSelectedItem(fCurrentTextTrait.getFontName());
		fCbTextSize.setSelectedItem("" + fCurrentTextTrait.getSize());
		fBtnTextColor.setColor(fCurrentTextTrait.getColor());
		fCbTextStyle.setSelectedItem(TextTraits.styleToString(fCurrentTextTrait.getStyle()));
		setting = false;
	}
//------------------------------------------------------------------------------

	@Override public void install(Properties properties)
	{
		if (verbose)
		for (Object key : properties.keySet())
		{
			System.out.println(key + " : " + properties.getProperty(key.toString()));
		}
		
		for (String name : kTraitNames)
		{
			String strTrait = properties.getProperty("text." + name);
			TextTraits traits = (strTrait == null) ? makeDefaultTextTraits(name) : new TextTraits(name, strTrait);
			fontMap.put(name, traits);
		}
		String curName = properties.getProperty("text.current");
		if (curName == null) 
			curName = "Nodes";
		fCurrentTextTrait = fontMap.get(curName);
	}

//------------------------------------------------------------------------------
    @Override public void extract(Properties properties)
    {
		if (verbose)   System.out.println("--  " + getName());
		
		for (String key : fontMap.keySet())
		{
			TextTraits traits = fontMap.get(key);
			properties.setProperty("text." + key, traits.toString());
		}
		if (fCurrentTextTrait != null) 
			properties.setProperty("text.current", fCurrentTextTrait.getTTName());
		
		dump(properties, "text");
    }


}
