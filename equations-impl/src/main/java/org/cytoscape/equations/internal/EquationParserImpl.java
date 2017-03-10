package org.cytoscape.equations.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cytoscape.equations.AbstractNode;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Function;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.equations.TreeNode;
import org.cytoscape.equations.event.EquationFunctionAddedEvent;
import org.cytoscape.equations.event.EquationFunctionRemovedEvent;
import org.cytoscape.equations.internal.builtins.ACos;
import org.cytoscape.equations.internal.builtins.ASin;
import org.cytoscape.equations.internal.builtins.ATan2;
import org.cytoscape.equations.internal.builtins.Abs;
import org.cytoscape.equations.internal.builtins.And;
import org.cytoscape.equations.internal.builtins.Average;
import org.cytoscape.equations.internal.builtins.BList;
import org.cytoscape.equations.internal.builtins.Combin;
import org.cytoscape.equations.internal.builtins.Concatenate;
import org.cytoscape.equations.internal.builtins.Cos;
import org.cytoscape.equations.internal.builtins.Cosh;
import org.cytoscape.equations.internal.builtins.Count;
import org.cytoscape.equations.internal.builtins.Degrees;
import org.cytoscape.equations.internal.builtins.Exp;
import org.cytoscape.equations.internal.builtins.FList;
import org.cytoscape.equations.internal.builtins.First;
import org.cytoscape.equations.internal.builtins.GeoMean;
import org.cytoscape.equations.internal.builtins.HarMean;
import org.cytoscape.equations.internal.builtins.IList;
import org.cytoscape.equations.internal.builtins.If;
import org.cytoscape.equations.internal.builtins.Largest;
import org.cytoscape.equations.internal.builtins.Last;
import org.cytoscape.equations.internal.builtins.Left;
import org.cytoscape.equations.internal.builtins.Len;
import org.cytoscape.equations.internal.builtins.ListToString;
import org.cytoscape.equations.internal.builtins.Ln;
import org.cytoscape.equations.internal.builtins.Log;
import org.cytoscape.equations.internal.builtins.Lower;
import org.cytoscape.equations.internal.builtins.Max;
import org.cytoscape.equations.internal.builtins.Median;
import org.cytoscape.equations.internal.builtins.Mid;
import org.cytoscape.equations.internal.builtins.Min;
import org.cytoscape.equations.internal.builtins.Mod;
import org.cytoscape.equations.internal.builtins.Mode;
import org.cytoscape.equations.internal.builtins.NormDist;
import org.cytoscape.equations.internal.builtins.Not;
import org.cytoscape.equations.internal.builtins.Now;
import org.cytoscape.equations.internal.builtins.Nth;
import org.cytoscape.equations.internal.builtins.Or;
import org.cytoscape.equations.internal.builtins.Permut;
import org.cytoscape.equations.internal.builtins.Pi;
import org.cytoscape.equations.internal.builtins.Product;
import org.cytoscape.equations.internal.builtins.Radians;
import org.cytoscape.equations.internal.builtins.Right;
import org.cytoscape.equations.internal.builtins.Round;
import org.cytoscape.equations.internal.builtins.SList;
import org.cytoscape.equations.internal.builtins.Sign;
import org.cytoscape.equations.internal.builtins.Sin;
import org.cytoscape.equations.internal.builtins.Sinh;
import org.cytoscape.equations.internal.builtins.Sqrt;
import org.cytoscape.equations.internal.builtins.StDev;
import org.cytoscape.equations.internal.builtins.Substitute;
import org.cytoscape.equations.internal.builtins.Sum;
import org.cytoscape.equations.internal.builtins.Tan;
import org.cytoscape.equations.internal.builtins.Tanh;
import org.cytoscape.equations.internal.builtins.Text;
import org.cytoscape.equations.internal.builtins.Today;
import org.cytoscape.equations.internal.builtins.Trunc;
import org.cytoscape.equations.internal.builtins.Upper;
import org.cytoscape.equations.internal.builtins.Value;
import org.cytoscape.equations.internal.builtins.Var;
import org.cytoscape.equations.internal.parse_tree.BinOpNode;
import org.cytoscape.equations.internal.parse_tree.BooleanConstantNode;
import org.cytoscape.equations.internal.parse_tree.FConvNode;
import org.cytoscape.equations.internal.parse_tree.FloatConstantNode;
import org.cytoscape.equations.internal.parse_tree.FuncCallNode;
import org.cytoscape.equations.internal.parse_tree.IdentNode;
import org.cytoscape.equations.internal.parse_tree.SConvNode;
import org.cytoscape.equations.internal.parse_tree.StringConstantNode;
import org.cytoscape.equations.internal.parse_tree.UnaryOpNode;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class EquationParserImpl implements EquationParser {
	
	private static final Logger logger = LoggerFactory.getLogger(EquationParserImpl.class);

	private Tokeniser tokeniser;
	private Map<String, Function> nameToFunctionMap;
	private String lastErrorMessage;
	private TreeNode parseTree;
	private Map<String, Class<?>> variableNameToTypeMap;
	private Set<String> variableReferences;
	private Map<String, Object> defaultVariableValues;
	private Set<Function> registeredFunctions;
	
	private final CyServiceRegistrar serviceRegistrar;

	public EquationParserImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.nameToFunctionMap = new HashMap<>();
		this.registeredFunctions = new HashSet<>();
		this.parseTree = null;

		registerBuiltins();
	}
	
	@Deprecated
	@Override
	public void registerFunction(Function func) throws IllegalArgumentException {
		registerFunctionInternal(func);
		
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.addEventPayload(this, func, EquationFunctionAddedEvent.class);
	}

	public void registerFunctionInternal(final Function func) throws IllegalArgumentException {
		// Sanity check for the name of the function.
		final String funcName = func.getName().toUpperCase();
		if (funcName == null || funcName.equals(""))
			throw new IllegalArgumentException("empty or missing function name.");

		// Sanity check to catch duplicate function registrations.
		if (nameToFunctionMap.get(funcName) != null)
			throw new IllegalArgumentException("attempt at registering " + funcName + "() twice.");

		nameToFunctionMap.put(funcName, func);
		registeredFunctions.add(func);
	}

	@Override
	public Function getFunction(final String functionName) {
		return nameToFunctionMap.get(functionName);
	}

	@Override
	public Set<Function> getRegisteredFunctions() {
		return registeredFunctions;
	}

	@Override
	public boolean parse(final String formula, final Map<String, Class<?>> variableNameToTypeMap) {
		if (formula == null)
			throw new NullPointerException("formula string must not be null.");
		if (formula.length() < 1 || formula.charAt(0) != '=')
			throw new NullPointerException("0: formula string must start with an equal sign.");

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
					+ token + ".");
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

	@Override
	public Class<?> getType() {
		return parseTree == null ? null : parseTree.getType();
	}

	@Override
	public String getErrorMsg() {
		return lastErrorMessage;
	}

	@Override
	public Set<String> getVariableReferences() {
		return variableReferences;
	}

	@Override
	public Map<String, Object> getDefaultVariableValues() {
		return defaultVariableValues;
	}

	@Override
	public TreeNode getParseTree() {
		return parseTree;
	}

	//
	// The actual parsing takes place here.
	//

	/**
	 *   Implements expr --> term | term {+ term } | term {- term} | term {&amp; term} | term compOp term.
	 */
	private AbstractNode parseExpr() {
		AbstractNode exprNode = parseTerm();

		for (;;) {
			final Token token = tokeniser.getToken();
			final int sourceLocation = tokeniser.getStartPos();
			if (token == Token.PLUS || token == Token.MINUS || token == Token.AMPERSAND) {
				final TreeNode term = parseTerm();
				if (token == Token.PLUS || token == Token.MINUS)
					exprNode = handleBinaryArithmeticOp(token, sourceLocation, exprNode, term);
				else // String concatenation.
					exprNode = new BinOpNode(sourceLocation, Token.AMPERSAND, exprNode, term);
			}
			else if (token.isComparisonOperator()) {
				final TreeNode term = parseTerm();
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
	private AbstractNode parseTerm() {
		AbstractNode termNode = parsePower();

		for (;;) {
			final Token token = tokeniser.getToken();
			final int sourceLocation = tokeniser.getStartPos();
			if (token == Token.MUL || token == Token.DIV) {
				final TreeNode powerNode = parsePower();
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
	private AbstractNode parsePower() {
		AbstractNode powerNode = parseFactor();

		final Token token = tokeniser.getToken();
		final int sourceLocation = tokeniser.getStartPos();
		if (token == Token.CARET) {
			final TreeNode rhs = parsePower();
			powerNode = handleBinaryArithmeticOp(token, sourceLocation, powerNode, rhs);
		}
		else
			tokeniser.ungetToken(token);

		return powerNode;
	}

	/**
	 *  Implements factor --> constant | variable_ref | "(" expr ")" | ("-"|"+") factor  | func_call
	 */
	private AbstractNode parseFactor() {
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
				throw new IllegalStateException(sourceLocation + ": identifier expected.");

			final String ident = tokeniser.getIdent();
			final Class<?> varRefType = variableNameToTypeMap.get(ident);
			if (varRefType == null)
				throw new IllegalStateException(sourceLocation + ": unknown variable reference name: \""
				                                + ident + "\".");
			variableReferences.add(ident);

			Object defaultValue = null;
			if (usingOptionalBraces) {
				token = tokeniser.getToken();

				// Do we have a default value?
				if (token == Token.COLON) {
					token = tokeniser.getToken();
					sourceLocation = tokeniser.getStartPos();
					if (token != Token.FLOAT_CONSTANT && token != Token.STRING_CONSTANT && token != Token.BOOLEAN_CONSTANT)
						throw new IllegalStateException(sourceLocation + ": expected default value for variable reference.");
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
					throw new IllegalStateException(sourceLocation + ": closing brace expected.");

				defaultVariableValues.put(ident, defaultValue);
			}

			return new IdentNode(varRefStartPos, tokeniser.getIdent(), defaultValue, varRefType);
		}

		// 3. a parenthesised expression
		if (token == Token.OPEN_PAREN) {
			final AbstractNode exprNode = parseExpr();
			token = tokeniser.getToken();
			if (token != Token.CLOSE_PAREN)
				throw new IllegalStateException(sourceLocation + ": '(' expected.");

			return exprNode;
		}

		// 4. a unary operator
		if (token == Token.PLUS || token == Token.MINUS) {
			final TreeNode factor = parseFactor();
			return handleUnaryOp(sourceLocation, token, factor);
		}

		// 5. function call
		if (token == Token.IDENTIFIER) {
			tokeniser.ungetToken(token);
			return parseFunctionCall();
		}

		if (token == Token.ERROR)
			throw new IllegalStateException(sourceLocation + ": " + tokeniser.getErrorMsg());

		throw new IllegalStateException(sourceLocation + ": unexpected input token: " + token + ".");
	}

	private AbstractNode handleUnaryOp(final int sourceLocation, final Token operator, final TreeNode operand) {
		final Class<?> operandType = operand.getType();
		if (operandType == Boolean.class || operandType == String.class
		    || FunctionUtil.isTypeOfList(operandType))
			throw new ArithmeticException(sourceLocation + ": can't apply a unary " + operator.asString()
			                              + " a boolean, string or list operand.");
		if (operandType == Double.class)
			return new UnaryOpNode(sourceLocation, operator, operand);
		else
			return new UnaryOpNode(sourceLocation, operator, new FConvNode(operand));
	}

	/**
	 *   Implements func_call --> ident "(" ")" | ident "(" expr {"," expr} ")".
	 */
	private AbstractNode parseFunctionCall() {
		Token token = tokeniser.getToken();
		final int functionNameStartPos = tokeniser.getStartPos();
		if (token != Token.IDENTIFIER)
			throw new IllegalStateException(functionNameStartPos + ": function name expected.");

		final String originalName = tokeniser.getIdent();
		final String functionNameCandidate = originalName.toUpperCase();
		if (functionNameCandidate.equals("DEFINED"))
			return parseDefined();

		final Function func = nameToFunctionMap.get(functionNameCandidate);
		if (func == null) {
			if (tokeniser.getToken() == Token.OPEN_PAREN)
				throw new IllegalStateException(functionNameStartPos + ": call to unknown function "
								+ originalName + "().");
			else
				throw new IllegalStateException(functionNameStartPos + ": unknown text \""
								+ originalName + "\", maybe you forgot to put quotes around this text?");
		}

		token = tokeniser.getToken();
		final int openParenPos = tokeniser.getStartPos();
		if (token != Token.OPEN_PAREN)
			throw new IllegalStateException(openParenPos + ": expected '(' after function name \""
			                                + functionNameCandidate + "\".");

		// Parse the comma-separated argument list.
		final ArrayList<Class<?>> argTypes = new ArrayList<Class<?>>();
		ArrayList<AbstractNode> args = new ArrayList<AbstractNode>();
		int sourceLocation;
		for (;;) {
			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token ==  Token.CLOSE_PAREN)
				break;

			tokeniser.ungetToken(token);
			final AbstractNode exprNode = parseExpr();
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
			                                + functionNameCandidate + "().");

		if (token != Token.CLOSE_PAREN)
			throw new IllegalStateException(sourceLocation + ": expected the closing parenthesis of a call to "
			                                + functionNameCandidate + ".");

		AbstractNode[] nodeArray = new AbstractNode[args.size()];
		return new FuncCallNode(functionNameStartPos, func, returnType, args.toArray(nodeArray));
	}


	/**
	 *  Implements --> "DEFINED" "(" ident ")".  If the opening brace is found a closing brace is also required.
	 */
	private AbstractNode parseDefined() {
		Token token = tokeniser.getToken();
		final int definedStart = tokeniser.getStartPos();
		if (token != Token.OPEN_PAREN)
			throw new IllegalStateException(definedStart + ": \"(\" expected after \"DEFINED\".");

		token = tokeniser.getToken();
		int sourceLocation = tokeniser.getStartPos();
		Class<?> varRefType;
		if (token != Token.DOLLAR) {
			if (token != Token.IDENTIFIER)
				throw new IllegalStateException(sourceLocation + ": variable reference expected after \"DEFINED(\".");
			varRefType = variableNameToTypeMap.get(tokeniser.getIdent());
		}
		else {
			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token != Token.OPEN_BRACE)
				throw new IllegalStateException(sourceLocation + ": \"{\" expected after \"DEFINED($\".");

			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token != Token.IDENTIFIER)
				throw new IllegalStateException(sourceLocation + ": variable reference expected after \"DEFINED(${\".");

			varRefType = variableNameToTypeMap.get(tokeniser.getIdent());
			token = tokeniser.getToken();
			sourceLocation = tokeniser.getStartPos();
			if (token != Token.CLOSE_BRACE)
				throw new IllegalStateException(sourceLocation + ":\"}\" expected after after \"DEFINED(${"
				                                + tokeniser.getIdent() + "\".");
		}

		token = tokeniser.getToken();
		sourceLocation = tokeniser.getStartPos();
		if (token != Token.CLOSE_PAREN)
			throw new IllegalStateException(sourceLocation + ": missing \")\" in call to DEFINED().");

		return new BooleanConstantNode(definedStart, varRefType != null);
	}

	//
	// Helper functions for the parseXXX() methods.
	//

	/**
	 *  Deals w/ any necessary type conversions for any binary arithmetic operation on numbers.
	 */
	private AbstractNode handleBinaryArithmeticOp(final Token operator, final int sourceLocation, final TreeNode lhs, final TreeNode rhs) {
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
			                      + operator.asString() + "\". (lhs="
			                      + lhs.toString() + ":" + lhs.getType() + ", rhs="
			                      + rhs.toString() + ":" + rhs.getType() + ")");
	}

	/**
	 *  Deals w/ any necessary type conversions for any binary comparison operation.
	 */
	private AbstractNode handleComparisonOp(final Token operator, final int sourceLocation, final TreeNode lhs, final TreeNode rhs) {
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
			                           + operator.asString() + "\". (lhs="
			                           + lhs.toString() + ":" + lhs.getType() + ", rhs="
			                           + rhs.toString() + ":" + rhs.getType() + ")");
	}

	private void registerBuiltins() {
		registerFunctionInternal(new Abs());
		registerFunctionInternal(new ACos());
		registerFunctionInternal(new ASin());
		registerFunctionInternal(new And());
		registerFunctionInternal(new ATan2());
		registerFunctionInternal(new Average());
		registerFunctionInternal(new BList());
		registerFunctionInternal(new Combin());
		registerFunctionInternal(new Concatenate());
		registerFunctionInternal(new Cos());
		registerFunctionInternal(new Cosh());
		registerFunctionInternal(new Count());
		registerFunctionInternal(new Degrees());
		registerFunctionInternal(new org.cytoscape.equations.internal.builtins.Error());
		registerFunctionInternal(new Exp());
		registerFunctionInternal(new First());
		registerFunctionInternal(new FList());
		registerFunctionInternal(new GeoMean());
		registerFunctionInternal(new HarMean());
		registerFunctionInternal(new If());
		registerFunctionInternal(new IList());
		registerFunctionInternal(new Largest());
		registerFunctionInternal(new Last());
		registerFunctionInternal(new Left());
		registerFunctionInternal(new Len());
		registerFunctionInternal(new ListToString());
		registerFunctionInternal(new Ln());
		registerFunctionInternal(new Log());
		registerFunctionInternal(new Lower());
		registerFunctionInternal(new Max());
		registerFunctionInternal(new Median());
		registerFunctionInternal(new Mid());
		registerFunctionInternal(new Min());
		registerFunctionInternal(new Mod());
		registerFunctionInternal(new Mode());
		registerFunctionInternal(new Not());
		registerFunctionInternal(new NormDist());
		registerFunctionInternal(new Now());
		registerFunctionInternal(new Nth());
		registerFunctionInternal(new Or());
		registerFunctionInternal(new Permut());
		registerFunctionInternal(new Pi());
		registerFunctionInternal(new Product());
		registerFunctionInternal(new Radians());
		registerFunctionInternal(new Right());
		registerFunctionInternal(new Round());
		registerFunctionInternal(new Sign());
		registerFunctionInternal(new Sin());
		registerFunctionInternal(new Sinh());
		registerFunctionInternal(new SList());
		registerFunctionInternal(new StDev());
		registerFunctionInternal(new Sqrt());
		registerFunctionInternal(new Substitute());
		registerFunctionInternal(new Sum());
		registerFunctionInternal(new Tan());
		registerFunctionInternal(new Tanh());
		registerFunctionInternal(new Text());
		registerFunctionInternal(new Today());
		registerFunctionInternal(new Trunc());
		registerFunctionInternal(new Upper());
		registerFunctionInternal(new Value());
		registerFunctionInternal(new Var());
	}
	
	// Listeners for function services
	public void registerFunctionService(final Function function, final Map<?, ?> props) {
		if (function != null) {
			registerFunctionInternal(function);

			final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.addEventPayload(this, function, EquationFunctionAddedEvent.class);
			logger.info("New Function Registered: " + function.getName());
		}
	}
	
	public void unregisterFunctionService(final Function function, final Map<?, ?> props) {
		if (function != null) {
			registeredFunctions.remove(function);
			nameToFunctionMap.remove(function.getName().toUpperCase(), function);
			
			final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.addEventPayload(this, function, EquationFunctionRemovedEvent.class);
		}
	}
}
