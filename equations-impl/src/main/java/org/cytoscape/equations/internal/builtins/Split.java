package org.cytoscape.equations.internal.builtins;

import java.util.regex.Pattern;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.equations.internal.Categories;
import org.cytoscape.equations.internal.StringList;


/**
 * @since 3.9
 */
public class Split extends AbstractFunction {
	
	public Split() {
		super(new ArgDescriptor[] {
			new ArgDescriptor(ArgType.STRING, "text", "The text that will be split."),
			new ArgDescriptor(ArgType.STRING, "delimiter", "The text that will be used as a delimiter.")
		});
	}

	public String getName() { return "SPLIT"; }
	
	@Override
	public String getCategoryName() { return Categories.TEXT; }

	public String getFunctionSummary() { return "Splits a text string into substrings around matches of a delimiter. Returns a list of strings."; }

	public Class<?> getReturnType() { return StringList.class; }
	
	public Object evaluateFunction(Object[] args) {
		final String text = FunctionUtil.getArgAsString(args[0]);
		final String delimiter = FunctionUtil.getArgAsString(args[1]);

		String[] strings = text.split(Pattern.quote(delimiter));
		return new StringList(strings);
	}
}