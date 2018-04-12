package org.cytoscape.tableimport.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.BACKSLASH;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.COLON;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.COMMA;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.SLASH;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.SPACE;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.TAB;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING_LIST;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.tableimport.internal.reader.TextDelimiter;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.TypeUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;


@SuppressWarnings("serial")
public class AttributeEditorPanel extends JPanel {

	private static final String OTHER = "Other:";

	private static final float ICON_FONT_SIZE = 14.0f;
	
	private JTextField attributeNameTextField;
	
	private final Map<SourceColumnSemantic, JToggleButton> typeButtons = new LinkedHashMap<>();
	private final Map<AttributeDataType, JToggleButton> dataTypeButtons = new LinkedHashMap<>();
	private final Map<String, JToggleButton>namespaceButtons = new LinkedHashMap<>();
	
	private JToggleButton stringButton;
	private JToggleButton booleanButton;
	private JToggleButton floatingPointButton;
	private JToggleButton integerButton;
	private JToggleButton longButton;
	private JToggleButton stringListButton;
	private JToggleButton booleanListButton;
	private JToggleButton floatingPointListButton;
	private JToggleButton integerListButton;
	private JToggleButton longListButton;
	
	private JLabel listDelimiterLabel;
	private JComboBox<String> listDelimiterComboBox;
	private JTextField otherTextField;
	
	private ButtonGroup typeButtonGroup;
	private ButtonGroup namespaceButtonGroup;
	private ButtonGroup dataTypeButtonGroup;
	
	private String attrName;
	private SourceColumnSemantic attributeType;
	private String namespace = CyNetwork.DEFAULT_ATTRS;
	private final List<SourceColumnSemantic> availableTypes;
	private final List<String> availableNamespaces;
	private AttributeDataType attributeDataType;
	private String listDelimiter;
	
	private final IconManager iconManager;

	public AttributeEditorPanel(
			final Window parent,
			final String attrName,
			final List<SourceColumnSemantic> availableTypes,
			final List<String> availableNamespaces,
			final SourceColumnSemantic attrType,
			final String namespace,
			final AttributeDataType attrDataType,
			final String listDelimiter,
			final IconManager iconManager
	) {
		this.attrName = attrName;
		this.availableTypes = availableTypes;
		this.attributeType = attrType;
		this.availableNamespaces = availableNamespaces;
		this.namespace = namespace;
		this.attributeDataType = attrDataType;
		this.listDelimiter = listDelimiter;
		this.iconManager = iconManager;
		
		if (!availableTypes.contains(NONE))
			availableTypes.add(0, NONE);
		
		initComponents();
		updateComponents();
	}

	public String getAttributeName() {
		return getAttributeNameTextField().getText().trim();
	}
	
	public SourceColumnSemantic getSelectedAttributeType() {
		final ButtonModel model = typeButtonGroup.getSelection();

		for (Entry<SourceColumnSemantic, JToggleButton> entry : typeButtons.entrySet()) {
			final JToggleButton btn = entry.getValue();

			if (btn.getModel().equals(model))
				return entry.getKey();
		}

		return NONE;
	}

	public AttributeDataType getSelectedAttributeDataType() {
		final ButtonModel model = dataTypeButtonGroup.getSelection();

		for (Entry<AttributeDataType, JToggleButton> entry : dataTypeButtons.entrySet()) {
			final JToggleButton btn = entry.getValue();

			if (btn.getModel().equals(model))
				return entry.getKey();
		}

		return TYPE_STRING;
	}
	
	public String getListDelimiter() {
		if (isOtherDelimiterSelected())
			return getOtherTextField().getText();

		final String label = getListDelimiterComboBox().getSelectedItem().toString();
		final TextDelimiter del = TextDelimiter.getByLabel(label);
		
		return del != null ? del.getDelimiter() : null;
	}
	
	protected SourceColumnSemantic getAttributeType() {
		return attributeType;
	}
	
	private void setAttributeType(SourceColumnSemantic attributeType) {
		if (this.attributeType != attributeType)
			firePropertyChange("attributeType", this.attributeType, this.attributeType = attributeType);
	}
	
	protected String getNamespace() {
		return namespace;
	}
	
	private void setNamespace(String namespace) {
		if (this.namespace != namespace)
			firePropertyChange("namespace", this.namespace, this.namespace = namespace);
	}
	
	protected AttributeDataType getAttributeDataType() {
		return attributeDataType;
	}
	
	private void setAttributeDataType(AttributeDataType attributeDataType) {
		if (this.attributeDataType != attributeDataType)
			firePropertyChange("attributeDataType", this.attributeDataType, this.attributeDataType = attributeDataType);
	}
	
