/*
  File: EquationParserImpl.java

  Copyright (c) 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.equations.internal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Function;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.equations.Node;
import org.cytoscape.equations.internal.builtins.*;
import org.cytoscape.equations.internal.parse_tree.*;


public class EquationParserImpl implements EquationParser {
	private String formula;
	private Tokeniser tokeniser;
	private Map<String, Function> nameToFunctionMap;
	private String lastErrorMessage;
	private Node parseTree;
	private Map<String, Class<?>> variableNameToTypeMap;
	private Set<String> variableReferences;
	private Map<String, Object> defaultVariableValues;
	private Set<Function> registeredFunctions;

	public EquationParserImpl() {
		this.nameToFunctionMap = new HashMap<String, Function>();
		this.registeredFunctions = new HashSet<Function>();
		this.parseTree = null;

		registerBuiltins();
	}

	public void registerFunction(final Function func) throws IllegalArgumentException {
		// Sanity check for the name of the function.
		final String funcName = func.getName().toUpperCase();
		if (funcName == null || funcName.equals(""))
			throw new IllegalArgumentException("empty or missing function name!");

		// Sanity check to catch duplicate function registrations.
		if (nameToFunctionMap.get(funcName) != null)
			throw new IllegalArgumentException("attempt at registering " + funcName + "() twice!");

		nameToFunctionMap.put(funcName, func);
		registeredFunctions.add(func);
	}

	/**
	 *  @return the function associated with the name "functionName" or null if no such function exists
	 */
	public Function getFunction(final String functionName) {
		return nameToFunctionMap.get(functionName);
	}

	public Set<Function> getRegisteredFunctions() { return registeredFunctions; }

	/**
	 *  @param formula                a valid formula which must start with an equal sign
	 *  @param variableNameToTypeMap  a list of existing variable names and their types
	 *  @return true if the parse succeeded otherwise false
	 */
	public boolean parse(final String formula, final Map<String, Class<?>> variableNameToTypeMap) {
		if (formula == null)
			throw new NullPointerException("formula string must not be null!");
		if (formula.length() < 1 || formula.charAt(0) != '=')
			throw new NullPointerException("0: formula string must start with an equal sign!");

		this.formula = formula;
		this.variableNameToTypeMap = variableNameToTypeMap;
		this.variableReferences = new TreeSet<String>();
		this.defaultVariableValues = new TreeMap<String, Object>();
		this.tokeniser = new Tokeniser(formula.substring(1));
		this.lastErrorMessage = null;

		try {
			parseTree = parseExpr();
			final Token token = tokeniser.getToken();
			final int tokenStartPos = tokeniser.getStartPos();
			if (token != Token.EOS)
				throw new IllegalStateException(
					tokenStartPos
					+ ": premature end of expression: expected end-of-string, but found "
					+ token + "!");
		} catch (final IllegalStateException e) {
			lastErrorMessage = e.getMessage();
			return false;
		} catch (final ArithmeticException e) {
			lastErrorMessage = e.getMessage();
			return false;
		} catch (final IllegalArgumentException e) {
			lastErrorMessage = e.getMessage();
			return false;
		}

		return true;
	}

	/**
	 *  @return the result type of the parsed formula if the parse succeeded, otherwise null
	 */
	public Class<?> getType() { return parseTree == null ? null : parseTree.getType(); }

	/**
	 *  If parse() failed, this will return the last error messages.
	 *  @return the last error message of null
	 */
	public String getErrorMsg() { return lastErrorMessage; }

	public Set<String> getVariableReferences() { return variableReferences; }

	public Map<String, Object> getDefaultVariableValues() { return defaultVariableValues; }

	/**
	 *  @return the parse tree.  Must only be called if parse() returns true!
	 */
	public Node getParseTree() { return parseTree; }

	//
	// The actual parsing takes place here.
	//

	/**
	 *   Implements expr --> term | term {+ term } | term {- term} | term {&amp; term} | term compOp term.
	 */
	private Node parseExpr() {
		Node exprNode = parseTerm();

		for (;;) {
			final Token token = tokeniser.getToken();
			final int sourceLocation = tokeniser.getStartPos();
			if (token == Token.PLUS || token == Token.MINUS || token == Token.AMPERSAND) {
				final Node term = parseTerm();
				if (token == Token.PLUS || token == Token.MINUS)
					exprNode = handleBinaryArithmeticOp(token, sourceLocation, exprNode, term);
				else // String concatenation.
					exprNode = new BinOpNode(sourceLocation, Token.AMPERSAND, exprNode, term);
			}
			else if (token.isComparisonOperator()) {
				final Node term = parseTerm();
				return handleComparisonOp(token, sourceLocation, exprNode, term); // No chaining for comparison operators!
			} else {
				tokeniser.ungetToken(token);
				return exprNode;
			}
		}
	}

	/**
	 *  Implements term --> power {* power} | power {/ power}
	 */
	private Node parseTerm() {
		Node termNode = parsePower();

		for (;;) {
			final Token token = tokeniser.getToken();
			final int sourceLocation = tokeniser.getStartPos();
			if (token == Token.MUL || token == Token.DIV) {
				final Node powerNode = parsePower();
				termNode = handleBinaryArithmeticOp(token, sourceLocation, termNode, powerNode);
			}
			else {
				tokeniser.ungetToken(token);
				return termNode;
			}
		}
	}

	/**
	 *  Implements power --> factor | factor ^ power
	 */
	private Node parsePower() {
		Node powerNode = parseFactor();

		final Token token = tokeniser.getToken();
		final int sourceLocation = tokeniser.getStartPos();
		if (token == Token.CARET) {
			final Node rhs = parsePower();
			powerNode = handleBinaryArithmeticOp(token, sourceLocation, powerNode, rhs);
		}
		else
			tokeniser.ungetToken(token);

		return powerNode;
	}

	/**
	 *  Implements factor --> constant | variable_ref | "(" expr ")" | ("-"|"+") factor  | func_call
	 */
	private Node parseFactor() {
		Token token = tokeniser.getToken();
		int sourceLocation = tokeniser.getStartPos();

		// 1. a constant
		if (token == Token.FLOAT_CONSTANT)
			return new FloatConstantNode(sourceLocation, tokeniser.getFloatConstant());
		else if (token == Token.STRING_CONSTANT)
			return new StringConstantNode(sourceLocation, tokeniser.getStringConstant());
		else if (token == Token.BOOLEAN_CONSTANT)
			return new BooleanConstantNode(sourceLocation, tokeniser.getBooleanConstant());

		// 2. a variable reference
		if (token == Token.DOLLAR) {
			final int varRefStartPos = sourceLocation;
			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			final boolean usingOptionalBraces = token == Token.OPEN_BRACE;
			if (usingOptionalBraces) {
				token = tokeniser.getToken();
				sourceLocation = tokeniser.getStartPos();
			}
			if (token != Token.IDENTIFIER)
				throw new IllegalStateException(sourceLocation + ": identifier expected!");

			final String ident = tokeniser.getIdent();
			final Class<?> varRefType = variableNameToTypeMap.get(ident);
			if (varRefType == null)
				throw new IllegalStateException(sourceLocation + ": unknown variable reference name: \""
				                                + ident + "\"!");
			variableReferences.add(ident);

			Object defaultValue = null;
			if (usingOptionalBraces) {
				token = tokeniser.getToken();

				// Do we have a default value?
				if (token == Token.COLON) {
					token = tokeniser.getToken();
					sourceLocation = tokeniser.getStartPos();
					if (token != Token.FLOAT_CONSTANT && token != Token.STRING_CONSTANT && token != Token.BOOLEAN_CONSTANT)
						throw new IllegalStateException(sourceLocation + ": expected default value for variable reference!");
					switch (token) {
					case FLOAT_CONSTANT:
						defaultValue = new Double(tokeniser.getFloatConstant());
						break;
					case BOOLEAN_CONSTANT:
						defaultValue = new Boolean(tokeniser.getBooleanConstant());
						break;
					case STRING_CONSTANT:
						defaultValue = new String(tokeniser.getStringConstant());
						break;
					}
					token = tokeniser.getToken();
					sourceLocation = tokeniser.getStartPos();
				}

				if (token != Token.CLOSE_BRACE)
					throw new IllegalStateException(sourceLocation + ": closing brace expected!");

				defaultVariableValues.put(ident, defaultValue);
			}

			return new IdentNode(varRefStartPos, tokeniser.getIdent(), defaultValue, varRefType);
		}

		// 3. a parenthesised expression
		if (token == Token.OPEN_PAREN) {
			final Node exprNode = parseExpr();
			token = tokeniser.getToken();
			if (token != Token.CLOSE_PAREN)
				throw new IllegalStateException(sourceLocation + ": '(' expected!");

			return exprNode;
		}

		// 4. a unary operator
		if (token == Token.PLUS || token == Token.MINUS) {
			final Node factor = parseFactor();
			return handleUnaryOp(sourceLocation, token, factor);
		}

		// 5. function call
		if (token == Token.IDENTIFIER) {
			tokeniser.ungetToken(token);
			return parseFunctionCall();
		}

		if (token == Token.ERROR)
			throw new IllegalStateException(sourceLocation + ": " + tokeniser.getErrorMsg());

		throw new IllegalStateException(sourceLocation + ": unexpected input token: " + token + "!");
	}

	private Node handleUnaryOp(final int sourceLocation, final Token operator, final Node operand) {
		final Class<?> operandType = operand.getType();
		if (operandType == Boolean.class || operandType == String.class
		    || FunctionUtil.isSomeKindOfList(operandType))
			throw new ArithmeticException(sourceLocation + ": can't apply a unary " + operator.asString()
			                              + " a boolean, string or list operand!");
		if (operandType == Double.class)
			return new UnaryOpNode(sourceLocation, operator, operand);
		else
			return new UnaryOpNode(sourceLocation, operator, new FConvNode(operand));
	}

	/**
	 *   Implements func_call --> ident "(" ")" | ident "(" expr {"," expr} ")".
	 */
	private Node parseFunctionCall() {
		Token token = tokeniser.getToken();
		final int functionNameStartPos = tokeniser.getStartPos();
		if (token != Token.IDENTIFIER)
			throw new IllegalStateException(functionNameStartPos + ": function name expected!");

		final String originalName = tokeniser.getIdent();
		final String functionNameCandidate = originalName.toUpperCase();
		if (functionNameCandidate.equals("DEFINED"))
			return parseDefined();

		final Function func = nameToFunctionMap.get(functionNameCandidate);
		if (func == null) {
			if (tokeniser.getToken() == Token.OPEN_PAREN)
				throw new IllegalStateException(functionNameStartPos + ": call to unknown function "
								+ originalName + "()!");
			else
				throw new IllegalStateException(functionNameStartPos + ": unknown text \""
								+ originalName + "\", maybe you forgot to put quotes around this text?");
		}

		token = tokeniser.getToken();
		final int openParenPos = tokeniser.getStartPos();
		if (token != Token.OPEN_PAREN)
			throw new IllegalStateException(openParenPos + ": expected '(' after function name \""
			                                + functionNameCandidate + "\"!");

		// Parse the comma-separated argument list.
		final ArrayList<Class<?>> argTypes = new ArrayList<Class<?>>();
		ArrayList<Node> args = new ArrayList<Node>();
		int sourceLocation;
		for (;;) {
			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token ==  Token.CLOSE_PAREN)
				break;

			tokeniser.ungetToken(token);
			final Node exprNode = parseExpr();
			argTypes.add(exprNode.getType());
			args.add(exprNode);

			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token != Token.COMMA)
				break;
		}

		final Class<?> returnType = func.validateArgTypes(argTypes.toArray(new Class<?>[argTypes.size()]));
		if (returnType == null)
			throw new IllegalStateException((openParenPos + 1) + ": invalid number or type of arguments in call to "
			                                + functionNameCandidate + "()!");

		if (token != Token.CLOSE_PAREN)
			throw new IllegalStateException(sourceLocation + ": expected the closing parenthesis of a call to "
			                                + functionNameCandidate + "!");

		Node[] nodeArray = new Node[args.size()];
		return new FuncCallNode(functionNameStartPos, func, returnType, args.toArray(nodeArray));
	}


	/**
	 *  Implements --> "DEFINED" "(" ident ")".  If the opening brace is found a closing brace is also required.
	 */
	private Node parseDefined() {
		Token token = tokeniser.getToken();
		final int definedStart = tokeniser.getStartPos();
		if (token != Token.OPEN_PAREN)
			throw new IllegalStateException(definedStart + ": \"(\" expected after \"DEFINED\"!");

		token = tokeniser.getToken();
		int sourceLocation = tokeniser.getStartPos();
		Class<?> varRefType;
		if (token != Token.DOLLAR) {
			if (token != Token.IDENTIFIER)
				throw new IllegalStateException(sourceLocation + ": variable reference expected after \"DEFINED(\"!");
			varRefType = variableNameToTypeMap.get(tokeniser.getIdent());
		}
		else {
			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token != Token.OPEN_BRACE)
				throw new IllegalStateException(sourceLocation + ": \"{\" expected after \"DEFINED($\"!");

			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token != Token.IDENTIFIER)
				throw new IllegalStateException(sourceLocation + ": variable reference expected after \"DEFINED(${\"!");

			varRefType = variableNameToTypeMap.get(tokeniser.getIdent());
			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token != Token.CLOSE_BRACE)
				throw new IllegalStateException(sourceLocation + ":\"}\" expected after after \"DEFINED(${"
				                                + tokeniser.getIdent() + "\"!");
		}

		token = tokeniser.getToken();
		sourceLocation = tokeniser.getStartPos();
		if (token != Token.CLOSE_PAREN)
			throw new IllegalStateException(sourceLocation + ": missing \")\" in call to DEFINED()!");

		return new BooleanConstantNode(definedStart, varRefType != null);
	}

	//
	// Helper functions for the parseXXX() methods.
	//

	/**
	 *  Deals w/ any necessary type conversions for any binary arithmetic operation on numbers.
	 */
	private Node handleBinaryArithmeticOp(final Token operator, final int sourceLocation, final Node lhs, final Node rhs) {
		// First operand is Double:
		if (lhs.getType() == Double.class && rhs.getType() == Double.class)
			return new BinOpNode(sourceLocation, operator, lhs, rhs);
		if (lhs.getType() == Double.class
		    && (rhs.getType() == Long.class || rhs.getType() == Boolean.class || rhs.getType() == String.class))
			return new BinOpNode(sourceLocation, operator, lhs, new FConvNode(rhs));

		// First operand is Long:
		if (lhs.getType() == Long.class && rhs.getType() == Double.class)
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), rhs);
		if (lhs.getType() == Long.class
		    && (rhs.getType() == Long.class || rhs.getType() == Boolean.class || rhs.getType() == String.class))
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), new FConvNode(rhs));

		// First operand is Boolean:
		if (lhs.getType() == Boolean.class && rhs.getType() == Double.class)
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), rhs);
		if (lhs.getType() == Boolean.class
		    && (rhs.getType() == Long.class || rhs.getType() == Boolean.class || rhs.getType() == String.class))
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), new FConvNode(rhs));

		// First operand is String:
		if (lhs.getType() == String.class && rhs.getType() == Double.class)
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), rhs);
		if (lhs.getType() == String.class
		    && (rhs.getType() == Long.class || lhs.getType() == Boolean.class || lhs.getType() == String.class))
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), new FConvNode(rhs));

		throw new ArithmeticException(sourceLocation + ": incompatible operands for \""
			                      + operator.asString() + "\"! (lhs="
			                      + lhs.toString() + ":" + lhs.getType() + ", rhs="
			                      + rhs.toString() + ":" + rhs.getType() + ")");
	}

	/**
	 *  Deals w/ any necessary type conversions for any binary comparison operation.
	 */
	private Node handleComparisonOp(final Token operator, final int sourceLocation, final Node lhs, final Node rhs) {
		// First operand is Double:
		if (lhs.getType() == Double.class && rhs.getType() == Double.class)
			return new BinOpNode(sourceLocation, operator, lhs, rhs);
		if (lhs.getType() == Double.class && rhs.getType() == Long.class)
			return new BinOpNode(sourceLocation, operator, lhs, new FConvNode(rhs));
		if (lhs.getType() == Double.class && rhs.getType() == Boolean.class)
			return new BinOpNode(sourceLocation, operator, lhs, new FConvNode(rhs));
		if (lhs.getType() == Double.class && rhs.getType() == Long.class)
			return new BinOpNode(sourceLocation, operator, lhs, new FConvNode(rhs));

		// First operand is Long:
		if (lhs.getType() == Long.class && rhs.getType() == Double.class)
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), rhs);
		if (lhs.getType() == Long.class
		    && (rhs.getType() == Long.class || rhs.getType() == Boolean.class || rhs.getType() == String.class))
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), new FConvNode(rhs));

		// First operand is Boolean:
		if (lhs.getType() == Boolean.class && rhs.getType() == Boolean.class)
			return new BinOpNode(sourceLocation, operator, lhs, rhs);
		if (lhs.getType() == Boolean.class && rhs.getType() == Double.class)
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), rhs);
		if (lhs.getType() == Boolean.class && rhs.getType() == Long.class)
			return new BinOpNode(sourceLocation, operator, new FConvNode(lhs), new FConvNode(rhs));
		if (lhs.getType() == Boolean.class && rhs.getType() == String.class)
			return new BinOpNode(sourceLocation, operator, new SConvNode(lhs), rhs);

		// First operand is String:
		if (lhs.getType() == String.class && rhs.getType() == String.class)
			return new BinOpNode(sourceLocation, operator, lhs, rhs);
		if (lhs.getType() == String.class
		    && (rhs.getType() == Double.class || rhs.getType() == Long.class || rhs.getType() == Boolean.class))
			return new BinOpNode(sourceLocation, operator, lhs, new SConvNode(rhs));

		throw new IllegalArgumentException(sourceLocation + ": incompatible operands for \""
			                           + operator.asString() + "\"! (lhs="
			                           + lhs.toString() + ":" + lhs.getType() + ", rhs="
			                           + rhs.toString() + ":" + rhs.getType() + ")");
	}

	private void registerBuiltins() {
		registerFunction(new Abs());
		registerFunction(new ACos());
		registerFunction(new ASin());
		registerFunction(new And());
		registerFunction(new ATan2());
		registerFunction(new Average());
		registerFunction(new BList());
		registerFunction(new Combin());
		registerFunction(new Concatenate());
		registerFunction(new Cos());
		registerFunction(new Cosh());
		registerFunction(new Count());
		registerFunction(new Degrees());
		registerFunction(new org.cytoscape.equations.internal.builtins.Error());
		registerFunction(new Exp());
		registerFunction(new First());
		registerFunction(new FList());
		registerFunction(new GeoMean());
		registerFunction(new HarMean());
		registerFunction(new If());
		registerFunction(new IList());
		registerFunction(new Largest());
		registerFunction(new Last());
		registerFunction(new Left());
		registerFunction(new Len());
		registerFunction(new ListToString());
		registerFunction(new Ln());
		registerFunction(new Log());
		registerFunction(new Lower());
		registerFunction(new Max());
		registerFunction(new Median());
		registerFunction(new Mid());
		registerFunction(new Min());
		registerFunction(new Mod());
		registerFunction(new Mode());
		registerFunction(new Not());
		registerFunction(new NormDist());
		registerFunction(new Now());
		registerFunction(new Nth());
		registerFunction(new Or());
		registerFunction(new Permut());
		registerFunction(new Pi());
		registerFunction(new Product());
		registerFunction(new Radians());
		registerFunction(new Right());
		registerFunction(new Round());
		registerFunction(new Sign());
		registerFunction(new Sin());
		registerFunction(new Sinh());
		registerFunction(new SList());
		registerFunction(new StDev());
		registerFunction(new Sqrt());
		registerFunction(new Substitute());
		registerFunction(new Sum());
		registerFunction(new Tan());
		registerFunction(new Tanh());
		registerFunction(new Text());
		registerFunction(new Today());
		registerFunction(new Trunc());
		registerFunction(new Upper());
		registerFunction(new Value());
		registerFunction(new Var());
	}
}
