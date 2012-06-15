package org.cytoscape.view.vizmap.internal.mappings;

import java.util.List;

import org.cytoscape.view.vizmap.mappings.ValueTranslator;

public class StringTranslator implements ValueTranslator<Object, String>{

	@Override
	public String translate(final Object inputValue) {

		if(inputValue != null) {
			if (inputValue instanceof List) {
				// Special handler for List column.
				final List<?> list = (List<?>)inputValue;
				final StringBuffer sb = new StringBuffer();

				if (list != null && !list.isEmpty()) {
					for (Object item : list)
						sb.append(item.toString() + "\n");

					sb.deleteCharAt(sb.length() - 1);
				}
				return sb.toString();
			} else
				return inputValue.toString();
		} else
			return null;
	}

	@Override
	public Class<String> getTranslatedValueType() {
		return String.class;
	}

}
