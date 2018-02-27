package org.cytoscape.internal.prefs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.cytoscape.internal.prefs.lib.HBox;

public class PrefsBehavior extends AbstractPrefsPanel {
  	static Font dlogFont = new Font(Font.DIALOG, Font.PLAIN, 12);

	protected PrefsBehavior(Cy3PreferencesPanel dlog) {
		super(dlog, "cytoscape3");
	}
    @Override public void initUI()
    {
        super.initUI();
        
		Box page = Box.createVerticalBox();
		page.add(Box.createRigidArea(new Dimension(20, 40)));
		JLabel line0 = new JLabel("These parameters customize the interface and what information to display");
		page.add(new HBox(line0 ));
	    line0.setFont(ital11);
		page.add(Box.createRigidArea(new Dimension(20, 30)));
		for (int i=0; i< displayNames.length; i++)
		{
			String prompt =  displayNames[i];
			String shortened = prompt.replaceAll(" ", "");
			String fld  = shortened.substring(0, 1).toLowerCase() + shortened.substring(1);
			page.add(makeCheckBoxLine(prompt, fld,  tips[i]));
		}
//	    
//	    page.add(Box.createRigidArea(new Dimension(30,40)));
//	    page.add(new HBox(true, true, new JLabel("Privacy Settings")));
//	    page.add(Box.createRigidArea(new Dimension(30,20)));
//	  	page.add(makeCheckBoxLine("Check Version Info on Startup", "privacy.check.version", "Make a connection to our server to establish whether any updates have been posted."));
//	  	page.add(makeCheckBoxLine("Report shutdown events", "privacy.report.quit", "Provide us information on your sesssion length."));
//	  	page.add(makeCheckBoxLine("Send telemetry reports to Cytoscape Team", "privacy.telemetry", "Transfer logs to our server for statistic aggregation."));
//	  	page.add(makeCheckBoxLine("Provide contact info", "user.contact", "provide an email address where we can reach you"));
//	  	
//		HBox line = new HBox();
//		JCheckBox cb = (JCheckBox) components.get("user.contact");  //new JCheckBox("contact");
//		JTextField contact = new JTextField();
//   		cb.setToolTipText("By providing an email address, you'll receive notifications of free t-shirt availability.");
//    		cb.setFont(dlogFont);
//    		line.add(cb);
//    		cb.addActionListener(new ActionListener() {
//				@Override public void actionPerformed(ActionEvent e) {
//					contact.setEnabled(cb.isSelected());
//				} 	});
//    		line.add(contact);
//    		line.add(Box.createHorizontalGlue());
//		components.put("user.contact", cb);			//getName() + "." + 
//
//		setSizes(contact, 250, 27);
//		components.put("contact.info", contact);
////		line.add(new HBox(true, true, contact));
//		//TODO add contact info
//		//TODO add rest port
//	  	page.add(line);
//
	  	page.add(Box.createVerticalGlue());
		add(page);   
	}
	
	
	String[] displayNames = { "Show Network Provenance Hierarchy", "Show Node Edge Count",  "Show QuickStart As Startup",  
							"Canonicalize Names",  "Hide Welcome Screen",  "Maximize View On Create"  };
		
	String[] tips = { "Display the nested levels of networks in the status bar", "Includes the numbers of nodes and edges in the status bar",  "Display a panel of recent sessions and samples from our documentation",  
							"A canonical name includes ",  "Skip the view showing recent sessions",  "Use the largest window sizes possible by default"  };
 

}
