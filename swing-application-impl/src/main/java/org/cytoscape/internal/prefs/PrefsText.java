package org.cytoscape.internal.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

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
import org.cytoscape.internal.prefs.lib.ParseUtil;
import org.cytoscape.internal.prefs.lib.TextTraits;
import org.cytoscape.internal.prefs.lib.VBox;

public class PrefsText extends AbstractPrefsPanel {

	protected PrefsText(Cy3PreferencesRoot dlog) {
		super(dlog, "text");
		setBorder(BorderFactory.createEmptyBorder(20,32,0,0));
	}
//    private final Font panelFont = new Font("Dialog", Font.PLAIN, 10);
	private int fCurrentTextTrait = 0; // Text trait radio buttons correspond 0-7
	final private static String kDefaultFont = "SansSerif";
	final private static String kStringListTypeStyles[] = { "plain", "bold", "italic", "bold-italic" };
	final private static String kRadioButtonNames[] = { "Tables", "Annotations", "Node Labels", "Edge Labels", "Legend", "Charts" };
	final private static String kTraitNames[] = { 	"Tables", 	"Annotations", 	"Node Labels", 	"Edge Labels", 	"Legend", "Charts" };

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
	public static TreeSet<String> sFontFamilies = null;
	
	private JButton fBtnSetAllTT;
	private final JRadioButton[] fRadioButtons = new JRadioButton[kNumTextTraits];
	private AntiAliasedPanel fFunctionPanel;
	private AntiAliasedPanel fTextSettingsPanel;
//	private BoxSubPanel fSamplePanel;
	
	private boolean fInitializingUI;
	private boolean fInitialized = false;

	@Override public void initUI()
	{
		fInitializingUI = true;
		super.initUI();
		
		Box line = Box.createHorizontalBox();
		line.add(getFunctionPanel());
		line.add(initTextSettingsPanel());
		
		
		Box page = Box.createVerticalBox();
		page.add(line);
		Box line2 = Box.createHorizontalBox();
		line2.add(getExampleTextPanel());
		page.add(line2);
//		addLeftSpacer();
//		addRightSpacer();
		fInitializingUI = false;
		add(page);
	}

	private AntiAliasedPanel getFunctionPanel()
	{
		if (fFunctionPanel == null)
		{
			VBox p = new VBox("Components", true, false);
			
			AntiAliasedPanel.setSizes(p, new Dimension(240, 220));
			ButtonGroup btnGroup = new ButtonGroup();
			for (int currTrait = 0; currTrait < kNumTextTraits; currTrait++)
			{
				fRadioButtons[currTrait] = new JRadioButton(kRadioButtonNames[currTrait]);
				final int trait = currTrait;
				fRadioButtons[currTrait].addActionListener(new ActionListener()	{	public void actionPerformed(ActionEvent e)		{		changeCurrentTarget(trait);	}			});
//				fRadioButtons[currTrait].setFont(panelFont);
				btnGroup.add(fRadioButtons[currTrait]);
				p.add(new HBox(true, true, fRadioButtons[currTrait]));
			}
			
			fBtnSetAllTT = new JButton("Use For All");
			fBtnSetAllTT.addActionListener(new ActionListener()		{	public void actionPerformed(ActionEvent e)		{		updateAllTraits();		}	});
//			fBtnSetAllTT.setFont(panelFont);
			p.add(fBtnSetAllTT);
			p.addSpacer();
			fFunctionPanel = p;
		}
		return fFunctionPanel;
	}
	
	private AntiAliasedPanel initTextSettingsPanel()
	{
		if (fTextSettingsPanel == null)
		{
			VBox p = new VBox("Text Settings", true, true);
			AntiAliasedPanel.setSizes(p, new Dimension(310, 220));
			fCbTextFont = new JComboBox<String>();
			fCbTextFont.setMaximumRowCount(32);
			AntiAliasedPanel.setSizes(fCbTextFont, new Dimension(230, 27));
			fCbTextFont.addItem(kDefaultFont);
			if (sFontFamilies == null) initFonts();
			for(String familyName : sFontFamilies)
				fCbTextFont.addItem(familyName);
			fCbTextFont.addActionListener(new ActionListener()	{		public void actionPerformed(ActionEvent e)	{		textSettingChanged();	}			});
			p.add(new BoxComponent(new JLabel("Font:"), fCbTextFont));
			p.addLeading();
		
			fCbTextSize = new JComboBox<String>(new String[] { "8", "9", "10", "11", "12"} );
			fCbTextSize.setSelectedIndex(2);
			fCbTextSize.addActionListener(new ActionListener()	{		public void actionPerformed(ActionEvent e)	{		textSettingChanged();	}			});
			fCbTextSize.setMaximumSize(new Dimension(60, 28));
			fBtnTextColor = new ColorMenuButton();
			fBtnTextColor.setText("Color");
			fBtnTextColor.setColor(Color.black);
			fBtnTextColor.addActionListener(new java.awt.event.ActionListener()	{	public void actionPerformed(ActionEvent e)			{				fBtnTextColor_actionPerformed(e);			}			});
			fLblTextColor = new JLabel("Color:");
			
//			fCbTextJustification = new JComboBox(kStringListTypeJusts);
//			fCbTextJustification.addActionListener(new ActionListener()			{	public void actionPerformed(ActionEvent e)	{		textSettingChanged();	}			});
//			fLblTextJustification = new JLabel("Justification:");

			fCbTextStyle = new JComboBox<String>(kStringListTypeStyles);
			fCbTextStyle.addActionListener(new ActionListener()		{		public void actionPerformed(ActionEvent e)	{		textSettingChanged();	}			});
			fLblTextStyle = new JLabel("Style:");
			p.add(new BoxComponent( new JLabel("Size:"), fCbTextSize));
//			p.addLeading();
			p.add(new BoxComponent(fLblTextColor, fBtnTextColor));
//			p.addLeading();
			p.add(new BoxComponent( fLblTextStyle, fCbTextStyle));
			p.add(Box.createVerticalGlue());
			fTextSettingsPanel = p;
		}
		
		return fTextSettingsPanel;
	}
//	SElement fElement;
	
