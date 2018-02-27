package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.internal.prefs.lib.BoxComponent;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.RangedIntegerTextField;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

public class PrefsNetwork extends AbstractPrefsPanel {

	protected PrefsNetwork(Cy3PreferencesPanel dlog) {
		super(dlog, "network");
//		namespace = "proxy";
	}
    @Override public void initUI()
    {
        super.initUI();
		setBorder(BorderFactory.createEmptyBorder(20,32,0,0));
		Box page = Box.createVerticalBox();
//	    page.add(new HBox(true, true, new JLabel("Network Configurations")));
//	    page.add(Box.createRigidArea(new Dimension(30,20)));
	    JLabel line0 =new JLabel("Your network administrator may require the use of a proxy server");
	    JLabel line1 =new JLabel("to provide anonymity and security for your access to the Internet.");
	    JLabel line2 = new JLabel("If so, enter your server and account information here.");
	    		 line0.setFont(ital11);
	    line1.setFont(ital11);
	    line2.setFont(ital11);
	    page.add(new HBox(line0));
	    page.add(new HBox(line1));
	    page.add(Box.createRigidArea(new Dimension(30,20)));
	    page.add(new HBox(line2));
	    page.add(Box.createRigidArea(new Dimension(30,20)));
		Box proxy = Box.createVerticalBox();
		proxy.add(makeProxyPanel());
		page.add(new HBox(true, true, proxy));
	    page.add(Box.createRigidArea(new Dimension(30,40)));
	    JLabel line3 = new JLabel("This field determines the Internet port monitored by CyREST service.");
		line3.setFont(ital11);
		page.add(new HBox(line3));
	    page.add(Box.createRigidArea(new Dimension(30,20)));
	    page.add(makeNumberFieldShort("CyREST port", 80, "rest.port", 1234, 1025, 9999));

		add(page);   
	}
    
	   //--------------------------------------------------------------------------
	String[] proxyTypes = { "direct", "http", "socks"};
	String[] displayNames = { "proxy.host", "proxy.port",  "proxy.username",  "proxy.password", 
			"privacy.check.version", "privacy.report.quit", "privacy.telemetry", "user.contact" };

  	private JTextField fHost, fUsername; // proxy settings
	private JPasswordField fPassword;
	private JComboBox<String> fType;
	private RangedIntegerTextField fPort; 
	
	static String[] strs = new String[] {"Host", "Port", "User Name", "Password", "Type"};
	private AntiAliasedPanel makeProxyPanel()
	{
		AntiAliasedPanel panel = new AntiAliasedPanel("Proxy Settings");
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		setSizes(panel, new Dimension(580,120));
		fHost = new JTextField(25);
        fPort =  new  RangedIntegerTextField(0,9999, new Dimension(80,27));//  new JTextField(5);
        fUsername = new JTextField(15);
        fPassword = new JPasswordField(15);
        fType = new JComboBox<String>(proxyTypes);
        setSizes(fType, new Dimension(100, 27));
        components.put("proxy.host", fHost);
        components.put("proxy.port", fPort);
        components.put("proxy.username", fUsername);
        components.put("proxy.password", fPassword);
        components.put("proxy.server.type", fType);
       
        JLabel[] labels = new JLabel[5];
        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        for (int i=0; i<5; i++)
        {
        		labels[i] = new JLabel(strs[i]);	
    	        setSizes(labels[i], new Dimension(67, 27));
        		labels[i].setFont(labelFont);
        		labels[i].setHorizontalAlignment(SwingConstants.RIGHT);
        		labels[i].setOpaque(false);
        }
        Dimension SPACE = new Dimension(20,20);
        HBox lin0 = new HBox(true, false, labels[4], fType, Box.createHorizontalGlue());
        HBox lin1 = new HBox(true, true, Box.createRigidArea(SPACE), labels[0], fHost,labels[1], fPort);		
        HBox lin2 = new HBox(true, true,  labels[2], fUsername,  labels[3], fPassword);
        panel.add(new HBox(lin0));
        panel.add(new HBox(lin1));
        panel.add(new HBox(lin2));
		return panel;
	}
	
}
