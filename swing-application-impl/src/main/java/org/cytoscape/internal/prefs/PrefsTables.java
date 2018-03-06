package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.cytoscape.internal.prefs.lib.BoxComponent;
import org.cytoscape.internal.prefs.lib.ColorMenuButton;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.VBox;

public class PrefsTables extends AbstractPrefsPanel
{
	public PrefsTables(Cy3PreferencesPanel dlog)
	{
		super(dlog, "table", "Tables", "\uf0ce", "Options to customize the display of tables in Cytoscape");
		init();
	}
	
//	private JComboBox fCbDestination ;
	private JCheckBox fCkShowButtons, fCkRememberColumnNames;	//fCkQuickClose,  fCkHeatMap, 
    private ColorMenuButton fGridLineColor, fGridFillColor;

	
	//   	private BoxSubPanel fTablePanel; 
	
	private void init()
	{
//		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		Box panel = Box.createVerticalBox();
		setBorder(new EmptyBorder(12, 40, 0, 0));

		Box repoBox = Box.createHorizontalBox(); // makeRepositoryPanel();
		Box lineA = Box.createHorizontalBox();
		Box lineB = Box.createHorizontalBox();
		Box lineC = Box.createHorizontalBox();
		lineC.add(repoBox);
		panel.add(lineA);
		panel.add(lineB);
		panel.add(lineC);
		add(panel);
		
	}
    @Override public void initUI()
    {
        super.initUI();
		Box page = Box.createHorizontalBox();
		Box col1 = Box.createVerticalBox();
		Box col2 = Box.createVerticalBox();
		col1.setMaximumSize(new Dimension(100, 10000));
		page.add(col1);
		page.add(Box.createRigidArea(new Dimension(10,20)));
		page.add(col2);
//		col1.add(makeColumnList());
		col2.add(new VBox( true, true, makeRightSide()));
//		col2.add(new VBox( true, true, makeRightSide(), new JLabel("Import Profiles"), makeImportList()));
		setSizes(page, new Dimension(550,350));
	    JLabel line0 = new JLabel("This panel houses future preference settings to customize tables.");
	    line0.setFont(ital11);
		add(new HBox(line0));
		add(page);
    }

	private Component makeRightSide() {
		Box right = Box.createVerticalBox();
//		right.add(makeCheckBoxLine("Close Tables Without Save", "", "", "If checked, tables will be disposed upon closing without asking you to save"));
		right.add(makeCheckBoxLine("Show Button Bar", "table.showButtonBar", "If checked, a tool bar is visible"));
		right.add(makeCheckBoxLine("Alternating Background", "table.stripedBackground", "If checked, a tool bar is visible"));
//		right.add(makeCheckBoxLine("Remember Column Names",  "", "If checked, a tool bar is visible"));
//		right.add(makeCheckBoxLine("Show as Heat Maps", "table.heatMaps", "If checked, all numerical columns in your table will be color coded based on normalized intensity"));
		right.add(makeLabeledField("Number Format String", "floatingPointColumnFormat", "%.2f", "The digit specifies the number of decimal places; use e for scientific notation, or f for floating point"));
      	
        Box colorline = Box.createHorizontalBox();
        fGridLineColor = new ColorMenuButton();
		fGridLineColor.setText("Color");
		fGridLineColor.setToolTipText("Set the line color distinguishing cells in the table");
		components.put("table.gridlineColor", fGridLineColor);			
		colorline.add(new HBox(Box.createRigidArea(new Dimension(12,15)), new BoxComponent("Grid Color", fGridLineColor)));
//
		fGridFillColor = new ColorMenuButton();
		fGridFillColor.setText("Color");
		fGridFillColor.setToolTipText("Set the background color for alternate rows in the grid");
		components.put("table.gridFillColor", fGridLineColor);			
		HBox colorline2 = new HBox(Box.createRigidArea(new Dimension(12,15)), new BoxComponent("Fill Color:", fGridFillColor), Box.createHorizontalGlue());		
		right.add(new VBox(true, colorline, colorline2));
		right.add(Box.createVerticalGlue());
	
		return right;
	}
	
	int decPlaces = 2;
	private void update()
	{
		// generate sample text from curretn settings
	}
	HBox makeNumberFormatWidget()
	{
		ImageIcon increase = new ImageIcon(getClass().getResource("/images/decimalIncrease.png"));
		ImageIcon decrease = new ImageIcon(getClass().getResource("/images/decimalDecrease.png"));
		JTextField txtFld = new JTextField();
		JCheckBox sciNot = new JCheckBox("Scientific Notation");
		JLabel label = new JLabel("Format");
		JLabel sample = new JLabel("1.23");
		JButton moreDecPlaces = new JButton(increase);
		moreDecPlaces.addActionListener(e -> { decPlaces++; update(); } );
		JButton fewerDecPlaces = new JButton(decrease);
		HBox line = new HBox(true, true, label, sample, moreDecPlaces, fewerDecPlaces, sciNot, txtFld );
		return line;
	}
//	
//    private Box makeImportList() {
//	    	MasterListTableModel importerModel = new MasterListTableModel();
//	    	JTable tablOfColumns = new JTable();
//	    	tablOfColumns.setModel(importerModel);
//	    	importerModel.addRecord("Importer1");
//	    	importerModel.addRecord("10X transform");
//	    	importerModel.addRecord("RNA-seq 4");
//      	JScrollPane container = new JScrollPane(tablOfColumns);
//		Box tableListColumn = Box.createVerticalBox();
//	  	tableListColumn.add(container);
//	  	return tableListColumn;
//	  	}	
//    
    
//    private VBox makeColumnList()
//    {
//	    	MasterListTableModel tableTableModel = new MasterListTableModel();
//	    	JTable tablOfColumns = new JTable();
//	    	tablOfColumns.setModel(tableTableModel);
//	    	tableTableModel.addRecord("Name");
//	    	tableTableModel.addRecord("Shared Name");
//	    	tableTableModel.addRecord("Interactions");
//	    	tableTableModel.addRecord("Weight");
//	    	tableTableModel.addRecord("Spring-weighted");
//	  	JScrollPane container = new JScrollPane(tablOfColumns);
//	  	VBox tableListColumn = new VBox(false, false, Box.createRigidArea(new Dimension(10,10)), container);
//	  	tableListColumn.add(container);
//	  	return tableListColumn;
//   }
	//---------------------------------------------------------------------------------------------------------
//	   @Override public void install(Map<String, CyProperty<?>> cyPropMap)
//	    {
//    	
//	      }
//	    
//	    @Override public void extract(Map<String, CyProperty<?>> cyPropMap)
//	    {
//	    }
//
//	public static SElement getDefaults()
//	{
//		SElement e = new SElement("Tables");
//		return e;
//		
//	}
}