	private AntiAliasedPanel getExampleTextPanel()
	{
		AntiAliasedPanel p = new AntiAliasedPanel("Sample Text");
		AntiAliasedPanel.setSizes(p, new Dimension(550, 130));
		fSamplePane = new JEditorPane();
		fSamplePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		fSamplePane.setBackground(Color.white);
//			fSamplePane.setFont(panelFont);
		fSamplePane.setText("The sly fox jumped over the lazy dog.");
		p.add(fSamplePane);
		//Note: the following CENTER_ALIGNMENT is needed to prevent the label 
		//from resizing and leaving a gap on the left or right side
		//when the font size is changed.
//		fSamplePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		return p;
	}
	
	static public void initFonts()
	{
		sFontFamilies = new TreeSet<String>();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font fa[] = ge.getAllFonts();
		for (int i = 0; i < fa.length; i++)
			sFontFamilies.add(fa[i].getFamily());
	}

	private TextTraits getCurrentTextTraits()
	{
		TextTraits deftrait = makeDefaultTextTraits();
		return deftrait;
	}
	    
	    @Override public Map<String,String> extract()
	    {
	    	return new HashMap<String,String>();
	    }

	@Override public void install(Map<String,String> p)
	{
//		fInitializingUI = true;
		Map<String, String>  properties = getPropertyMap();
		TextTraits trait = getCurrentTextTraits();
//		
//		if (trait == null)
//		{
//			trait = makeDefaultTextTraits();
//			fElement.getChild(kTraitNames[fCurrentTextTrait]).addContent(trait.getAsElement());
//		}
				
		String font = trait.getFontName();
		String size = Integer.toString(trait.getSize());
		Color color = trait.getColor();// ColorUtil.colorFromString(trait.getString("color"));
//		String just = trait.getString("just");
		String style = trait.getStyleString();
		fCbTextFont.setSelectedItem(font);
		fCbTextSize.setSelectedItem(size);
		fBtnTextColor.setColor(color);
//		fCbTextJustification.setSelectedItem(just);
		fCbTextStyle.setSelectedItem(style);
		int styleInt = 0;
		if (style.equals("plain")) styleInt = Font.PLAIN;
		else if (style.equals("bold")) styleInt = Font.BOLD;
		else if (style.equals("italic")) styleInt = Font.ITALIC;
		else if (style.equals("bold-italic")) styleInt = Font.BOLD | Font.ITALIC;
		installSampleText(font, size, color, styleInt);			//just, 
		for (int currTrait = 0; currTrait < kNumTextTraits; currTrait++)
			fRadioButtons[currTrait].setSelected(currTrait == fCurrentTextTrait);
//		fInitializingUI = false;
		if (!fInitialized)
		{
			fInitialized = true;
			changeCurrentTarget(kTablesIndex);
		}
	}

	private void installSampleText(String font, String size, Color color, int styleInt)			//String just, 
	{
		Font newFont = new Font(font, styleInt, ParseUtil.getInteger(size, 10));
		fSamplePane.setFont(newFont);
//		fSamplePane.setText("Lazy Dog");
		fSamplePane.setForeground(color);

	}
	

