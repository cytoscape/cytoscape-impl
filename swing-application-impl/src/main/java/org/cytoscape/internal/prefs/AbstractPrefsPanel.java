package org.cytoscape.internal.prefs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.internal.prefs.lib.ColorPane;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

abstract public class AbstractPrefsPanel extends AntiAliasedPanel
{
	private static final long serialVersionUID = 1L;
	protected Cy3PreferencesPanel root;
	final private static Dimension PREFS_PANEL_SIZE = new Dimension(615, 420);
	final public static Dimension getPanelSize() { return new Dimension(PREFS_PANEL_SIZE); }
	final public static int WINDOW_WIDTH = PREFS_PANEL_SIZE.width;
//	final public static int WINDOW_HEIGHT = PREFS_PANEL_SIZE.height;
	final static int FOOTER_HEIGHT = 50;
	final public static int ROW_HEIGHT = 140; // (WINDOW_HEIGHT - FOOTER_HEIGHT) / 3;  // TODO -- hard-coded assumption of 3rows
	final static public Font ital11 = new Font("SansSerif", Font.ITALIC, 11);
//	protected String namespace;
	
	protected AbstractPrefsPanel(Cy3PreferencesPanel container, String inStr)
	{
		super();
		root = container;
		setName(inStr); 
//		setBorder(BorderFactory.createLineBorder(Color.red));
	}

	public void initUI()	{	setSizes(this, PREFS_PANEL_SIZE); }
	public void adjust()	{ }		//  set buttons or text state

	//---------------------------------------------------------------------------------------------
	
	public Map<String, Properties> getPropertyMap()	{ return root.getPropertyMap();	}
    Dimension leading = new Dimension(12,12);
	//---------------------------------------------------------------------------------------------
	public HBox makeLabeledField(String s, String propertyName, String deflt, String tooltip)

	{
		JLabel label = new JLabel(s);
		JLabel spacer = new JLabel("    " );
		JTextField field = new JTextField(deflt);
		label.setToolTipText(tooltip);
		field.setToolTipText(tooltip);
		HBox line = new HBox();
		components.put(propertyName, field);
		line.add(label, spacer, field);
		setSizes(field, 180,30);
        return line;
	}
	//---------------------------------------------------------------------------------------------
	public  HBox makeNumberSliderRow(String s, String propName, int min, int max, int val) {
		
		JLabel label = new JLabel(s);
		setSizes(label, 150, 30);
//		label.setBorder(BorderFactory.createLineBorder(Color.green));
		JTextField fld = new JTextField("" + val);
		setSizes(fld, 80,30);
        JSlider slider = new JSlider(JSlider.HORIZONTAL,min, max, val);
        slider.addChangeListener(e -> { fld.setText("" + slider.getValue());} );
        slider.setMajorTickSpacing(max - min);
        slider.setPaintLabels(true);
        slider.setMaximumSize(new Dimension(200, 36));
        fld.setHorizontalAlignment(SwingConstants.RIGHT);
		components.put(propName, slider);		//getName() + "." + 
		Component spacer = Box.createHorizontalStrut(40);
		HBox line = new HBox(spacer, label, fld, slider);
 		return line;
	}
	//---------------------------------------------------------------------------------------------
	public JSlider makeSlider(String string) {
		JSlider slider = new JSlider(JSlider.VERTICAL,0, 50, 25);
		return slider;
	}
//	//---------------------------------------------------------------------------------------------
	  public static int getInteger(String inString, int deflt)
	  {
	      int result = deflt;
	      if (inString != null)
	      {
	          inString = inString.trim();
	          try
	          {
	              if (inString.length() > 0)
	                  result = Integer.parseInt(inString);
	          }
	          catch (NumberFormatException ex)            {}
	      }
	      return result;
	  }
		protected String boolState(JCheckBox ck )
		   {
			   return ck.isSelected() ? "true" : "false";
		   }

		//---------------------------------------------------------------------------------------------
	public AbstractBorder makeSubpanelBorder(String name)
	{
		TitledBorder border = new TitledBorder(name);
		border.setTitleColor(Color.black);
		border.setTitleFont(new Font(Font.DIALOG, 0, 14));
		return border;
	}
	
	//---------------------------------
	//added to play with prefs antialiaspanel border testing
	protected AbstractBorder makeSubpanelBorder2(String name)
	{
		TitledBorder border = new TitledBorder(name);
		border.setTitleColor(Color.blue);
		border.setTitleFont(new Font("Dialog", Font.BOLD, 10));
		return border;
	}
	
