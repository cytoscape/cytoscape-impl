package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.cytoscape.internal.prefs.lib.HBox;

//line.add(Box.createHorizontalGlue()); 
//container.add(Box.createRigidArea(new Dimension(5,0)));

public class PrefsGroups extends AbstractPrefsPanel {

	protected PrefsGroups(Cy3PreferencesRoot dlog) {
		super(dlog, "groupSettings");
		// TODO Auto-generated constructor stub
	}
	
	String[] optionsArray = { "None", "Expand/Contract", "Select" };
	String[] showGroupArray = { "None", "Compound Node", "Show Group Node", "Single Node" };
    @Override public void initUI()
    {
        super.initUI();
		Box page = Box.createVerticalBox();
		page.setBorder(new EmptyBorder(12, 40, 0, 0));
	    page.add(createHeader());
	    
	    page.add(new HBox(makeLabeledCombo("Double-click action", "groupAction", optionsArray, 30, 180, 180), Box.createHorizontalGlue()));
	    page.add(new HBox(makeLabeledCombo("Visualization for group", "groupView", showGroupArray, 30, 180, 180), Box.createHorizontalGlue()));
	    page.add(new HBox(new JCheckBox("Show collapsed node as a Nested Network"), Box.createHorizontalGlue()));

	    page.add(makeAggregationPanel());
		add(page);   
	}
  
	private Component createHeader() {
	    Box header = Box.createVerticalBox();
	    JLabel jlab = new JLabel("Choose the method to aggregate group information" + 
	    					" either as a single instance or a list");
	    
	    jlab.setHorizontalAlignment(SwingConstants.LEFT);
	    Font font = new Font("SansSerif", Font.ITALIC, 11);
	    jlab.setFont(font);
	    header.add(new HBox(jlab, Box.createHorizontalGlue()));
	    header.add(Box.createRigidArea(new Dimension(5,20)));
		return header;
	}
    
    int WIDTH = 140;
    int HEIGHT = 26;
    Box aggregationPanel;
    List<JComboBox> aggregationCombos = new ArrayList<JComboBox>();
    private Box makeAggregationPanel() {
		
    	aggregationPanel = Box.createVerticalBox();
//    	aggregationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
	    JCheckBox ckbox = new JCheckBox("Enable attribute aggregation");
		ckbox.setToolTipText("");
		components.put("groupSettings" + "." + "enableAggregation", ckbox);
		ckbox.setSelected(true);
	    ckbox.addActionListener(e -> { enableAggregationPane(ckbox.isSelected()); });  
	    
	    aggregationPanel.add(new HBox(true, true, ckbox));
		
	    Box header = Box.createHorizontalBox();
		JLabel typeLabel = new JLabel("Type");
		typeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setSizes(typeLabel, WIDTH, HEIGHT);
		JLabel singLabel = new JLabel("Single Item");
		singLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setSizes(singLabel, WIDTH, HEIGHT);
		JLabel plurLabel = new JLabel("List");
		plurLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setSizes(plurLabel, WIDTH, HEIGHT);
		header.add(typeLabel);
		header.add(singLabel);
		header.add(plurLabel);
		aggregationPanel.add(header);
		
		
		for (String s : types)
		{
			Box line = Box.createHorizontalBox();
			JLabel typerLabel = new JLabel(s);
			setSizes(typeLabel, WIDTH, HEIGHT);
			typerLabel.setHorizontalAlignment(SwingConstants.LEFT);
			JComboBox singleOption = new JComboBox(aggsNum);
			setSizes(singleOption, WIDTH, HEIGHT);
			JComboBox pluralOption = new JComboBox(aggsListNum);
			setSizes(pluralOption, WIDTH, HEIGHT);
			line.add(typerLabel);
			setSizes(typerLabel, WIDTH, HEIGHT);
			line.add(singleOption);
			setSizes(singleOption, WIDTH, HEIGHT);
			line.add(pluralOption);
			setSizes(pluralOption, WIDTH, HEIGHT);
			aggregationPanel.add(line);
			aggregationCombos.add(singleOption);
			aggregationCombos.add(pluralOption);
		}
		aggregationPanel.add(Box.createRigidArea(new Dimension(12,12)));
		
		aggregationPanel.add(new HBox(makeLabeledCombo("Attribute to override", "attribute", availableAttributes), Box.createHorizontalGlue()));
		aggregationPanel.add(new HBox(makeLabeledCombo("Attribute type", "attributeType", attributeTypes), Box.createHorizontalGlue()));
		aggregationPanel.add(new HBox(makeLabeledCombo("Aggregation type", "aggregationType", aggregationTypes), Box.createHorizontalGlue()));

		
		return aggregationPanel;
	}
    
	private void enableAggregationPane(boolean selected) {
		aggregationPanel.setEnabled(selected);
		for (JComboBox combo : aggregationCombos)
			combo.setEnabled(selected);
		
		
	}

	String[] availableAttributes = { "None", "Name", "Weight", "isNumeric"};
	String[] attributeTypes = { "Integer", "Long", "Double", "String", "Boolean"};
	String[] aggregationTypes = { "None", "Summary", "Average", "Range", "Distribution"};

	String[] types = { "Integer", "Long", "Double", "String", "Boolean"};
	String[] aggsNum = { "None", "Mean", "Median", "Min", "Max", "Sum"};
	String[] aggsListNum = { "None", "Mean", "Median", "Min", "Max", "Sum", "Concatenate", "Unique Values"};
	String[] aggsBool = { "None", "Logical AND", "Logical OR"};
	String[] aggsStr = { "None", "Comma Separated Text", "Tab Separated Text", "Most Common Value", "Unique Values"};
	String[] aggsStrList = { "None", "Concatenate", "Unique Values"};


}
