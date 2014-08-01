package org.cytoscape.ding.internal.charts.stripe;

import static org.cytoscape.ding.internal.charts.AbstractChartCustomGraphics.DATA_COLUMNS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.ColorScheme;
import org.cytoscape.ding.internal.charts.ColorSchemeEditor;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class StripeChartEditor extends AbstractChartEditor<StripeChart> {

	private static final long serialVersionUID = -7480674403722656873L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public StripeChartEditor(final StripeChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Object.class, false, 1, false, true, false, false, false, false, appMgr, iconMgr, colIdFactory);
		
		setDistinctValues();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected ColorSchemeEditor<StripeChart> getColorSchemeEditor() {
		if (colorSchemeEditor == null) {
			colorSchemeEditor = new StripeColorSchemeEditor(chart, getColorSchemes(), appMgr.getCurrentNetwork(),
					iconMgr);
		}
		
		return colorSchemeEditor;
	}
	
	@Override
	protected JComboBox createDataColumnComboBox(final Collection<CyColumnIdentifier> columns, final boolean acceptsNull) {
		final JComboBox cmb = new CyColumnComboBox(columns, false);
		cmb.setSelectedItem(null);
		cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<CyColumnIdentifier> colIds = getDataPnl().getDataColumnNames();
				chart.set(DATA_COLUMNS, colIds);
				setDistinctValues();
			}
		});
		
		return cmb;
	}
	
	@Override
	protected boolean isDataColumn(final CyColumn c) {
		// This chart can only be mapped to List-typed columns
		return List.class.isAssignableFrom(c.getType()) && super.isDataColumn(c);
	}
	
	private static SortedSet<Object> getDistinctValues(final CyNetwork network, final CyColumnIdentifier columnId) {
		final SortedSet<Object> values;
		
		final List<CyNode> allNodes = network.getNodeList();
		final CyTable table = network.getDefaultNodeTable();
		final CyColumn column = table.getColumn(columnId.getColumnName());
		
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
				values.addAll(StripeChart.getDistinctValuesFromRow(network, node, columnId));
		} else {
			values = new TreeSet<Object>();
		}
		
		return values;
	}
	
	private void setDistinctValues() {
		final List<CyColumnIdentifier> colIds = getDataPnl().getDataColumnNames();
		
		if (!colIds.isEmpty()) {
			final SortedSet<Object> distinctValues = 
					getDistinctValues(appMgr.getCurrentNetwork(), colIds.get(0));
			chart.set(StripeChart.DISTINCT_VALUES, new ArrayList<Object>(distinctValues));
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class StripeColorSchemeEditor extends ColorSchemeEditor<StripeChart> {

		private static final long serialVersionUID = -4721595280722442047L;
		
		public StripeColorSchemeEditor(final StripeChart chart, final ColorScheme[] colorSchemes, final CyNetwork network,
				final IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			if (total <= 0) {
				final List<CyColumnIdentifier> dataColumns = chart.getList(DATA_COLUMNS, CyColumnIdentifier.class);
				
				if (network != null) {
					final Set<Object> set = new HashSet<Object>();
					
					final List<CyNode> allNodes = network.getNodeList();
					final CyTable table = network.getDefaultNodeTable();
					
					for (final CyColumnIdentifier colId : dataColumns) {
						final CyColumn column = table.getColumn(colId.getColumnName());
						
						if (column != null && column.getType() == List.class) {
							for (final CyNode node : allNodes) {
								final CyRow row = network.getRow(node);
								final List<?> values = row.getList(colId.getColumnName(), column.getListElementType());
								
								if (values != null)
									set.addAll(values);
							}
						}
					}
					
					total = set.size();
				}
			}
			
			return total;
		}
	}
}