	//---------------------------------
	protected JTextField numberField(int value)
	{
    	JTextField fld = new JTextField("" + value);
    	setSizes(fld, 50,30 );
    	fld.setHorizontalAlignment(SwingConstants.RIGHT);
    	return fld;
	}
	
	//---------------------------------
	protected Box makeNumberField(String prompt, String propname, int value, int min, int max)
    {
	    	Box line = Box.createHorizontalBox();
	    	JLabel label = new JLabel(prompt);
	    	line.add(label);
	    	setSizes(label, 180,25);
	
	    	JTextField fld = new JTextField("" + value);
	    	fld.setMaximumSize(new Dimension(80,30) );
	    	line.add(fld);
	    	fld.setHorizontalAlignment(SwingConstants.RIGHT);
  		line.setMaximumSize(new Dimension(500,25));
		components.put(propname, fld);
  		return line;
     }
	//---------------------------------
	protected HBox makeNumberFieldShort(String prompt, int promptLen, String property, int value, int min, int max)
    {
		JLabel label = new JLabel(prompt);
		setSizes(label, promptLen, 25);
    		JTextField fld = new JTextField("" + value);
    		setSizes(fld, 60, 30);
    		fld.setFont(smallFont);
    		fld.setHorizontalAlignment(SwingConstants.RIGHT);
		components.put(property, fld);
		HBox line = new HBox(false, true, label, fld);
  		return line;
      }

	//---------------------------------
	protected Component makeLabeledPassworldField(String prompt, String fld) 
	{
  		Box line = Box.createHorizontalBox();
  		JLabel lab = new JLabel(prompt);
  		setSizes(lab, 150, 30);
  		line.add(lab);
  		JPasswordField choices = new JPasswordField();
  		line.add(choices);
  		line.setMaximumSize(new Dimension(500,30));
  		return line;
  	}
	//---------------------------------
	public static boolean isEmpty(String s)	{		return (s == null) || s.isEmpty();	}
	protected void setSizes(JComponent panel, int w, int h)
    {
    	Dimension d = new Dimension(w, h);
    	panel.setPreferredSize(d);
    	panel.setMaximumSize(d);
    	panel.setMinimumSize(d);
    	panel.setSize(d);
    }
	//---------------------------------
	protected Component makeLabeledCombo(String prompt, String propName, String[] options) {
			return makeLabeledCombo( prompt, propName, options, 30, 150, 130);
	}
	//---------------------------------
	protected Component makeLabeledCombo(String prompt, String propName, String[] options, int lineHeight, int labelWidth, int comboWidth)
	{
		Box line = Box.createHorizontalBox();
		JLabel lab = new JLabel(prompt);
		setSizes(lab, labelWidth, lineHeight);
		line.add(lab);
		JComboBox<String> choices = new JComboBox<String>(options);
		choices.setName(propName);
		line.add(choices);
		setSizes(choices, comboWidth, lineHeight);
		choices.setSelectedIndex(0);
		components.put(propName, choices);			//getName()+"." + 
		return line;
	}

	//---------------------------------
	protected HBox prepartitionOption(String namespace)
    {
    	return makeCheckBoxLine("Partition graph before layout", "singlePartition", "tip");
    }

	//---------------------------------------------------------------------------------------------------------
	protected HBox makeCheckBoxLine(String name, String propName, Font f, String tip)
    {
		HBox box = new HBox();
		JCheckBox cb = new JCheckBox(name);
    		cb.setToolTipText(tip);
    		cb.setFont(f);
    		box.add(cb);
		box.add(Box.createHorizontalGlue());
		components.put(propName, cb);			//getName() + "." + 
		return box;
    }
	//---------------------------------------------------------------------------------------------------------
	protected HBox makeCheckBoxLine(String name, String propName, String tip)
	{
		return makeCheckBoxLine(name, propName,  new Font(Font.DIALOG, Font.PLAIN, 12), tip);
	}

	//------------------------------------------------------------------
	// These were made for Layouts and aren't used anymore without PrefsLayouts
	
	static Font smallFont = new Font("Dialog", Font.BOLD, 10);
	    
	protected HBox makeSpacingBox() {
		Box page = Box.createHorizontalBox();
		HBox line = new HBox();
		JLabel hLabel = new JLabel("Horizontal");
		JLabel vLabel = new JLabel("Vertical");
		hLabel.setFont(smallFont);
		vLabel.setFont(smallFont);

		line.add(new JLabel("Spacing"), Box.createRigidArea(new Dimension(60, 5)), hLabel,
				makeIntegerBox(getName() + "." + "nodeVerticalSpacing"), vLabel,
				makeIntegerBox(getName() + "." + "nodeVerticalSpacing"), Box.createRigidArea(new Dimension(45, 5)),
				Box.createHorizontalGlue());
		// page.add(line);
		// page.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		// setSizes(page, 300, 36);
		return line;

	}

