package org.cytoscape.internal.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.internal.prefs.lib.FontAwesomeIcon;


/** The preferences for Cytoscape 3.7.
 *
 */

public class Cy3PreferencesRoot extends PreferenceContainer implements ActionListener
{
	private static final long serialVersionUID = 1L;
	static int NPREFS = 10;				
	static int FOOTER_HEIGHT = 10;
	public static double WINDOW_WIDTH = 680;
	public static double WINDOW_HEIGHT = WINDOW_WIDTH / 1.618;
	public static double ROW_HEIGHT = (WINDOW_HEIGHT - FOOTER_HEIGHT) / 3;  // TODO -- hard-coded assumption of 3rows

	//----------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lf);
			
			Cy3PreferencesRoot pref = new Cy3PreferencesRoot();
			pref.showDlog();
		} catch (Throwable e)	
		{ 	
			System.err.println("Error caught in main();");
			e.printStackTrace(); 
		}
		
	}
	// the set of properties read in from files all put into one map
	Map<String, String> propertyMap =  new HashMap<String, String>();
	public Map<String, String> getPropertyMap()	{ return propertyMap; }

	//----------------------------------------------------------------------------------------------------
	public Cy3PreferencesRoot() 
	{
		super("Preferences");
		try
		{
//			InputStream istream = Cy3PreferencesRoot.class.getResourceAsStream("lib/fontawesome-webfont.ttf");
//	        Font font = Font.createFont(Font.TRUETYPE_FONT, istream);
//	        font = font.deriveFont(Font.PLAIN, 24f);
			
	        initUI();

			Prefs prefsCopy = Prefs.getPrefs();
			if (prefsCopy instanceof Cy3PrefsAPI)
				Cy3PrefsAPI.readProperties();
			else { System.err.println("Prefs mismatch");  }
			
			Dimension buttonSize = new Dimension(100, 25);

			homePanel = new JPanel();
			homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.PAGE_AXIS));
//			homePanel.setBorder(BorderFactory.createLineBorder(Color.blue));
			contentsPanel.add(homePanel,  "home");
			for(int row = 0; row<PPanels.rowLengths.length; row++) 
			{
				int rowLength = PPanels.rowLengths[row];
				JButton[] buttons = new JButton[rowLength];
				JComponent[] components = getRowsComponents(row, rowLength);  			
				for (int j=0; j < rowLength; j++) 
				{
					int index = j;
					if (row > 0)  index += PPanels.rowLengths[0];
					if (row > 1)  index += PPanels.rowLengths[1];
					PPanels panel = PPanels.values()[index];
					char iconName = panel.getIcon().charAt(0);
					Icon icon = new FontAwesomeIcon(iconName, 18);
					String name = panel.getDisplayName();
					buttons[j] = new JButton(name);
					buttons[j].setSize(buttonSize);
					buttons[j].setIcon(icon);
					buttons[j].setToolTipText(panel.getTooltip());
					buttons[j].addActionListener(buttonListener);
					int wid = 140; 
					int hght = 120;   //  icon.getIconHeight() / 2;
					Dimension dim = new Dimension(wid, hght);
					AntiAliasedPanel.setSizes(buttons[j], dim);
					buttons[j].setHorizontalTextPosition(SwingConstants.CENTER);
					buttons[j].setVerticalTextPosition(SwingConstants.BOTTOM);
					components[j].setName(buttons[j].getText());
//					System.out.println("adding: " + buttons[j].getText() + " -> " + components[j]);
					contentsPanel.add(components[j], buttons[j].getText());
				}
				addButtonRow(buttons, components, PPanels.rowNames[row]);
			}
			contentsPanel.add(fPrefs[NPREFS-1], "Advanced");

		}
		catch (Exception e)		{	e.printStackTrace();	}
		
		install(Prefs.getPrefs());
	}
	//-------------------------------------------------------------------------
	private final AbstractPrefsPanel[] fPrefs = new AbstractPrefsPanel[NPREFS];

	// keep this in synch with NPREFS above and rowLengths array in PPanels.java
	private void initUI() {
														// row 1
			fPrefs[0] = new PrefsGroups(this);
			fPrefs[1] = new PrefsTables(this);
			fPrefs[2] = new PrefsStyles(this);

			fPrefs[3] = new PrefsBehavior(this);			// row 2
			fPrefs[4] = new PrefsEfficiency(this);
			fPrefs[5] = new PrefsSecurity(this);
	
			fPrefs[6] = new PrefsColors(this);		// row 3
			fPrefs[7] = new PrefsText(this);
			fPrefs[8] = new PrefsLinks(this);
						
			fPrefs[9] = new PrefsAdvanced(this);			// available thru Tabular button

			for (int i=0; i < NPREFS; i++)
				fPrefs[i].initUI();
		}
	// ----------------------------------------------------------------------------------------
	public JComponent[] getRowsComponents(int row, int rowLen)
		{
			JComponent[] components = new JComponent[rowLen];
			int start = 0;
			if (row > 0) 
					start += PPanels.rowLengths[0];
			if (row > 1) 
				start += PPanels.rowLengths[1];				// TODO assumes 3 rows

			for (int i=0; i < rowLen; i++)	
				components[i] = fPrefs[i + start];	
			int width = (int) WINDOW_WIDTH - 48;  
			int height = (int) WINDOW_HEIGHT;
			Dimension size = new Dimension(width, height);
			for (JComponent c : components)
				AntiAliasedPanel.setSizes(c, size);
			AntiAliasedPanel.setSizes(homePanel, size);
			return components;
		}
	// ---------------------------------------------------------------------------------------------------------
		Prefs prefsLocalCopy = new Cy3PrefsAPI();
		
		public Prefs getPrefs() { return prefsLocalCopy;	}
	// ---------------------------------------------------------------------------------------------------------
		public void install(Prefs inPrefs)    
		{
			if (inPrefs == null)
			 inPrefs = Prefs.getPrefs();
			for (int i=0; i < NPREFS; i++)
				fPrefs[i].install(inPrefs);
		}

		public void extract()
		{
			Map<String,String> props = new HashMap<String, String>();
			for (int i=0; i < NPREFS; i++)
			{
				Map<String,String> subprops = fPrefs[i].extract();
				props.putAll(subprops);
			}
			writePrefsToConfig(props);
		}

		// ---------------------------------------------------------------------------------------------------------
		private void writePrefsToConfig(Map<String,String> props) {			// TODO
			
			Set<Entry<String, String>> entries = props.entrySet();
			Map<String, String> sorted = new TreeMap<String, String>(); 
			for (Entry<String, String> e : entries )
				sorted.put(e.getKey(), e.getValue());
			
			for (Entry<String, String> e : sorted.entrySet() )
				System.out.println(e.getKey() + " ==>  " + e.getValue());
		
		}
	// ---------------------------------------------------------------------------------------------------------
	@Override public void savePrefs()	{	extract();	}
	
}