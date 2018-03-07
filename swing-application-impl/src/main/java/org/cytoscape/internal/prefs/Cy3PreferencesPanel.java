package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
	static int NPREFS = PPanels.values().length;		
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
		makeLocalCopy(inPropMap);
		AbstractPrefsPanel.setSizes(this, AbstractPrefsPanel.getPanelSize());
		try
		{
			// InputStream istream = Cy3PreferencesPanel.class.getResourceAsStream("lib/fontawesome-webfont.ttf");
	    //     Font font = Font.createFont(Font.TRUETYPE_FONT, istream);
	    //     font = font.deriveFont(Font.PLAIN, 24f);
			
	        initUI();
	        JPanel homePanel = new JPanel();
			homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.PAGE_AXIS));
			contentsPanel.add(homePanel,  "home");
			for(int row = 0; row<PPanels.rowLengths.length; row++) 
			{
				int rowLength = PPanels.rowLengths[row];
				JButton[] buttons = new JButton[rowLength];
				AbstractPrefsPanel[] components = getRowsComponents(row, rowLength);  			
				for (int j=0; j < rowLength; j++) 
				{
					int index = j;
					if (row > 0)  index += PPanels.rowLengths[0];
					if (row > 1)  index += PPanels.rowLengths[1];
					PPanels panel = PPanels.values()[index];
					buttons[j] = makePanelButton(components[j]);
					components[j].setName(buttons[j].getText());
					contentsPanel.add(components[j], buttons[j].getText());
				}
				addButtonRow(buttons, PPanels.rowNames[row], homePanel);
			}
			contentsPanel.add(fPrefs[NPREFS-1], "Advanced");

		}
		catch (Exception e)		{	e.printStackTrace();	}
	}
	//-------------------------------------------------------------------------
	boolean advancedMode = false;
	PrefsAdvanced advancedPanel;
	private final AbstractPrefsPanel[] fPrefs = new AbstractPrefsPanel[NPREFS];

	// order of the prefs layout is determined by PPanels enum
	// keep this in synch with PPanels.java
	private void initUI() {
			int i = 0;									
			fPrefs[i++] = new PrefsBehavior(this);			
			fPrefs[i++] = new PrefsColor(this);		
			fPrefs[i++] = new PrefsText(this);

			fPrefs[i++] = new PrefsGroups(this);
			fPrefs[i++] = new PrefsTables(this);
			fPrefs[i++] = new PrefsLinks(this);

			fPrefs[i++] = new PrefsEfficiency(this);
			fPrefs[i++] = new PrefsNetwork(this);
			fPrefs[i++] = new PrefsPrivacy(this);
	
						
			fPrefs[i++] = advancedPanel = new PrefsAdvanced(this);			// available modally thru Tabular button

			for (i=0; i < NPREFS; i++)
				fPrefs[i].initUI();
		}
	
	// ----------------------------------------------------------------------------------------
	private AbstractPrefsPanel[] getRowsComponents(int row, int rowLen)
		{
			AbstractPrefsPanel[] components = new AbstractPrefsPanel[rowLen];
			int start = 0;
			if (row > 0) 
					start += PPanels.rowLengths[0];
			if (row > 1) 
				start += PPanels.rowLengths[1];				// TODO assumes 3 rows

			for (int i=0; i < rowLen; i++)	
				components[i] = fPrefs[i + start];	
			int width = AbstractPrefsPanel.WINDOW_WIDTH;  
			int height = AbstractPrefsPanel.ROW_HEIGHT;
			Dimension size = new Dimension(width, height);
			for (JComponent c : components)
				AntiAliasedPanel.setSizes(c, size);
			return components;
		}
	
	private JButton makePanelButton(AbstractPrefsPanel panel) {
		String iconName = panel.getIcon();
		Icon icon = new TextIcon(iconName, iconManager.getIconFont(24.0f), 32, 32);
		String name = panel.getDisplayName();
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


		private Properties getProperties(String propName) {	return propertyMap.get(propName);	}
		
		public void extract()
		{
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
		}
		
		@Override public void savePrefs()
		{
			extract();
			saveStateToConfigDirectory();
		}
		// ---------------------------------------------------------------------------------------------------------
		protected void resetAllPanels() {
			for (int i=0; i < NPREFS; i++)
				fPrefs[i].reset();
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
				 // AbstractConfigDirPropsReader propsReader = (AbstractConfigDirPropsReader) thing;
				  Properties properties = (Properties) reader;  //propsReader.getProperties();
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
				Object reader = orig.getProperties();
				if (reader instanceof AbstractConfigDirPropsReader)
				{
				  AbstractConfigDirPropsReader propsReader = (AbstractConfigDirPropsReader) orig;
				  Properties properties = propsReader.getProperties();
				  if (properties != null)
				  {
					  Properties local =  propertyMap.get(s);
					  for (Object key : local.keySet())
						  properties.put(key, local.get(s));

				  }
				}
			}
		}
	// ---------------------------------------------------------------------------------------------------------
	// prefs.groupSettings doesn't reflect changes!!
		
	public void saveStateToConfigDirectory()
	{
		copyLocalToGlobalProperties();
		Map<String,Properties> prefs = propertyMap;
		System.out.println("saveStateToConfigDirectory");
		for (String key : prefs.keySet()) {
			if (key.startsWith("layout")) continue;
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
}
