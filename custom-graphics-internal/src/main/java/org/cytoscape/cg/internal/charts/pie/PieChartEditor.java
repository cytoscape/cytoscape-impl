package org.cytoscape.cg.internal.charts.pie;

import static org.cytoscape.cg.internal.charts.AbstractChart.DATA_COLUMNS;

import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.cg.internal.charts.AbstractChartEditor;
import org.cytoscape.cg.internal.charts.ColorSchemeEditor;
import org.cytoscape.cg.model.ColorScheme;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

@SuppressWarnings("serial")
public class PieChartEditor extends AbstractChartEditor<PieChart> {

	private JLabel startAngleLbl;
	private JComboBox<Double> startAngleCmb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PieChartEditor(PieChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, false, false, false, true, false, false, false, false, serviceRegistrar);
		
		domainLabelPositionLbl.setVisible(false);
		getDomainLabelPositionCmb().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		startAngleLbl = new JLabel("Start Angle (degrees):");
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		var p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		var layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleCmb()))
				);
		
		return p;
	}
	
	private JComboBox<Double> getStartAngleCmb() {
		if (startAngleCmb == null) {
			startAngleCmb = createAngleComboBox(chart, PieChart.START_ANGLE, ANGLES);
		}
		
		return startAngleCmb;
	}
	
	@Override
	protected ColorSchemeEditor<PieChart> getColorSchemeEditor() {
		if (colorSchemeEditor == null) {
			colorSchemeEditor = new PieColorSchemeEditor(
					chart,
					getColorSchemes(),
					serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork(),
					serviceRegistrar.getService(IconManager.class)
			);
		}
		
		return colorSchemeEditor;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class PieColorSchemeEditor extends ColorSchemeEditor<PieChart> {

		public PieColorSchemeEditor(PieChart chart, ColorScheme[] colorSchemes, CyNetwork network,
				IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			if (total <= 0 && network != null) {
				var dataColumns = chart.getList(DATA_COLUMNS, CyColumnIdentifier.class);
				
				// Multiple columns are merged into one single series
				int listCount = 0;
				int singleCount = 0;
				
				var allNodes = network.getNodeList();
				var table = network.getDefaultNodeTable();
				
				for (var colId : dataColumns) {
					var column = table.getColumn(colId.getColumnName());
					
					if (column == null)
						continue;
					
					int count = 0;
					
					if (column.getType() == List.class) {
						for (var node : allNodes) {
							var row = network.getRow(node);
							var values = row.getList(column.getName(), column.getListElementType());
							
							if (values != null)
								count = Math.max(count, values.size());
						}
						
						listCount += count;
					} else {
						singleCount++;
					}
				}
				
				total = listCount + singleCount;
			}
			
			return total;
		}
	}
}