	protected HBox makeMarginBox()
	    {
			HBox line = new HBox();
			JLabel topLabel = new JLabel("Top");
			JLabel leftLabel = new JLabel("Left");
			JLabel rightLabel = new JLabel("Right");
			topLabel.setFont(smallFont);
			leftLabel.setFont(smallFont);
			rightLabel.setFont(smallFont);
			line.add(new JLabel("Margin"), Box.createRigidArea(new Dimension(60, 5)), topLabel, 
					makeIntegerBox(getName() + "." + "topEdge"), Box.createRigidArea(new Dimension(10, 5)), leftLabel, 
					makeIntegerBox(getName() + "." + "leftEdge"), Box.createRigidArea(new Dimension(10, 5)), rightLabel, 
					makeIntegerBox(getName() + "." + "rightMargin"), Box.createHorizontalGlue());
		  	
			return line;
	    }
	    
		protected JTextField makeIntegerBox(String propertyName) {
			JTextField fld = new JTextField();
			fld.setHorizontalAlignment(SwingConstants.RIGHT);
			setSizes(fld, 50, 27);
			components.put(propertyName, fld);
			return fld;
		}

//------------------------------------------------------------------
// Semantic Meat
// Map from key to displayName, to the component
//------------------------------------------------------------------
	protected Map<String, JComponent> components = new HashMap<String, JComponent>();
//	protected Map<String, String> displayNames = new HashMap<String, String>();
	boolean verbose = true;
	//------------------------------------------------------------------

    protected void dump(Properties properties, String filter) {
		if (verbose)
		for (Object key : properties.keySet())
		{
			if (key.toString().startsWith(filter))
				System.out.println(key + " : " + properties.getProperty(key.toString()));
		}
		
	}
	public void install(Properties properties)
	{		
	    	for (String name : components.keySet())
	    	{
	    		String property = (String) properties.get(name);
	    		JComponent component = components.get(name);
	    		if (property != null && component != null)
	    			inject(property, component);
	    	}
	}

	   
	protected String getPropFileName()	{ return "cytoscape 3";	}
    public void extract(Properties properties)
    {
	   System.out.println("--  " + getName());
	   for (String fld : components.keySet())
	   {
			JComponent comp = components.get(fld);
		   if (comp == null) continue;	
		   String val = scrape(comp);
			properties.put(fld, val);
		  if (!getName().equals("linkouts")) 
			  System.out.println(fld + " : " + val);
	    }
    }
	
	public void reset() {
		
		   for (String fld : components.keySet())
		   {
				JComponent comp = components.get(fld);
				if (comp != null)
					reset(comp);
		   }
	}
	   
	private void reset(JComponent comp) {
		// TODO store factory defaults with components when they are created
		
	}
//------------------------------------------------------------------
// For each type of control we have used,
//	here we push a value into the control (inject).
//	and pull the value out (scrape)

	protected void inject(String value, JComponent control) {
		if (control instanceof JCheckBox) {
			JCheckBox ck = (JCheckBox) control;
			boolean val = value.toLowerCase().startsWith("t");
			ck.setSelected(val);
		}
		if (control instanceof JTextField) {
			JTextField fld = (JTextField) control;
			fld.setText(value);
		}
		if (control instanceof JComboBox) {
			JComboBox s = (JComboBox) control;
			s.setSelectedItem(value);
		}

		if (control instanceof ColorPane) {
			ColorPane s = (ColorPane) control;
			try {
			s.setColor(new Color(Integer.parseInt(value)));
			} catch (NumberFormatException e) {
			}
			}

		if (control instanceof JSlider) {
			JSlider s = (JSlider) control;
			try {
				int i = Integer.parseInt(value);
				s.setValue(i);
			} catch (NumberFormatException e) {
			}
		}
	}
	
	protected String scrape(JComponent control) {
		if (control instanceof JCheckBox) {
			JCheckBox ck = (JCheckBox) control;
			return boolState(ck); 
		}
		if (control instanceof JTextField) {
			JTextField fld = (JTextField) control;
			return fld.getText();
		}
		if (control instanceof JSlider) {
			JSlider s = (JSlider) control;
			return "" + s.getValue();
		}
		if (control instanceof JComboBox) {
			JComboBox<?> s = (JComboBox<?>) control;
			return "" + s.getSelectedItem();
		}
		if (control instanceof ColorPane) {
			ColorPane s = (ColorPane) control;
			return "" + s.getColor().getRGB();
		}
		return "";
	}

};
