package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.util.ColorUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;


public class TextAnnotationTunables extends AbstractAnnotationTunables {
	private static final String PLAIN = "Plain";
	private static final String BOLD = "Bold";
	private static final String ITALIC = "Italic";
	private static final String BOLD_ITALIC = "BoldItalic";
	
	private static final String[] FONT_STYLES = { PLAIN, BOLD, ITALIC, BOLD_ITALIC };
	private static final List<String> fontFamilies = getFontFamilies();

  @Tunable(context="nogui", 
           required=true,
           description="The text to be displayed")
  public String text = null;

  @Tunable(context="nogui", 
           description="The font size")
  public Integer fontSize = null;

  @Tunable(context="nogui", 
           description="The color of the text")
  public String color = null;

  @Tunable(context="nogui", 
           description="The font style (bold, italic, etc.)")
  public String fontStyle = null;
  // public ListSingleSelection<String> fontStyle = new ListSingleSelection<String>(Arrays.asList(FONT_STYLES));

  @Tunable(context="nogui", 
           description="The font family to use")
  public String fontFamily = null;
  // public ListSingleSelection<String> fontFamily = new ListSingleSelection<String>(fontFamilies);

  public TextAnnotationTunables () {
  }

  public Map<String, String> getArgMap(TaskMonitor tm) {
    var args = new HashMap<String, String>();
    putIfNotNull(tm, args, TextAnnotation.TEXT, text);
    putIfNotNull(tm, args, TextAnnotation.COLOR, getColor(color));
    putIfNotNull(tm, args, TextAnnotation.FONTSTYLE, fontStyle, Arrays.asList(FONT_STYLES));
    putIfNotNull(tm, args, TextAnnotation.FONTFAMILY, fontFamily, fontFamilies);

    return args;
  }

  private static List<String> getFontFamilies() {
	  Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    List<String> strFonts = new ArrayList<String>();
    for (var font: fonts) {
      strFonts.add(font.getFamily());
    }
    return strFonts;
  }

  public void update(TaskMonitor tm, Annotation annotation) {
    TextAnnotation tAnnotation = (TextAnnotation)annotation;
    if (text != null) tAnnotation.setText(text);
    if (fontFamily != null) tAnnotation.setFontFamily(fontFamily);
    if (color != null) tAnnotation.setTextColor(ColorUtil.parseColor(color));
    if (fontStyle != null) {
      if (fontStyle.equalsIgnoreCase("bold"))
        tAnnotation.setFontStyle(Font.BOLD);
      else if (fontStyle.equalsIgnoreCase("plain"))
        tAnnotation.setFontStyle(Font.PLAIN);
      else if (fontStyle.equalsIgnoreCase("italic"))
        tAnnotation.setFontStyle(Font.ITALIC);
      else if (fontStyle.equalsIgnoreCase("bolditalic"))
        tAnnotation.setFontStyle(Font.BOLD|Font.ITALIC);
    }
  }
}
