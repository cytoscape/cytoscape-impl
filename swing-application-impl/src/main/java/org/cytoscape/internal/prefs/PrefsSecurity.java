package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.internal.prefs.lib.BoxComponent;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.RangedIntegerTextField;

public class PrefsSecurity extends AbstractPrefsPanel {

	protected PrefsSecurity(Cy3PreferencesRoot dlog) {
		super(dlog, "security");
//		namespace = "proxy";
	}
    @Override public void initUI()
    {
        super.initUI();
		setBorder(BorderFactory.createEmptyBorder(20,32,0,0));
		Box page = Box.createVerticalBox();
	    page.add(new HBox(true, true, new JLabel("Security")));
	    page.add(Box.createRigidArea(new Dimension(30,40)));

	    page.add(new HBox(true, true, makeProxyPanel()));
	    page.add(Box.createRigidArea(new Dimension(30,40)));
	  	page.add(makeCheckBoxLine("Check Version Info on Startup", "privacy.check.version", "tip"));
	  	page.add(makeCheckBoxLine("Report shutdown events", "privacy.report.quit", "Provide us information on your sesssion length."));
	  	page.add(makeCheckBoxLine("Send telemetry reports to Cytoscape Team", "privacy.telemetry", "tip"));
	  	page.add(makeCheckBoxLine("Provide contact info", "user.contact", "provide an email address where we can reach you"));
		add(page);   
	}
    
	String[] proxyTypes = { "direct", "http", "socks"};

    private Component makeProxyPanel() {
		Box page = Box.createVerticalBox();
		page.add(makeRepositoryPanel());
		return page;
	}
	private JTextField fHost, fUsername; // proxy settings
	private JPasswordField fPassword;
	private RangedIntegerTextField fPort;
	
	static String[] strs = new String[] {"Host", "Port", "User Name", "Password"};
	AntiAliasedPanel makeRepositoryPanel()
	{
		AntiAliasedPanel panel = new AntiAliasedPanel("Proxy Settings", false);
		setSizes(panel, new Dimension(580,120));
		fHost = new JTextField(25);
        fPort =  new  RangedIntegerTextField(0,9999, new Dimension(80,27));//  new JTextField(5);
        fUsername = new JTextField(15);
        fPassword = new JPasswordField(15);
        components.put("proxy.host", fHost);
        components.put("proxy.port", fPort);
        components.put("proxy.username", fUsername);
        components.put("proxy.password", fPassword);
        
        JLabel[] labels = new JLabel[4];
        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        for (int i=0; i<4; i++)
        {
        		labels[i] = new JLabel(strs[i]);	
    	        setSizes(labels[i], new Dimension(67, 27));
        		labels[i].setFont(labelFont);
        		labels[i].setHorizontalAlignment(SwingConstants.RIGHT);
        		labels[i].setOpaque(false);
        }
        Dimension SPACE = new Dimension(20,20);
        Box lin1 = Box.createHorizontalBox();
        Box lin2 = Box.createHorizontalBox();
        lin1.add(new HBox(true, true, Box.createRigidArea(SPACE), new BoxComponent(labels[0], fHost, new BoxComponent(labels[1], fPort))));		
        lin2.add(new HBox(true, true, new BoxComponent(labels[2], fUsername, new BoxComponent(labels[3], fPassword))));
        panel.add(lin1);
        panel.add(lin2);
		return panel;
	}
	

}
