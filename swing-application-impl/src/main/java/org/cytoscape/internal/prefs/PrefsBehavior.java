package org.cytoscape.internal.prefs;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class PrefsBehavior extends AbstractPrefsPanel {

	protected PrefsBehavior(Cy3PreferencesRoot dlog) {
		super(dlog, "cytoscape3");
	}
    @Override public void initUI()
    {
        super.initUI();
        
		Box page = Box.createVerticalBox();
		page.add(Box.createRigidArea(new Dimension(20, 40)));
		for (int i=0; i< displayNames.length; i++)
		{
			String prompt =  displayNames[i];
			String shortened = prompt.replaceAll(" ", "");
			String fld  = shortened.substring(0, 1).toLowerCase() + shortened.substring(1);
			page.add(makeCheckBoxLine(prompt, fld,  tips[i]));
		}
		page.add(Box.createVerticalGlue());
		add(page);   
	}
	
	
	String[] displayNames = { "Show Network Provenance Hierarchy", "Show Node Edge Count",  "Show QuickStart As Startup",  
							"Canonicalize Names",  "Hide Welcome Screen",  "Maximize View On Create"  };
		
	String[] tips = { "Show Network Provenance Hierarchy", "Show Node Edge Count",  "Show QuickStart As Startup",  
							"Canonicalize Names",  "Hide Welcome Screen",  "Maximize View On Create"  };
	   @Override public void install(Map<String, String> props)
	    {
//		   Map<String, String> map = getPropertyMap("cytoscape3.props");
		   for (String fld : displayNames)
		   {
			  String shortened = fld.replaceAll(" ", "");
			  String squished = shortened.substring(0, 1).toLowerCase() + shortened.substring(1);
			  String key = "cytoscape3." + squished;
			  String value = props.get(key);
			  if (value != null)
			  {
			   JComponent comp = components.get(key);
			   if (comp == null) continue;	
			   if (comp instanceof JCheckBox)
			   {
				   JCheckBox ck = (JCheckBox) comp;
				   boolean checked = value.toLowerCase().startsWith("t");
				   ck.setSelected(checked);
			   }
			   if (comp instanceof JTextField)
				   ((JTextField) comp).setText(value);
			  }
		   }
	    }
	   
	   @Override public Map<String,String> extract()
	    {
		   Map<String,String> attributes = new HashMap<String,String>();
		   for (String fld : displayNames)
		   {
			   String shortened = fld.replaceAll(" ", "");
			   String squished = shortened.substring(0, 1).toLowerCase() + shortened.substring(1);
				JComponent comp = components.get("cytoscape3." + squished);
			   if (comp == null) continue;	
			   if (comp instanceof JCheckBox)
			   {
				   JCheckBox ck = (JCheckBox) comp;
				   attributes.put(squished, boolState(ck));
			   }
		   }
		   overwriteProperties("cytoscape3.props", attributes);
		   return attributes;
	    }	 
}
