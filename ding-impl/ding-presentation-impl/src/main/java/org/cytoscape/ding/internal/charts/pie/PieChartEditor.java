package org.cytoscape.ding.internal.charts.pie;

import static org.cytoscape.ding.internal.charts.AbstractChart.DATA_COLUMNS;

import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.ColorSchemeEditor;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class PieChartEditor extends AbstractChartEditor<PieChart> {

	private static final long serialVersionUID = -6185083260942898226L;
	
	private JLabel startAngleLbl;
	private JComboBox<Double> startAngleCmb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PieChartEditor(final PieChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, false, false, false, true, false, false, false, false, appMgr, iconMgr, colIdFactory);
		
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
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
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
			colorSchemeEditor = new PieColorSchemeEditor(chart, getColorSchemes(), appMgr.getCurrentNetwork(), iconMgr);
		}
		
		return colorSchemeEditor;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class PieColorSchemeEditor extends ColorSchemeEditor<PieChart> {

		private static final long serialVersionUID = 9051308361014862739L;

		public PieColorSchemeEditor(final PieChart chart, final ColorScheme[] colorSchemes, final CyNetwork network,
				final IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			if (total <= 0 && network != null) {
				final List<CyColumnIdentifier> dataColumns = chart.getList(DATA_COLUMNS, CyColumnIdentifier.class);
				
				// Multiple columns are merged into one single series
				int listCount = 0;
				int singleCount = 0;
				
				final List<CyNode> allNodes = network.getNodeList();
				final CyTable table = network.getDefaultNodeTable();
				
				for (final CyColumnIdentifier colId : dataColumns) {
					final CyColumn column = table.getColumn(colId.getColumnName());
					if (column == null) continue;
					
					int count = 0;
					
					if (column.getType() == List.class) {
						for (final CyNode node : allNodes) {
							final CyRow row = network.getRow(node);
							final List<?> values = row.getList(column.getName(), column.getListElementType());
							
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