	private void extractCurrentTrait()
	{
//		TextTraits fontPref = getCurrentTextTraits();
//		if (fontPref == null)
//		{
//			fontPref = makeDefaultTextTraits();
////			getPrefsElement().getChildSElement(kTraitNames[fCurrentTextTrait]).addContent(fontPref);
//		}
//		
//		String fontName = (String) fCbTextFont.getSelectedItem();
//		if (fontName == null) fontName = ""; 
//		fontPref.setFontName(fontName);
//		fontPref.setSize(Integer.parseInt((String) fCbTextSize.getSelectedItem()));
//		fontPref.setColor(fBtnTextColor.getColor());
////		fontPref.setAttribute("just", (String) fCbTextJustification.getSelectedItem());
//		fontPref.setStyle(ParseUtil.stringToStyle((String) fCbTextStyle.getSelectedItem(), false));
////		boolean needsLayoutDiscrim = fCurrentTextTrait == kLayoutAxesIndex || fCurrentTextTrait == kLayoutAxesNumIndex;
////		String discrimName =  needsLayoutDiscrim ? "name" : null;
////		String discrimValue = needsLayoutDiscrim ? (fCurrentTextTrait == kLayoutAxesIndex) ? "label" : "numbers" : null;
////		Prefs.setPrefsAtPath(fElement, kTraitPaths[fCurrentTextTrait], fontPref.getAsElement(), discrimName, discrimValue);
//		return null; // fontPref.getAsElement();
	}

	private static TextTraits makeDefaultTextTraits()
	{
		TextTraits fontPref = new TextTraits();
		return fontPref;
	}

	private void updateAllTraits()
	{
		int savedCurrentTextTrait = fCurrentTextTrait;
		for (int i = 0; i < kNumTextTraits; i++)
		{
			fCurrentTextTrait = i;
			extractCurrentTrait();
		}
		fCurrentTextTrait = savedCurrentTextTrait;
	}

	void fBtnTextColor_actionPerformed(ActionEvent e)
	{
		if (fInitializingUI) return;
		Color c = fBtnTextColor.getColor();
		fSamplePane.setForeground(c);
		extractCurrentTrait();
	}

	// respond to radio buttons changing the trait we're editing
	private void changeCurrentTarget(int newTrait)
	{
		if (fInitializingUI) return;
		extractCurrentTrait();
		fCurrentTextTrait = newTrait;
//		install();
		
		boolean useColorStyle = true;  //fCurrentTextTrait != kTablesIndex && fCurrentTextTrait != kStatTableIndex;
//		boolean useJustification = fCurrentTextTrait == kLayoutGraphNotesIndex || fCurrentTextTrait == kLayoutTextBoxesIndex;
		fBtnTextColor.setEnabled(useColorStyle);
		fLblTextColor.setEnabled(useColorStyle);
		fCbTextStyle.setEnabled(useColorStyle);
		fLblTextStyle.setEnabled(useColorStyle);
//		fCbTextJustification.setEnabled(useJustification);
//		fLblTextJustification.setEnabled(useJustification);
	}
//	public static SElement getDefaults()
//    {
//		SElement p = new TextTraits().getAsElement();
//		return p;
//    }
	
//	public static void setDefaults(SElement p)
//    {
//		TextTraits deftrait = makeDefaultTextTraits();
			

		
    	// 		"ChartData/Graph", 	"ChartData/Graph", 	"ChartData/Legend/TextTraits" };
//    	
//    	if (p.getChild("Workspace").getChild("TextTraits")==null) {
//    		p.getChild("Workspace").addContent(makeDefaultTextTraits().getAsElement()); }
//    	
//    	if (p.getChild("Graph").getChild("TextTraits")==null) {
//    		p.getChild("Graph").addContent(makeDefaultTextTraits().getAsElement());}
//    	
//    	if (p.getChild("Annotation")==null) {
//    		SElement annElement = new SElement("Annotation");
//    		//this next line is to avoid duplicate Annotation from being created in prefs.saveState:121
//    		annElement.setString("section", "TextBox");
//    		p.addContent(annElement); }
//    	
//    	if (p.getChild("Annotation").getChild("TextTraits")==null){
//    		p.getChild("Annotation").addContent(makeDefaultTextTraits().getAsElement());}
//    		
//    	if (p.getChild("ParsedText")==null) {
//    		SElement annElement = new SElement("ParsedText");
//    		p.addContent(annElement); }
//    	
//    	if (p.getChild("ParsedText").getChild("TextTraits")==null){
//    		p.getChild("ParsedText").addContent(makeDefaultTextTraits().getAsElement());}  	
//    	
//    	if (p.getChild("ChartData")==null){
//    		SElement cdElement = new SElement("ChartData");
//    		p.addContent(cdElement); }
//    	
//    	if (p.getChild("ChartData").getChild("Graph")==null){
//    		SElement gElement = new SElement("Graph");
//    		gElement.addContent(makeDefaultTextTraits().getAsElement());
//    		p.getChild("ChartData").addContent(gElement);}
//    	
////    	p.addContent(prefs);
//    }
	
	private void textSettingChanged()
	{
		if (fInitializingUI) return;
		extractCurrentTrait();
//		install();
	}

}
