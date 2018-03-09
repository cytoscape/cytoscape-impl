package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

public class PrefsEfficiency extends AbstractPrefsPanel {

	protected PrefsEfficiency(Cy3PreferencesPanel dlog) {
		super(dlog,  "cytoscape3", "Efficiency", "\uf085", "Threshold settings to improve performance with large networks", -1);
		setBorder(BorderFactory.createEmptyBorder(20,32,0,0));

	}
	String[] displayNames = { "Coarse Detail", "Edge Arrow",  "Node Border",  "Node Label", "View Threshold", "Undo Depth" };
	
	private JSlider coarseDetail;
	private JSlider edgeArrow;
	private JSlider nodeBorder;
	private JSlider nodeLabel;
	private JSlider nodeView;
	private JSlider undoLimit;
	
	static String THRESHOLD_INTRO = "These threshold levels set the number of elements in a graph,";
	static String THRESHOLD_INTRO2 = "at which point drawing operations are suppressed.";

	static String UNDO_INTRO = "This sets the number of steps that can be reversed with Undo.";
	static String UNDO_INTRO2 = "Conserve memory by limiting the number of tasks to save.";
    @Override public void initUI()
    {
        super.initUI();
		Box col1 = Box.createVerticalBox();
		JLabel line1 = new JLabel(THRESHOLD_INTRO);		line1.setFont(ital11);
		JLabel line2 = new JLabel(THRESHOLD_INTRO2);		line2.setFont(ital11);
		JLabel undo1 = new JLabel(UNDO_INTRO);			undo1.setFont(ital11);
		JLabel undo2 = new JLabel(UNDO_INTRO2);			undo2.setFont(ital11);
		HBox firstLine = new HBox(line1);		
		HBox secondLine = new HBox(line2);
		col1.add(firstLine);
		col1.add(secondLine);
		col1.add(Box.createRigidArea(new Dimension(20,14)));
		
		coarseDetail = makeSlider("Coarse Detail", "render.coarseDetailThreshold", 500, 50000, 1000);
	    col1.add(makeNumberSliderRow("Coarse Detail", "render.coarseDetailThreshold", coarseDetail));

	    edgeArrow = makeSlider("Edge Arrow",  "render.edgeArrowThreshold", 500, 100000, 1000);
	    col1.add(makeNumberSliderRow("Edge Arrow",  "render.edgeArrowThreshold", edgeArrow));

	    nodeBorder = makeSlider("Node Border",  "render.nodeBorderThreshold", 1000, 75000, 5000);
	    col1.add(makeNumberSliderRow("Node Border",  "render.nodeBorderThreshold", nodeBorder));

	    nodeLabel = makeSlider("Node Label",  "render.nodeLabelThreshold", 1000, 85000, 5000);
	    col1.add(makeNumberSliderRow("Node Label",  "render.nodeLabelThreshold", nodeLabel));

	    nodeView = makeSlider("View Threshold", "viewThreshold",  1000, 150000, 10000);
	    col1.add(makeNumberSliderRow("View Threshold", "viewThreshold", nodeView));

		col1.add(Box.createRigidArea(new Dimension(20,20)));
		col1.add(new HBox(undo1));
		col1.add(new HBox(undo2));
		col1.add(Box.createRigidArea(new Dimension(20,10)));

		undoLimit = makeSlider("Undo Depth", "undo.limit",  0, 100, 20);
	    col1.add(makeNumberSliderRow("Undo Depth", "undo.limit", undoLimit));

		add(col1);   
	}

//----------------------------------------------------------------------------------------
    private JSlider makeSlider(String s, String propName, int min, int max, int val)
    {
        JSlider slider = new JSlider(JSlider.HORIZONTAL,min, max, val);
        slider.setMajorTickSpacing(max - min);
        slider.setPaintLabels(true);
        slider.setToolTipText("" + slider.getValue());
        slider.setMaximumSize(new Dimension(200, 36));
        slider.addChangeListener(e -> { slider.setToolTipText("" + slider.getValue());} );
       return slider;
    }
    
    
    
    private  HBox makeNumberSliderRow(String s, String propName, JSlider slider) 
    {
		JLabel label = new JLabel(s);
		setSizes(label, 150, 30);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setHorizontalTextPosition(SwingConstants.LEFT);
//			label.setBorder(BorderFactory.createLineBorder(Color.green));
//		JTextField fld = new JTextField("" + slider.getValue());
//		fld.addFocusListener(new FocusListener() {
//			
//			@Override
//			public void focusLost(FocusEvent e) {
//				String txt = fld.getText();
//				try
//				{
//					int val = Integer.parseInt(txt);
//					slider.setValue(val);
//				}
//				catch (Exception ex) {}
//			}
//			
//			@Override
//			public void focusGained(FocusEvent e) {			}
//		});
//		setSizes(fld, 80,30);
//        slider.addChangeListener(e -> { fld.setText("" + slider.getValue());} );
//        fld.setHorizontalAlignment(SwingConstants.RIGHT);
		components.put(propName, slider);
		Component spacer = Box.createHorizontalStrut(40);
		HBox line = new HBox(spacer, label, slider);		//fld, 
 		return line;
	}
}
