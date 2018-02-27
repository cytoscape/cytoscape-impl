package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import org.cytoscape.internal.prefs.lib.BoxComponent;
import org.cytoscape.internal.prefs.lib.ColorMenuButton;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.VBox;


public class PrefsTables extends AbstractPrefsPanel
{
	public PrefsTables(Cy3PreferencesRoot dlog)
	{
		super(dlog, "table");
		init();
	}
	
//	private JComboBox fCbDestination ;
	private JCheckBox fCkHeatMap, fCkShowButtons, fCkRememberColumnNames;	//fCkQuickClose, 
    private ColorMenuButton fGridLineColor, fGridFillColor;

	
	//   	private BoxSubPanel fTablePanel; 
	
	private void init()
	{
//		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		Box panel = Box.createVerticalBox();
		setBorder(new EmptyBorder(12, 40, 0, 0));

//		BoxSubPanel opts = new BoxSubPanel("Table Options", false);
//		Box line1 = Box.createHorizontalBox();
//		line1.add( new JLabel("Destination:"));
//		line1.add(fCbDestination = new JComboBox());
//		fCbDestination.setMaximumSize(new Dimension(120, 27));
//		line1.add(Box.createRigidArea(new Dimension(20,20)));
//		line1.add(fCkOpenAfterSaving = new JCheckBox("Open After Saving"));
//		line1.setAlignmentX(Component.LEFT_ALIGNMENT);
//		opts.add(line1);
//
//		fCkHeatMap = makeCheckBox("Show as Heat Maps", "If checked, all numerical columns in your table will be color coded based on normalized intensity");
//		fCkShowButtons = makeCheckBox("Show Button Bar", "If checked, a tool bar is visible");
//		fCkRememberColumnNames = makeCheckBox("Remember Column Names", "If checked, a tool bar is visible");
//		Box line2 = Box.createHorizontalBox();
//		line2.add(fCkHeatMap);
////		line2.add(fCkQuickClose);
//		line2.add(fCkShowButtons);
//		line2.add(fCkRememberColumnNames);
//		line2.setAlignmentX(Component.LEFT_ALIGNMENT);
//		opts.add(line2);
//			
//		lineA.add(opts);
//		GuiFactory.setSizes(opts, new Dimension(550,80));

		Box repoBox = Box.createHorizontalBox(); // makeRepositoryPanel();
//		repoBox.equalizeWidth(opts);
		Box lineA = Box.createHorizontalBox();
		Box lineB = Box.createHorizontalBox();
		Box lineC = Box.createHorizontalBox();
		lineC.add(repoBox);
		panel.add(lineA);
		panel.add(lineB);
		panel.add(lineC);
	
//		panel.add(colors);
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
//		page.add(Box.createRigidArea(new Dimension(10,20)));
//		page.add(col3);
		col1.add(makeColumnList());
		col2.add(new VBox( true, true, makeRightSide(), new JLabel("Import Profiles"), makeImportList()));
		setSizes(page, new Dimension(550,350));
		add(page);
    }

	private Component makeRightSide() {
		Box right = Box.createVerticalBox();
//		right.add(makeCheckBoxLine("Close Tables Without Save", "", "", "If checked, tables will be disposed upon closing without asking you to save"));
		right.add(makeCheckBoxLine("Show Button Bar", "", "If checked, a tool bar is visible"));
		right.add(makeCheckBoxLine("Alternating Background", "", "If checked, a tool bar is visible"));
//		right.add(makeCheckBoxLine("Remember Column Names",  "", "If checked, a tool bar is visible"));
		right.add(makeCheckBoxLine("Show as Heat Maps", "", "If checked, all numerical columns in your table will be color coded based on normalized intensity"));
      	
        Box colorline = Box.createHorizontalBox();
        fGridLineColor = new ColorMenuButton();
		fGridLineColor.setText("Color");
		fGridLineColor.setToolTipText("Set the line color distinguishing cells in the table");
		colorline.add(new HBox(Box.createRigidArea(new Dimension(12,15)), new BoxComponent("Grid Color", fGridLineColor)));
//
		fGridFillColor = new ColorMenuButton();
		fGridFillColor.setText("Color");
		fGridFillColor.setToolTipText("Set the background color for alternate rows in the grid");
//		colorline.add(Box.createRigidArea(dim2Cols));
		HBox colorline2 = new HBox(Box.createRigidArea(new Dimension(12,15)), new BoxComponent("Fill Color:", fGridFillColor), Box.createHorizontalGlue());		
		right.add(new VBox(true, colorline, colorline2));
		right.add(Box.createVerticalGlue());
	
		return right;
	}
	
    private Box makeImportList() {
    	MasterListTableModel importerModel = new MasterListTableModel();
    	JTable tablOfColumns = new JTable();
    	tablOfColumns.setModel(importerModel);
    	importerModel.addRecord("Importer1");
    	importerModel.addRecord("10X transform");
    	importerModel.addRecord("RNA-seq 4");
      	JScrollPane container = new JScrollPane(tablOfColumns);
		Box tableListColumn = Box.createVerticalBox();
	  	tableListColumn.add(container);
	  	return tableListColumn;
	  	}	
    
    
    private VBox makeColumnList()
    {
    	MasterListTableModel tableTableModel = new MasterListTableModel();
    	JTable tablOfColumns = new JTable();
    	tablOfColumns.setModel(tableTableModel);
    	tableTableModel.addRecord("Name");
    	tableTableModel.addRecord("Shared Name");
    	tableTableModel.addRecord("Interactions");
    	tableTableModel.addRecord("Weight");
    	tableTableModel.addRecord("Spring-weighted");
      	JScrollPane container = new JScrollPane(tablOfColumns);
      	VBox tableListColumn = new VBox(false, false, Box.createRigidArea(new Dimension(10,10)), container);
	  	tableListColumn.add(container);
	  	return tableListColumn;
   }
	//---------------------------------------------------------------------------------------------------------
	   @Override public void install(Map<String, String> prefs)
	    {
	    	Map<String, String> map = getPropertyMap();
    	
	      }
	    
	    @Override public Map<String, String> extract()
	    {
	    	Map<String, String> map = getPropertyMap();
	    	return map;
   
	    }
//
//	public static SElement getDefaults()
//	{
//		SElement e = new SElement("Tables");
//		return e;
//		
//	}
}
