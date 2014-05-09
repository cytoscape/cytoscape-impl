package org.cytoscape.ding.internal.charts.stripe;

import static org.cytoscape.ding.internal.charts.AbstractEnhancedCustomGraphics.DATA_COLUMNS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class StripeChartEditor extends AbstractChartEditor<StripeChart> {

	private static final long serialVersionUID = -7480674403722656873L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public StripeChartEditor(final StripeChart chart, final CyApplicationManager appMgr) {
		super(chart, Object.class, 1, false, true, false, false, false, false, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JComboBox createDataColumnComboBox(final Collection<CyColumn> columns, final boolean acceptsNull) {
		final JComboBox cmb = new CyColumnComboBox(columns, false);
		cmb.setSelectedItem(null);
		cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> colNames = getDataPnl().getDataColumnNames();
				chart.set(DATA_COLUMNS, colNames);
				
				if (!colNames.isEmpty()) {
					final SortedSet<Object> distinctValues = 
							getDistinctValues(appMgr.getCurrentNetwork(), colNames.get(0));
					chart.set(StripeChart.DISTINCT_VALUES, new ArrayList<Object>(distinctValues));
				}
			}
		});
		
		return cmb;
	}
	
	private static SortedSet<Object> getDistinctValues(final CyNetwork network, final String columnName) {
		final SortedSet<Object> values;
		
		final List<CyNode> allNodes = network.getNodeList();
		final CyTable table = network.getDefaultNodeTable();
		final CyColumn column = table.getColumn(columnName);
		
		if (column != null && column.getType() == List.class) {
			final Class<?> listElementType = column.getListElementType();
			final Collator collator = Collator.getInstance(Locale.getDefault());
			
			values = new TreeSet<Object>(new Comparator<Object>() {
				@Override
				public int compare(final Object o1, final Object o2) {
					if (Number.class.isAssignableFrom(listElementType)) {
						final double d1 = ((Number)o1).doubleValue();
						final double d2 = ((Number)o2).doubleValue();
						
						if (d1 < d2) return -1;
						if (d1 > d2) return 1;
						return 0;
					} else {
						return collator.compare(o1.toString(), o2.toString());
					}
				}
			});
			
			for (final CyNode node : allNodes)
				values.addAll(StripeChart.getDistinctValuesFromRow(network, node, columnName));
		} else {
			values = new TreeSet<Object>();
		}
		
		return values;
	}
}
