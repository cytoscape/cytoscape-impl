package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberSeriesMappingGenerator<V extends Number> extends AbstractDiscreteMappingGenerator<V> {

	private static final Logger logger = LoggerFactory.getLogger(NumberSeriesMappingGenerator.class);

	public NumberSeriesMappingGenerator(final Class<V> type) {
		super(type);
	}

	@Override
	public <T> Map<T, V> generateMap(final Set<T> attributeSet) {

		final Map<T, V> valueMap = new HashMap<T, V>();

		// Error check
		if (attributeSet == null || attributeSet.size() == 0)
			return valueMap;

		final String start = JOptionPane.showInputDialog(null, "Enter start value (1st number of the series)", "0");
		final String increment = JOptionPane.showInputDialog(null, "Enter increment", "1");

		if ((increment == null) || (start == null))
			return valueMap;

		Double inc;
		Double st;
		try {
			inc = Double.valueOf(increment);
			st = Double.valueOf(start);
		} catch (Exception ex) {
			logger.error("Invalid value.", ex);
			inc = null;
			st = null;
		}

		if ((inc == null) || (inc.doubleValue() < 0) || (st == null))
			return null;

		for (T key : attributeSet) {
			valueMap.put(key, (V) st);
			st = st + inc;
		}

		return valueMap;
	}

}
