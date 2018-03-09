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

public class PrefsPrivacy extends AbstractPrefsPanel {
  	static Font dlogFont = new Font(Font.DIALOG, Font.PLAIN, 12);

	protected PrefsPrivacy(Cy3PreferencesPanel dlog) {
		super(dlog, "cytoscape3", "Privacy", "\uf023", "Ability to provide or limit exposure of your identity to our log servers", -1);
	}
    @Override public void initUI()
    {
        super.initUI();
        
		Box page = Box.createVerticalBox();
	    
	    page.add(Box.createRigidArea(new Dimension(30,40)));
	    page.add(new HBox(true, true, new JLabel("Privacy Settings")));
	    page.add(Box.createRigidArea(new Dimension(30,20)));
	    JLabel line0 =new JLabel("We respect your right to privacy, but tracking information is critical to our process.");
	    JLabel line1 =new JLabel("By checking the boxes below, you will help us learn more about our users and the product.");
	    JLabel line2 = new JLabel("Our full privacy policy can be found <here>");
	    	
	    line0.setFont(ital11);
	    line1.setFont(ital11);
	    line2.setFont(ital11);
	    page.add(new HBox(line0));
	    page.add(new HBox(line1));
	    page.add(Box.createRigidArea(new Dimension(30,20)));

	  	page.add(makeCheckBoxLine("Check Version Info on Startup", "privacy.check.version", "Make a connection to our server to establish whether any updates have been posted."));
	  	page.add(makeCheckBoxLine("Report shutdown events", "privacy.report.quit", "Provide us information on your sesssion length."));
	  	page.add(makeCheckBoxLine("Send telemetry reports to Cytoscape Team", "privacy.telemetry", "Transfer logs to our server for statistic aggregation."));
	  	page.add(makeCheckBoxLine("Provide contact info", "user.contact", "provide an email address where we can reach you"));
	  	
		HBox line = new HBox();
		JCheckBox cb = (JCheckBox) components.get("user.contact");  //new JCheckBox("contact");
		JTextField contact = new JTextField();
   		cb.setToolTipText("By providing an email address, you'll receive notifications of free t-shirt availability.");
    		cb.setFont(dlogFont);
    		line.add(cb);
    		cb.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					contact.setEnabled(cb.isSelected());
				} 	});
    		line.add(contact);
    		line.add(Box.createHorizontalGlue());
		components.put("user.contact", cb);			//getName() + "." + 

		setSizes(contact, 250, 27);
		components.put("contact.info", contact);
//		line.add(new HBox(true, true, contact));
		//TODO add contact info
		//TODO add rest port
	  	page.add(line);

	  	page.add(Box.createVerticalGlue());
		add(page);   
	}
 
}
