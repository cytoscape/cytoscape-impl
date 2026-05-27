package org.cytoscape.view.table.internal.impl;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;

/**
 * Clipboard helper for a JTable that preserves cell-grid structure on copy/paste.
 *
 * <p>The core idea mirrors how spreadsheets (Excel, Google Sheets) work: a copy
 * publishes the same selection in MORE THAN ONE clipboard flavor, and a paste
 * reads the richest flavor available.
 *
 * <ul>
 *   <li>{@link #GRID_FLAVOR} — an in-JVM object reference carrying the exact
 *       {@code List<List<String>>}. Used for paste WITHIN this app: perfect
 *       fidelity, no parsing, so a single cell with internal line breaks stays
 *       a single cell.</li>
 *   <li>{@link DataFlavor#stringFlavor} — tab-separated columns, newline-separated
 *       rows, with CSV-style quoting. Used for external apps (text editors,
 *       real spreadsheets) and as the fallback parser. The quoting is what
 *       disambiguates a multi-line cell ("a\nb") from multiple rows (a\nb).</li>
 * </ul>
 */
public final class GridClipboard {

	/**
	 * In-JVM structured flavor: the exact grid object, no serialization ambiguity.
	 */
	public static final DataFlavor GRID_FLAVOR = makeGridFlavor();

	private GridClipboard() {}

	private static DataFlavor makeGridFlavor() {
		return new DataFlavor(
				DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.List",
				"Cell grid (List<List<String>>)"
		);
	}

	// ---------------------------------------------------------------------
	// Wiring: install Ctrl/Cmd+C and Ctrl/Cmd+V on a table
	// ---------------------------------------------------------------------

