package org.cytoscape.view.vizmap.internal.mappings;

import java.awt.Paint;

import org.cytoscape.view.vizmap.mappings.ValueTranslator;

public class ColorTranslator implements ValueTranslator<String, Paint>{

	@Override
	public Paint translate(String inputValue) {
		if(inputValue != null)
			return ColorUtil.parseColorText(inputValue);
		else
			return null;
	}

	@Override
	public Class<Paint> getTranslatedValueType() {
		return Paint.class;
	}

}