	private void initComponents() {
		listDelimiterLabel = new JLabel("List Delimiter:");
		listDelimiterLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		typeButtonGroup = new ButtonGroup();
		namespaceButtonGroup = new ButtonGroup();
		dataTypeButtonGroup = new ButtonGroup();

		final List<JToggleButton> dataTypeBtnList = new ArrayList<>();
		
		dataTypeBtnList.add(stringButton = createDataTypeButton(TYPE_STRING));
		dataTypeBtnList.add(integerButton = createDataTypeButton(TYPE_INTEGER));
		dataTypeBtnList.add(longButton = createDataTypeButton(TYPE_LONG));
		dataTypeBtnList.add(floatingPointButton = createDataTypeButton(TYPE_FLOATING));
		dataTypeBtnList.add(booleanButton = createDataTypeButton(TYPE_BOOLEAN));
		dataTypeBtnList.add(stringListButton = createDataTypeButton(TYPE_STRING_LIST));
		dataTypeBtnList.add(integerListButton = createDataTypeButton(TYPE_INTEGER_LIST));
		dataTypeBtnList.add(longListButton = createDataTypeButton(TYPE_LONG_LIST));
		dataTypeBtnList.add(floatingPointListButton = createDataTypeButton(TYPE_FLOATING_LIST));
		dataTypeBtnList.add(booleanListButton = createDataTypeButton(TYPE_BOOLEAN_LIST));
		
		setStyles(dataTypeBtnList.toArray(new JToggleButton[dataTypeBtnList.size()]));
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		final SequentialGroup namespaceHGroup = layout.createSequentialGroup();
		final ParallelGroup namespaceVGroup = layout.createParallelGroup(CENTER, false);
		
		if (availableNamespaces.size() > 1) {
			for (String ns : availableNamespaces) {
				final JToggleButton btn = createNamespaceButton(ns);
				namespaceHGroup.addComponent(btn, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				namespaceVGroup.addComponent(btn);
			}
		}
		
		final SequentialGroup typeHGroup = layout.createSequentialGroup();
		final ParallelGroup typeVGroup = layout.createParallelGroup(CENTER, false);
		
		for (SourceColumnSemantic type : availableTypes) {
			final JToggleButton btn = createTypeButton(type);
			typeHGroup.addComponent(btn, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			typeVGroup.addComponent(btn);
		}
		
		setStyles(typeButtons.values().toArray(new JToggleButton[typeButtons.size()]));
		
		final JLabel typeLabel = new JLabel("Meaning:");
		typeLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		final JLabel dataTypeLabel = new JLabel("Data Type:");
		dataTypeLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getAttributeNameTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(typeLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(typeHGroup)
				.addGroup(namespaceHGroup)
				.addComponent(dataTypeLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(stringButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(integerButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(longButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(floatingPointButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(booleanButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(stringListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(integerListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(longListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(floatingPointListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(booleanListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(listDelimiterLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getListDelimiterComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getOtherTextField(), 12, 36, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getAttributeNameTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(typeLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(typeVGroup)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(namespaceVGroup)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(dataTypeLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(CENTER)
						.addComponent(stringButton)
						.addComponent(integerButton)
						.addComponent(longButton)
						.addComponent(floatingPointButton)
						.addComponent(booleanButton)
				)
				.addGroup(layout.createParallelGroup(CENTER)
						.addComponent(stringListButton)
						.addComponent(integerListButton)
						.addComponent(longListButton)
						.addComponent(floatingPointListButton)
						.addComponent(booleanListButton)
						)
				.addGroup(layout.createSequentialGroup()
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(CENTER)
								.addComponent(listDelimiterLabel)
								.addComponent(getListDelimiterComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getOtherTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
				)
		);
	}
	
	protected JTextField getAttributeNameTextField() {
		if (attributeNameTextField == null) {
			attributeNameTextField = new JTextField(attrName);
			attributeNameTextField.setToolTipText("Column Name");
			attributeNameTextField.putClientProperty("JComponent.sizeVariant", "small");
			attributeNameTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					onTextChanged();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					onTextChanged();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					onTextChanged();
				}
				public void onTextChanged() {
					firePropertyChange("attributeName", attrName, attrName = getAttributeName());
				}
			});
		}
		
		return attributeNameTextField;
	}
	
	private JComboBox<String> getListDelimiterComboBox() {
		if (listDelimiterComboBox == null) {
			listDelimiterComboBox = new JComboBox<>();
			listDelimiterComboBox.putClientProperty("JComponent.sizeVariant", "small");
			listDelimiterComboBox.setModel(
					new DefaultComboBoxModel<>(new String[] {
							PIPE.toString(),
	                        COLON.toString(),
	                        SLASH.toString(),
	                        BACKSLASH.toString(),
	                        COMMA.toString(),
	                        SPACE.toString(),
	                        TAB.toString(),
	                        OTHER
	                    }));
			
			final ListCellRenderer<? super String> renderer = listDelimiterComboBox.getRenderer();
			
			listDelimiterComboBox.setRenderer(new ListCellRenderer<String>() {
				@Override
				public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
						boolean isSelected, boolean cellHasFocus) {
					final Component c =
							renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					if (OTHER.equals(value) && c instanceof JComponent)
						((JComponent)c).setFont(((JComponent)c).getFont().deriveFont(Font.ITALIC));
					
					return c;
				}
			});
			
			listDelimiterComboBox.addActionListener(evt -> {
				final boolean isOther = isOtherDelimiterSelected();
				getOtherTextField().setEnabled(isOther);
				
				if (!isOther || !getOtherTextField().getText().isEmpty())
					firePropertyChange("listDelimiter", listDelimiter, listDelimiter = getListDelimiter());
			});
		}
		
		return listDelimiterComboBox;
	}
	
	private JTextField getOtherTextField() {
		if (otherTextField == null) {
			otherTextField = new JTextField();
			otherTextField.putClientProperty("JComponent.sizeVariant", "small");
			otherTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					onTextChanged();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					onTextChanged();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					onTextChanged();
				}
				public void onTextChanged() {
					firePropertyChange("listDelimiter", listDelimiter, listDelimiter = getListDelimiter());
				}
			});
		}
		
		return otherTextField;
	}

	private void setStyles(JToggleButton... btnList) {
		if (LookAndFeelUtil.isAquaLAF()) {
			for (int i = 0; i < btnList.length; i++) {
				final JToggleButton btn = btnList[i];
				btn.putClientProperty("JButton.buttonType", "segmentedGradient");
				btn.putClientProperty("JButton.segmentPosition", "only");
				btn.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		LookAndFeelUtil.equalizeSize(btnList);
	}
	
	private void updateComponents() {
		updateTypeButtonGroup();
		updateDataTypeButtonGroup();
		updateListDelimiterComboBox();
		updateOtherTextField();
		updateTypeButtons();
		updateDataTypeButtons();
		updateNamespaceButtons();
	}
	
	private void updateTypeButtons() {
		final AttributeDataType dataType = getSelectedAttributeDataType();

		for (Entry<SourceColumnSemantic, JToggleButton> entry : typeButtons.entrySet()) {
			final SourceColumnSemantic type = entry.getKey();
			final JToggleButton btn = entry.getValue();
			btn.setEnabled(TypeUtil.isValid(type, dataType));
			btn.setForeground(btn.isEnabled() ? type.getForeground() : UIManager.getColor("Button.disabledForeground"));
		}
	}
	
	private void updateDataTypeButtons() {
		final SourceColumnSemantic type = getSelectedAttributeType();

		for (Entry<AttributeDataType, JToggleButton> entry : dataTypeButtons.entrySet()) {
			final AttributeDataType dataType = entry.getKey();
			final JToggleButton btn = entry.getValue();
			btn.setEnabled(TypeUtil.isValid(type, dataType));
		}
	}
	
	private void updateNamespaceButtons() {
		final SourceColumnSemantic type = getSelectedAttributeType();

		for (Entry<String, JToggleButton> entry : namespaceButtons.entrySet()) {
			final String namespace = entry.getKey();
			final JToggleButton btn = entry.getValue();
			btn.setEnabled(TypeUtil.isValid(type, namespace));
			
			if (namespace.equals(this.namespace) && !btn.isEnabled())
				setNamespace(TypeUtil.getPreferredNamespace(type));
		}
		
		updateNamespaceButtonGroup();
	}
	
	private void updateTypeButtonGroup() {
		JToggleButton btn = typeButtons.get(attributeType);

		if (btn == null)
			btn = typeButtons.get(NONE);
		if (btn != null)
			typeButtonGroup.setSelected(btn.getModel(), true);
	}
	
	private void updateDataTypeButtonGroup() {
		final JToggleButton button = dataTypeButtons.get(attributeDataType);
		final ButtonModel model = button != null ? button.getModel() : null;
		
		if (model != null)
			dataTypeButtonGroup.setSelected(model, true);
	}
	
	private void updateNamespaceButtonGroup() {
		JToggleButton btn = namespaceButtons.get(namespace);

		if (btn != null)
			namespaceButtonGroup.setSelected(btn.getModel(), true);
		else
			namespaceButtonGroup.clearSelection();
	}
	
	private void updateListDelimiterComboBox() {
		listDelimiterLabel.setEnabled(attributeDataType.isList());
		getListDelimiterComboBox().setEnabled(attributeDataType.isList());
		
		if (listDelimiter == null || listDelimiter.isEmpty()) {
			getListDelimiterComboBox().setSelectedIndex(0);
		} else {
			for (int i = 0; i < getListDelimiterComboBox().getItemCount(); i++) {
				final String label = getListDelimiterComboBox().getItemAt(i);
				final TextDelimiter del = TextDelimiter.getByLabel(label);
				
				if (del != null && listDelimiter.equals(del.getDelimiter())) {
					getListDelimiterComboBox().setSelectedIndex(i);

					return;
				}
			}
			
			getListDelimiterComboBox().setSelectedItem(OTHER);
		}
	}
	
	private void updateOtherTextField() {
		getOtherTextField().setEnabled(attributeDataType.isList() && isOtherDelimiterSelected());
		
		if (listDelimiter != null && !listDelimiter.isEmpty() && isOtherDelimiterSelected())
			getOtherTextField().setText(listDelimiter);
	}
	
	private boolean isOtherDelimiterSelected() {
		return OTHER.equals(getListDelimiterComboBox().getSelectedItem().toString());
	}

	private JToggleButton createTypeButton(final SourceColumnSemantic type) {
		final JToggleButton btn = new JToggleButton(type.getText());
		btn.setToolTipText(type.getDescription());
		btn.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		btn.setForeground(type.getForeground());
		btn.setName(type.toString());
		btn.addActionListener(evt -> {
			setNamespace(TypeUtil.getPreferredNamespace(type));
			updateNamespaceButtons();
			updateDataTypeButtons();
			setAttributeType(getSelectedAttributeType());
		});
		
		typeButtonGroup.add(btn);
		typeButtons.put(type, btn);
		
		return btn;
	}
	
	private JToggleButton createNamespaceButton(final String namespace) {
		String text = null;
		String toolTip = null;
		
		if (namespace.equals(CyNetwork.LOCAL_ATTRS)) {
			text = "Local";
			toolTip = "<html>The column will be created in the network's table</html>";
		} else if (namespace.equals(CyNetwork.DEFAULT_ATTRS)) {
			text = "Shared";
			toolTip = "<html>The column will be created in the network collection's table<br>and shared among all networks in the same collection</html>";
		} else if (namespace.equals(CyNetwork.HIDDEN_ATTRS)) {
			text = "Hidden";
			toolTip = "<html>The column will be created in the network's private table<br>and hidden from the user</html>";
		} else {
			text = namespace;
		}
		
		final JToggleButton btn = new JToggleButton(text);
		btn.setToolTipText(toolTip);
		btn.setName(namespace);
		btn.addActionListener(evt -> {
			// When the user selects another namespace for a regular attribute (not a PK, source, target...),
			// we save it so the same namespace is used again as the default one in future imports
			TypeUtil.setPreferredNamespace(namespace);
			setNamespace(namespace);
		});
		
		setStyles(btn);
		
		if (!LookAndFeelUtil.isAquaLAF())
			btn.setFont(btn.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
		namespaceButtonGroup.add(btn);
		namespaceButtons.put(namespace, btn);
		
		return btn;
	}
	
	private JToggleButton createDataTypeButton(final AttributeDataType dataType) {
		final JToggleButton btn = new JToggleButton(dataType.getText());
		btn.setToolTipText(dataType.getDescription());
		btn.setFont(new Font("Serif", Font.BOLD, 11)); // This font is used as an icon--Don't change it!
		btn.setName(dataType.toString());
		btn.addActionListener(new DataTypeButtonActionListener(dataType.isList()));
		
		dataTypeButtonGroup.add(btn);
		dataTypeButtons.put(dataType, btn);
		
		return btn;
	}
	
	private class DataTypeButtonActionListener implements ActionListener {

		final boolean isList;
		
		DataTypeButtonActionListener(final boolean isList) {
			this.isList = isList;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			listDelimiterLabel.setEnabled(isList);
			getListDelimiterComboBox().setEnabled(isList);
			getOtherTextField().setEnabled(isList && isOtherDelimiterSelected());
			updateTypeButtons();
			updateNamespaceButtonGroup();
			setAttributeDataType(getSelectedAttributeDataType());
		}
	}
}