	/**
	 * Registers structure-preserving copy/paste on the given table.
	 */
	public static void installCopyPaste(JTable table) {
		InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = table.getActionMap();
		int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuMask), "gridCopy");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask), "gridPaste");

		am.put("gridCopy", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copy(table);
			}
		});
		am.put("gridPaste", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paste(table);
			}
		});
	}

	// ---------------------------------------------------------------------
	// Copy
	// ---------------------------------------------------------------------

	public static void copy(JTable table) {
		int[] rows = table.getSelectedRows();
		int[] cols = table.getSelectedColumns();
		
		if (rows.length == 0 || cols.length == 0)
			return;

		List<List<String>> grid = new ArrayList<>(rows.length);
		
		for (int r : rows) {
			List<String> rowData = new ArrayList<>(cols.length);
			
			for (int c : cols) {
				Object value = table.getValueAt(r, c);
				
				if (value instanceof ValidatedObjectAndEditString vo)
				    value = vo.getEditString();   // or vo.getValidatedObject()
				
				rowData.add(value == null ? "" : value.toString());
			}
			
			grid.add(rowData);
		}

		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(toTransferable(grid), null);
	}

	public static Transferable toTransferable(List<List<String>> grid) {
		return new GridTransferable(grid);
	}

	// ---------------------------------------------------------------------
	// Paste
	// ---------------------------------------------------------------------

	public static void paste(JTable table) {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		List<List<String>> grid = fromTransferable(cb.getContents(null));
		
		if (grid == null || grid.isEmpty())
			return;

		int startRow = Math.max(0, table.getSelectedRow());
		int startCol = Math.max(0, table.getSelectedColumn());

		for (int r = 0; r < grid.size(); r++) {
			int tr = startRow + r;
			
			if (tr >= table.getRowCount())
				break; // app-specific: grow the model here if you want auto-expand

			List<String> rowData = grid.get(r);
			
			for (int c = 0; c < rowData.size(); c++) {
				int tc = startCol + c;
				if (tc >= table.getColumnCount())
					break;
				if (table.isCellEditable(tr, tc))
					table.setValueAt(rowData.get(c), tr, tc);
			}
		}
	}

	/**
	 * Resolve a clipboard Transferable to a grid, preferring structure over plain text.
	 */
	@SuppressWarnings("unchecked")
	public static List<List<String>> fromTransferable(Transferable t) {
		if (t == null)
			return null;

		// 1. Best case: our own in-app structured flavor. Zero ambiguity.
		if (GRID_FLAVOR != null && t.isDataFlavorSupported(GRID_FLAVOR)) {
			try {
				Object data = t.getTransferData(GRID_FLAVOR);
				if (data instanceof List)
					return deepCopy((List<List<String>>) data);
			} catch (Exception ignore) {
				// fall through to plain text
			}
		}

		// 2. Fallback: plain text parsed with CSV/TSV quoting rules.
		try {
			if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String s = (String) t.getTransferData(DataFlavor.stringFlavor);
				return parseDelimited(s, '\t');
			}
		} catch (Exception ignore) {
			// nothing usable
		}
		return null;
	}

    // ---------------------------------------------------------------------
    // Plain-text encoding (TSV with CSV-style quoting)
    // ---------------------------------------------------------------------

	public static String toPlainText(List<List<String>> grid) {
		StringBuilder sb = new StringBuilder();
		
		for (int r = 0; r < grid.size(); r++) {
			List<String> row = grid.get(r);
			
			for (int c = 0; c < row.size(); c++) {
				if (c > 0)
					sb.append('\t');
				sb.append(quoteIfNeeded(row.get(c)));
			}
			
			if (r < grid.size() - 1)
				sb.append('\n');
		}
		
		return sb.toString();
	}

	private static String quoteIfNeeded(String value) {
		if (value == null)
			value = "";
		
		boolean needs = value.indexOf('\t') >= 0
				|| value.indexOf('\n') >= 0
				|| value.indexOf('\r') >= 0
				|| value.indexOf('"') >= 0;
		
		if (!needs)
			return value;
		
		return '"' + value.replace("\"", "\"\"") + '"';
	}

	/**
	 * Parse delimited text where a field MAY be double-quoted. A quoted field can
	 * contain the delimiter, CR and LF as literal content; an embedded quote is
	 * written as {@code ""}. Unquoted line breaks (LF / CR / CRLF) separate rows.
	 *
	 * <p>This is the rule that tells {@code "a\nb"} (one cell) apart from {@code a\nb}
	 * (two rows).
	 */
	public static List<List<String>> parseDelimited(String text, char delim) {
		List<List<String>> rows = new ArrayList<>();
		List<String> row = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		boolean inQuotes = false;
		boolean fieldWasQuoted = false;

		int i = 0, n = text.length();
		while (i < n) {
			char ch = text.charAt(i);

			if (inQuotes) {
				if (ch == '"') {
					if (i + 1 < n && text.charAt(i + 1) == '"') {
						field.append('"'); // escaped quote ""
						i += 2;
					} else {
						inQuotes = false; // closing quote
						i++;
					}
				} else {
					field.append(ch); // tab/newline here are literal content
					i++;
				}
			} else if (ch == '"' && field.length() == 0) {
				inQuotes = true;
				fieldWasQuoted = true;
				i++;
			} else if (ch == delim) {
				row.add(field.toString());
				field.setLength(0);
				fieldWasQuoted = false;
				i++;
			} else if (ch == '\r') {
				endRecord(rows, row, field);
				row = new ArrayList<>();
				field.setLength(0);
				fieldWasQuoted = false;
				i++;
				if (i < n && text.charAt(i) == '\n') i++; // swallow CRLF
			} else if (ch == '\n') {
				endRecord(rows, row, field);
				row = new ArrayList<>();
				field.setLength(0);
				fieldWasQuoted = false;
				i++;
			} else {
				field.append(ch);
				i++;
			}
		}

		// Trailing field/row — but don't emit a spurious empty row from a trailing line break.
		if (field.length() > 0 || !row.isEmpty() || fieldWasQuoted) {
			row.add(field.toString());
			rows.add(row);
		}
		return rows;
	}

	private static void endRecord(List<List<String>> rows, List<String> row, StringBuilder field) {
		row.add(field.toString());
		rows.add(row);
	}

	// ---------------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------------

	private static List<List<String>> deepCopy(List<List<String>> src) {
		List<List<String>> out = new ArrayList<>(src.size());
		for (List<String> r : src)
			out.add(new ArrayList<>(r));
		return out;
	}

	private static final class GridTransferable implements Transferable {
		private final List<List<String>> grid;
		private final String plain;
		private final DataFlavor[] flavors;

		GridTransferable(List<List<String>> grid) {
			this.grid = deepCopy(grid);
			this.plain = toPlainText(grid);
			this.flavors = (GRID_FLAVOR != null)
					? new DataFlavor[] { GRID_FLAVOR, DataFlavor.stringFlavor }
					: new DataFlavor[] { DataFlavor.stringFlavor };
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors.clone();
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor f) {
			for (DataFlavor x : flavors)
				if (x.equals(f))
					return true;
			
			return false;
		}

		@Override
		public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException {
			if (GRID_FLAVOR != null && GRID_FLAVOR.equals(f))
				return deepCopy(grid);
			if (DataFlavor.stringFlavor.equals(f))
				return plain;
			
			throw new UnsupportedFlavorException(f);
		}
	}
}