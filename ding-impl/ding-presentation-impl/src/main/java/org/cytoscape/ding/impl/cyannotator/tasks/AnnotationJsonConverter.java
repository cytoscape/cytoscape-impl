package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.Font;
import java.util.Map;
import java.util.function.Function;

import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotationJsonConverter {

	public static String toJson(DingAnnotation annotation) {
		var args = annotation.getArgMap();
		var objectMapper = new ObjectMapper();

		// Re-map color and font values for JSON
		// These values must be changed here because the values stored in the session can't change because of backwards compatibility.
		convert(AnnotationJsonConverter::convertColor, args, ArrowAnnotationImpl.ARROWCOLOR);
		convert(AnnotationJsonConverter::convertColor, args, ArrowAnnotationImpl.SOURCECOLOR);
		convert(AnnotationJsonConverter::convertColor, args, ArrowAnnotationImpl.TARGETCOLOR);
		convert(AnnotationJsonConverter::convertColor, args, TextAnnotation.COLOR);
		convert(AnnotationJsonConverter::convertColor, args, ShapeAnnotation.FILLCOLOR);
		convert(AnnotationJsonConverter::convertColor, args, ShapeAnnotation.EDGECOLOR);
		
		convert(AnnotationJsonConverter::convertFont,  args, TextAnnotationImpl.FONTSTYLE);
		
		try {
			return objectMapper.writeValueAsString(args);
		} catch (JsonProcessingException e) {
			return "{\"error\":\"" + e.getMessage() + "\"}";
		}
	}
	
	private static void convert(Function<String,String> converter, Map<String, String> args, String key) {
		if(args.containsKey(key)) {
			String val = args.get(key);
			val = converter.apply(val);
			if(val != null) {
				args.put(key, val);
			}
		}
	}
	
	private static String convertColor(String color) {
		try {
			int rgb = Integer.parseInt(color);
			return String.format("#%06X", 0xFFFFFF&(rgb));
		} catch(NumberFormatException e) {
			return null;
		}
	}
	
	private static String convertFont(String fontCode) {
		try {
			int style = Integer.parseInt(fontCode);
			if (style == Font.PLAIN)
				return "plain";
			if (style == Font.BOLD)
				return "bold";
			if (style == Font.ITALIC)
				return "italic";
			if (style == (Font.ITALIC | Font.BOLD))
				return "bolditalic";
			return "";
		} catch(NumberFormatException e) {
			return null;
		}
	}
}
