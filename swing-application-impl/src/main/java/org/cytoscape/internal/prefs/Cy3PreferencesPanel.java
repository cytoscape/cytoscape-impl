package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;


/** The preferences for Cytoscape 3.7.
 *
 */

public class Cy3PreferencesPanel extends PreferenceContainer implements ActionListener
{
	private static final long serialVersionUID = 1L;
	static int FOOTER_HEIGHT = 0;
	final IconManager iconManager;

	//----------------------------------------------------------------------------------------------------
//	public static void main(String[] args) {
//		try {
//			String lf = UIManager.getSystemLookAndFeelClassName();
//			UIManager.setLookAndFeel(lf);
//			Cy3PreferencesPanel pref = new Cy3PreferencesPanel(new JDialog(), propertyMap, null);
//	//		readPropertiesIfOffline();
//			pref.showDlog();
//		} catch (Throwable e)	
//		{ 	
//			System.err.println("Error caught in main();");
//			e.printStackTrace(); 
//		}
//		
//	}
	//----------------------------------------------------------------------------------------------------
	public Cy3PreferencesPanel(JDialog dlog, final Map<String, CyProperty<?>> inPropMap, final CyServiceRegistrar reg) 
	{
		super(dlog, reg);
		iconManager = reg.getService(IconManager.class);
		globalPropMap = inPropMap;
//		dumpGlobalProps("constructor");
		makeLocalCopy(inPropMap);
		AbstractPrefsPanel.setSizes(this, AbstractPrefsPanel.getPanelSize());
		try
		{
	        initUI();
	        JPanel homePanel = new JPanel();
			homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.PAGE_AXIS));
			contentsPanel.add(homePanel,  "home");
			for(int row = 0; row<rowLengths.length; row++) 
			{
				int rowLength = rowLengths[row];
				JButton[] buttons = new JButton[rowLength];
				AbstractPrefsPanel[] components = getRowsComponents(row, rowLength);  			
				for (int j=0; j < rowLength; j++) 
				{
					buttons[j] = makePanelButton(components[j]);
					contentsPanel.add(components[j], buttons[j].getText());
				}
				addButtonRow(buttons, rowNames[row], homePanel);
			}
			contentsPanel.add(fPrefs.get(fPrefs.size()-1), "Advanced");

		}
		catch (Exception e)		{	e.printStackTrace();	}
	}
	//-------------------------------------------------------------------------
	boolean advancedMode = false;
	PrefsAdvanced advancedPanel;
	private final List<AbstractPrefsPanel> fPrefs = new ArrayList<AbstractPrefsPanel>();
	
	public void add(AbstractPrefsPanel panel)
	{
		int gravity = panel.getGravity();
		int i = 0;
		for  (; i<fPrefs.size(); i++)
		{
			if (fPrefs.get(i).getGravity() > gravity)
				break;
		}
		fPrefs.add(i, panel);		
	}	
	
	//-------------------------------------------------------------------------
	private void initUI() {
		add(new PrefsBehavior(this));			
		add(new PrefsColor(this));		
		add(new PrefsText(this));

		add(new PrefsGroups(this));
		add(new PrefsTables(this));
		add(new PrefsLinks(this));

		add(new PrefsEfficiency(this));
		add(new PrefsNetwork(this));
		add(new PrefsPrivacy(this));
					
		add(advancedPanel = new PrefsAdvanced(this));			// available modally thru Tabular button

		for (AbstractPrefsPanel pref : fPrefs)
			pref.initUI();
	}
	
	public static int[] rowLengths = new int[] { 3, 3, 3 };
	public static String[] rowNames = new String[]{ "", "", ""};
	// ----------------------------------------------------------------------------------------
	private AbstractPrefsPanel[] getRowsComponents(int row, int rowLen)
		{
			AbstractPrefsPanel[] components = new AbstractPrefsPanel[rowLen];
			int start = 0;
			if (row > 0) 
				start += rowLengths[0];
			if (row > 1) 
				start += rowLengths[1];				// TODO assumes 3 rows

			for (int i=0; i < rowLen; i++)	
				components[i] = fPrefs.get(i + start);	
			int width = AbstractPrefsPanel.WINDOW_WIDTH;  
			int height = AbstractPrefsPanel.ROW_HEIGHT;
			Dimension size = new Dimension(width, height);
			for (JComponent c : components)
				AntiAliasedPanel.setSizes(c, size);
			return components;
		}
	
	private JButton makePanelButton(AbstractPrefsPanel panel) {
		String name = panel.getDisplayName();
		panel.setName(name);				// CardLayout relies on getName, so mirror it from displayName
		String iconName = panel.getIcon();
		Icon icon = new TextIcon(iconName, iconManager.getIconFont(24.0f), 32, 32);
		int wid = 160; 
		int hght = 100; 
		JButton btn = new JButton(name);
		btn.setSize(wid, hght);
		btn.setIcon(icon);
		btn.setToolTipText(panel.getTooltip());
		btn.addActionListener(buttonListener);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.BOTTOM);
		return btn;
	}
	// ---------------------------------------------------------------------------------------------------------
	public void install()    
	{
		dumpGlobalProps("install");
		if (advanced)
			advancedPanel.install(propertyMap);
		else
		for (AbstractPrefsPanel panel : fPrefs)
		{	
			String propName = panel.getPropFileName();
            Properties props = getProperties(propName);
			if (props != null)
				panel.install(props);
		}
	}

	
	public void extract()
	{
		dumpGlobalProps("extract pre");
		if (advancedMode)
			advancedPanel.extract(propertyMap);
		else
			for (AbstractPrefsPanel panel : fPrefs)
			{	
				String propName = panel.getPropFileName();
				Properties props = getProperties(propName);
				if (props != null)
					panel.extract(props);
			}
		dumpGlobalProps("extract post");
	}
	
	@Override public void savePrefs()
	{
		extract();
		saveStateToConfigDirectory();
	}
	// ---------------------------------------------------------------------------------------------------------
	protected void resetAllPanels() {
		for (AbstractPrefsPanel pref : fPrefs)
			pref.reset();
	}

	protected void resetCurrentPanel() {
		for (Component comp : contentsPanel.getComponents()) {
			if (comp.isVisible() == true) {
				AbstractPrefsPanel pref = (AbstractPrefsPanel) comp;
				pref.reset();
		    }
		}
	}
	//----------------------------------------------------------------------------------------------------
	// The set of properties read in from several files all put into one map.
	// This is kept separate from the <CyProperty> map that lives in PreferenceAction
	// so you can cancel out of the dialog with no changes taking effect.
	
	private Map<String, Properties> propertyMap;		
	public  Map<String, Properties> getPropertyMap()	{ return propertyMap; }
	private Properties getProperties(String propName) {	return propertyMap.get(propName);	}

	 // a reference to the properties used by the rest of the program
	Map<String, CyProperty<?>> globalPropMap;  
	
	private void makeLocalCopy(Map<String, CyProperty<?>> original)
	{
		propertyMap = new HashMap<String, Properties>();
		for (String s : original.keySet())
		{
			if (s.startsWith("vizmap")) continue;
			if (s.startsWith("layout")) continue;
			CyProperty<?> thing = original.get(s);
			Object reader = thing.getProperties();
			if (reader instanceof Properties)
			{
			  Properties properties = (Properties) reader;
			  if (properties != null)
			  {
				  Properties clone = new Properties();
				  for (Object obj : properties.keySet())
					  clone.put(obj, properties.get(obj)); 
				  propertyMap.put(s, clone);
			  }
			}
		}
	}
		
	private void copyLocalToGlobalProperties()
	{
		for (String s : globalPropMap.keySet())
		{
			CyProperty<?> orig = globalPropMap.get(s);
			if (orig == null) continue;
			try
			{
				Properties properties = (Properties) orig.getProperties();
				if (properties != null)
				{
					System.out.println("global props: + " + s + " " + properties);
					Properties local = propertyMap.get(s);
					if (local == null) continue;
					for (Object key : local.keySet())
					{
						Object locObj = local.get(key);
						if (locObj != null)
								properties.put(key, locObj);
					}
				}
			}
			catch (ClassCastException ex)
			{
				System.err.println("ClasscastException in copyLocalToGlobalProperties");
			}
		}
	}
	// ---------------------------------------------------------------------------------------------------------
		
	public void saveStateToConfigDirectory()
	{
		copyLocalToGlobalProperties();
		Map<String,Properties> prefs = propertyMap;
		System.out.println("saveStateToConfigDirectory");
		for (String key : prefs.keySet()) {
			if (key.startsWith("layout")) continue;
			if (key.startsWith("vizmap")) continue;
			final String propertyName = key;
			if (propertyName == null || propertyName.isEmpty()) continue;
			
			String propertyFileName = propertyName;
			if (!propertyFileName.endsWith(".props"))
				propertyFileName += ".props";

			final CyApplicationConfiguration config = serviceRegistrar.getService(CyApplicationConfiguration.class);
			final File outputFile = new File(config.getConfigurationDirectoryLocation(), propertyFileName);
			Properties properties = prefs.get(key);
				try {
					FileOutputStream out = new FileOutputStream(outputFile);
					properties.store(out, null);
					out.close();
				} catch (Exception e) {
					System.err.println("Error in writing properties file.");
				}
			}
		}
	// ---------------------------------------------------------------------------------------------------------
	//DEBUG
	
	private void dumpGlobalProps(String stat)
	{
		for (String s : globalPropMap.keySet())
		{
			if (s.startsWith("layout")) continue;
			if (s.startsWith("viz")) continue;
			if (s.startsWith("group")) continue;
			if (s.startsWith("commandline")) continue;
			if (s.startsWith("link")) continue;
			System.out.println(stat + "\n:-----------" + s);
			CyProperty<?> orig = globalPropMap.get(s);
			Object prop = orig.getProperties();
			if (prop instanceof Properties)
				  dumpProperties( (Properties) prop);
		}
	}
	
	void dumpProperties(Properties p)			// in alphabetical order
	{
	     Enumeration<Object> keysEnum = p.keys();
	     Vector<String> keyList = new Vector<String>();
	     while(keysEnum.hasMoreElements())
	       keyList.add((String)keysEnum.nextElement());
	     
	     Collections.sort(keyList);
	     for (String key : keyList)
			  System.out.println(key + ": " + p.getProperty(key));
	     System.out.println();
	}

}
