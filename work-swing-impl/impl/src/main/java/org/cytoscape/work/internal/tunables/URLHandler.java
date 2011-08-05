package org.cytoscape.work.internal.tunables;


import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ToolTipManager;

import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.bookmark.Category;
import org.cytoscape.property.bookmark.DataSource;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>URL</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class URLHandler extends AbstractGUITunableHandler {
	
	private BookmarksUtil bkUtil;
	private Bookmarks theBookmarks;
	private final String bookmarkCategory;
	private BookmarkComboBoxEditor bookmarkEditor;
	private JComboBox networkFileComboBox;
	private JLabel titleLabel;
	private JSeparator titleSeparator;
	private String pleaseMessage = "Please provide URL or select from list";
	private GroupLayout layout;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>URL</code> type
	 *
	 * It creates the GUI which displays a field to enter a URL, and a combobox which contains different registered URL with their description
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public URLHandler(Field f, Object o, Tunable t, Bookmarks bookmarks, BookmarksUtil bkUtil) {
		super(f, o, t);
		final Properties props = getParams();
		bookmarkCategory = props.getProperty("fileCategory");
		init(bookmarks, bkUtil);
	}


	public URLHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable,
			final Bookmarks bookmarks, final BookmarksUtil bkUtil) {
		super(getter, setter, instance, tunable);
		final Properties props = getParams();
		bookmarkCategory = props.getProperty("fileCategory");
		init(bookmarks, bkUtil);
	}

	private void init(final Bookmarks bookmarks, final BookmarksUtil bkUtil) {
		this.bkUtil = bkUtil;
		this.theBookmarks = bookmarks;

		//creation of the GUI and layout
		setGUI();
		setLayout();
		panel.setLayout(layout);

		Category theCategory = bkUtil.getCategory(bookmarkCategory,bookmarks.getCategory());
		if (theCategory == null) {
			theCategory = new Category();
			theCategory.setName(bookmarkCategory);

			List<Category> theCategoryList = bookmarks.getCategory();
			theCategoryList.add(theCategory);
		}

		loadBookmarkCMBox();
	}

	/**
	 * Set the url typed in the field, or choosen from the combobox to the object <code>URL</code> <code>o</code>
	 */
	public void handle() {
		final String urlString = bookmarkEditor.getURLstr();
		try {
			if (urlString != null) {
				try {
					setValue(new URL(urlString));
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} catch (final Exception e){
			e.printStackTrace();
		}
	}

	//Creation of the GUI :
	//	-field that displays the URL or the bookmark
	//	-combobox to choose a bookmark previously registered

	// Tooltips to inform the user are also provided on the combobox
	private void setGUI(){
		//adding tooltips to panel components
		final ToolTipManager tipManager = ToolTipManager.sharedInstance();
		tipManager.setInitialDelay(1);
		tipManager.setDismissDelay(7500);

		bookmarkEditor = new BookmarkComboBoxEditor();
		bookmarkEditor.setStr(pleaseMessage);
		titleSeparator = new JSeparator();
		titleLabel = new JLabel("Import URL file");

		networkFileComboBox = new JComboBox();
		networkFileComboBox.setRenderer(new MyCellRenderer());
		networkFileComboBox.setEditor(bookmarkEditor);
		networkFileComboBox.setEditable(true);
		networkFileComboBox.setName("networkFileComboBox");
		networkFileComboBox.setToolTipText("<html><body>You can specify URL by the following:<ul><li>Type URL</li><li>Select from pull down menu</li><li>Drag & Drop URL from Web Browser</li></ul></body><html>");
	}

	//diplays the panel's component in a good view
	private void setLayout(){
		layout = new GroupLayout(panel);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					  .addGroup(
						    layout.createSequentialGroup()
						    .addContainerGap()
						    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							      .addComponent(networkFileComboBox,0, 350,Short.MAX_VALUE)
							      .addComponent(titleLabel,GroupLayout.PREFERRED_SIZE,350,GroupLayout.PREFERRED_SIZE)
							      .addComponent(titleSeparator,GroupLayout.DEFAULT_SIZE,350,Short.MAX_VALUE)
							      )
						    .addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(
						  layout.createSequentialGroup()
						  .addContainerGap()
						  .addComponent(titleLabel)
						  .addGap(8, 8, 8)
						  .addComponent(titleSeparator,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
						  .addGap(7, 7, 7)
						  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						  .addComponent(networkFileComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
						  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,3, Short.MAX_VALUE)
						  .addContainerGap()));

	}

	//add the URL entries (with their bookmarks) from an external file to the combobox
	private void loadBookmarkCMBox() {
		networkFileComboBox.removeAllItems();
		DefaultComboBoxModel theModel = new DefaultComboBoxModel();

		DataSource firstDataSource = new DataSource();
		firstDataSource.setName("");
		firstDataSource.setHref(null);

		theModel.addElement(firstDataSource);

		// Extract the URL entries
		List<DataSource> theDataSourceList = bkUtil.getDataSourceList(bookmarkCategory,theBookmarks.getCategory());
		if (theDataSourceList != null) {
			for (int i = 0; i < theDataSourceList.size(); i++)
				theModel.addElement(theDataSourceList.get(i));
		}
		networkFileComboBox.setModel(theModel);
	}

	private class BookmarkComboBoxEditor implements ComboBoxEditor {
		DataSource theDataSource = new DataSource();
		JTextField tfInput = new JTextField(pleaseMessage);

		public String getURLstr() {
			return tfInput.getText();
		}

		public void setStr(String txt){
			tfInput.setText(txt);
		}

		public void addActionListener(ActionListener l) {
			tfInput.addActionListener(l);
		}

		public void addKeyListener(KeyListener l) {
			tfInput.addKeyListener(l);
		}

		public Component getEditorComponent() {
			return tfInput;
		}

		public Object getItem() {
			return theDataSource;
		}

		public void removeActionListener(ActionListener l) {
		}

		public void selectAll() {
		}

		public void setItem(Object anObject) {
			if (anObject instanceof DataSource) {
				theDataSource = (DataSource) anObject;
				tfInput.setText(theDataSource.getHref());
			}
		}
	}

	private class MyCellRenderer extends JLabel implements ListCellRenderer {
		private final static long serialVersionUID = 1202339872997986L;
		public MyCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			DataSource dataSource = (DataSource) value;
			setText(dataSource.getName());
			if (isSelected) {
				if (0 < index) {
					list.setToolTipText(dataSource.getHref());
				}
			}
			return this;
		}
	}
}
