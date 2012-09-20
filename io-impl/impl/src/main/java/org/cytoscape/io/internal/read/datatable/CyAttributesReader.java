package org.cytoscape.io.internal.read.datatable;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyAttributesReader extends AbstractTask implements CyTableReader {
	private static final Logger logger = LoggerFactory.getLogger(CyAttributesReader.class);

	private static final byte TYPE_BOOLEAN = 1;
	private static final byte TYPE_FLOATING_POINT = 2;
	private static final byte TYPE_INTEGER = 3;
	private static final byte TYPE_STRING = 4;

	private static final String ENCODING_SCHEME = "UTF-8";
	private static final String DECODE_PROPERTY = "cytoscape.decode.attributes";

	private boolean badDecode;
	private int lineNum;
	private boolean doDecoding;

	private CyTableFactory tableFactory;;
	private InputStream inputStream;

	private CyTable[] cyTables;
	private final CyApplicationManager appMgr;
	private final CyNetworkManager netMgr;
	private final CyRootNetworkManager rootNetFact;
	
	private static int nextTableNumber = 1;

	public CyAttributesReader(final InputStream inputStream, final CyTableFactory tableFactory,
				  final CyApplicationManager appMgr, final CyNetworkManager netMgr, final CyRootNetworkManager rootNetFact)
	{
		lineNum = 0;
		doDecoding = Boolean.valueOf(System.getProperty(DECODE_PROPERTY, "true"));

		this.tableFactory = tableFactory;
		this.appMgr = appMgr;
		this.netMgr = netMgr;
		this.inputStream = inputStream;
		this.rootNetFact = rootNetFact;
	}

	@Override
	public void run(TaskMonitor tm) throws IOException {
		tm.setProgress(0.0);

		final CyTable table = tableFactory.createTable(
				"Table " + Integer.toString(nextTableNumber++), CyNetwork.NAME,
				String.class, true, true);
		cyTables = new CyTable[] { table };
		tm.setProgress(0.1);
		try {
			loadAttributesInternal(table);
			tm.setProgress(0.3);
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
		tm.setProgress(1.0);	
		
	}

	private void loadAttributesInternal(final CyTable table) throws IOException {
		final InputStreamReader reader1 = new InputStreamReader(this.inputStream);

		badDecode = false;
		boolean guessedAttrType = false; // We later set this to true if we have to guess the attribute type.

		try {
			final BufferedReader reader = new BufferedReader(reader1);

			String attributeName;
			byte type = -1;

			{
				final String firstLine = reader.readLine();
				lineNum++;

				if (firstLine == null) {
					return;
				}

				final String searchStr = "class=";
				final int inx = firstLine.indexOf(searchStr);

				if (inx < 0) {
					attributeName = firstLine.trim();
				} else {
					attributeName = firstLine.substring(0, inx - 1).trim();

					String foo = firstLine.substring(inx);
					final StringTokenizer tokens = new StringTokenizer(foo);
					foo = tokens.nextToken();

					String className = foo.substring(searchStr.length()).trim();

					if (className.endsWith(")")) {
						className = className.substring(0,
								className.length() - 1);
					}

					if (className.equalsIgnoreCase(String.class.toString())
							|| className.equalsIgnoreCase("String")) {
						type = TYPE_STRING;
					} else if (className.equalsIgnoreCase(Boolean.class.toString())
							|| className.equalsIgnoreCase("Boolean")) {
						type = TYPE_BOOLEAN;
					} else if (className.equalsIgnoreCase(Integer.class.toString())
							|| className.equalsIgnoreCase("Integer")) {
						type = TYPE_INTEGER;
					} else if (className.equalsIgnoreCase(Double.class.toString())
							|| className.equalsIgnoreCase("Double")
							|| className.equalsIgnoreCase(Float.class.toString())
							|| className.equalsIgnoreCase("Float")) {
						type = TYPE_FLOATING_POINT;
					}
					logger.debug("New Column Loaded.  Data Type = " + attributeName + ": " + type);
				}
			}

			if (attributeName.indexOf("(") >= 0) {
				attributeName = attributeName.substring(0, attributeName.indexOf("(")).trim();
			}

			boolean firstLineDefined = true;
			boolean list = false;

			while (true) {
				final String line = reader.readLine();
				lineNum++;

				if (line == null)
					break;

				// Empty line?
				if ("".equals(line.trim())) {
					continue;
				}

				int inx = line.indexOf('=');
				String key = line.substring(0, inx).trim();
				String val = line.substring(inx + 1).trim();
				final boolean equation = val.startsWith("=");

				key = decodeString(key);

				if (firstLineDefined && val.startsWith("("))
					list = true;

				if (list) {

					// Chop away leading '(' and trailing ')'.
					val = val.substring(1).trim();
					val = val.substring(0, val.length() - 1).trim();

					String[] elms = val.split("::");
					final List<Object> elmsBuff = new ArrayList<Object>();

					for (String vs : elms) {
						vs = decodeString(vs);
						vs = decodeSlashEscapes(vs);
						elmsBuff.add(vs);
					}

					if (firstLineDefined) {
						if (type < 0) {
							guessedAttrType = true;
							while (true) {
								try {
									new Integer((String) elmsBuff.get(0));
									type = TYPE_INTEGER;
									break;
								} catch (Exception e) {
								}

								try {
									new Double((String) elmsBuff.get(0));
									type = TYPE_FLOATING_POINT;
									break;
								} catch (Exception e) {
								}
								type = TYPE_STRING;
								break;
							}
						}

						firstLineDefined = false;
					}

					for (int i = 0; i < elmsBuff.size(); i++) {
						if (type == TYPE_INTEGER) {
							elmsBuff.set(i,
									new Integer((String) elmsBuff.get(i)));
						} else if (type == TYPE_BOOLEAN) {
							elmsBuff.set(i,
									new Boolean((String) elmsBuff.get(i)));
						} else if (type == TYPE_FLOATING_POINT) {
							elmsBuff.set(i,
									new Double((String) elmsBuff.get(i)));
						} else {
							// A string; do nothing.
						}
					}

					setListAttribute(table, type, key, attributeName, elmsBuff);
				} else { // Not a list.

					val = decodeString(val);
					val = decodeSlashEscapes(val);

					if (firstLineDefined) {
						if (type < 0) {
							guessedAttrType = true;
							while (true) {
								try {
									new Integer(val);
									type = TYPE_INTEGER;
									break;
								} catch (Exception e) {
								}

								try {
									new Double(val);
									type = TYPE_FLOATING_POINT;
									break;
								} catch (Exception e) {
								}

								type = TYPE_STRING;
								break;
							}
						}

						firstLineDefined = false;
					}

					setAttributeForType(table, type, key, attributeName, val);
				}
			}
		} catch (Exception e) {
			String message;
			if (guessedAttrType) {
				message = "failed parsing data table file at line: "
						+ lineNum
						+ " with exception: "
						+ e.getMessage()
						+ " This is most likely due to a missing attribute type on the first line.\n"
						+ "Column type should be one of the following: "
						+ "(class=String), (class=Boolean), (class=Integer), or (class=Double). "
						+ "(\"Double\" stands for a floating point a.k.a. \"decimal\" number.)"
						+ " This should be added to end of the first line.";
			} else
				message = "failed parsing data table file at line: " + lineNum
						+ " with exception: " + e.getMessage();
			logger.warn(message, e);
			throw new IOException(message);
		}
	}

	private void setAttributeForType(CyTable tbl, byte type, String key,
			String attributeName, String val) {
		if (tbl.getColumn(attributeName) == null) {
			if (type == TYPE_INTEGER)
				tbl.createColumn(attributeName, Integer.class, false);
			else if (type == TYPE_BOOLEAN)
				tbl.createColumn(attributeName, Boolean.class, false);
			else if (type == TYPE_FLOATING_POINT)
				tbl.createColumn(attributeName, Double.class, false);
			else
				// type is String
				tbl.createColumn(attributeName, String.class, false);
		}

		CyRow row = tbl.getRow(key);

		if (type == TYPE_INTEGER)
			row.set(attributeName, new Integer(val));
		else if (type == TYPE_BOOLEAN)
			row.set(attributeName, new Boolean(val));
		else if (type == TYPE_FLOATING_POINT)
			row.set(attributeName, (new Double(val)));
		else
			// type is String
			row.set(attributeName, new String(val));
	}

	private void setListAttribute(CyTable tbl, Byte type, String key,
			String attributeName, final List<?> elmsBuff) {
		if (tbl.getColumn(attributeName) == null) {
			if (type == TYPE_INTEGER)
				tbl.createListColumn(attributeName, Integer.class, false);
			else if (type == TYPE_BOOLEAN)
				tbl.createListColumn(attributeName, Boolean.class, false);
			else if (type == TYPE_FLOATING_POINT)
				tbl.createListColumn(attributeName, Double.class, false);
			else
				// type is String, do nothing
				/* Intentionally empty. */;
		}
		CyRow row = tbl.getRow(key);
		row.set(attributeName, elmsBuff);
	}

	private String decodeString(String in) throws IOException {
		if (doDecoding) {
			try {
				in = URLDecoder.decode(in, ENCODING_SCHEME);
			} catch (IllegalArgumentException iae) {
				if (!badDecode) {
					// logger.info(MessageFormat.format(badDecodeMessage,
					// lineNum), iae);
					badDecode = true;
				}
			}
		}

		return in;
	}

	private static String decodeSlashEscapes(String in) {
		final StringBuilder elmBuff = new StringBuilder();
		int inx2;

		for (inx2 = 0; inx2 < in.length(); inx2++) {
			char ch = in.charAt(inx2);

			if (ch == '\\') {
				if ((inx2 + 1) < in.length()) {
					inx2++;

					char ch2 = in.charAt(inx2);

					if (ch2 == 'n') {
						elmBuff.append('\n');
					} else if (ch2 == 't') {
						elmBuff.append('\t');
					} else if (ch2 == 'b') {
						elmBuff.append('\b');
					} else if (ch2 == 'r') {
						elmBuff.append('\r');
					} else if (ch2 == 'f') {
						elmBuff.append('\f');
					} else {
						elmBuff.append(ch2);
					}
				} else {
					/* val ends in '\' - just ignore it. */}
			} else {
				elmBuff.append(ch);
			}
		}

		return elmBuff.toString();
	}

	public boolean isDoDecoding() {
		return doDecoding;
	}

	public void setDoDecoding(boolean doDec) {
		doDecoding = doDec;
	}

	@Override
	public CyTable[] getTables() {
		return cyTables;
	}
}
