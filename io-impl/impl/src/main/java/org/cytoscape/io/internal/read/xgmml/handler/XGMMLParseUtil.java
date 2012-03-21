package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XGMMLParseUtil {

	static final Pattern XLINK_PATTERN = Pattern.compile(".*#(-?\\d+)");
	
	public static double parseDocumentVersion(String value) {
		double version = 0.0;
    	
    	try {
			version = Double.parseDouble(value);
		} catch (Exception nfe) {
		}
    	
    	return version;
	}
    
	public static Long getIdFromXLink(String href) {
		Matcher matcher = XLINK_PATTERN.matcher(href);
		return matcher.matches() ? Long.valueOf(matcher.group(1)) : null;
	}
}
