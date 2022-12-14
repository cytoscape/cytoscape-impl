package org.cytoscape.tableimport.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.TRAILING;
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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
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
	private JLabel attrNameWarningLabel;
	
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
	
	private JButton copyButton;
	private JButton pasteButton;
	
	private ButtonGroup typeButtonGroup;
	private ButtonGroup namespaceButtonGroup;
	private ButtonGroup dataTypeButtonGroup;
	
	private final String attrName;
	private SourceColumnSemantic attributeType;
	private String namespace = CyNetwork.DEFAULT_ATTRS;
	private final List<SourceColumnSemantic> availableTypes;
	private final List<String> availableNamespaces;
	private AttributeDataType attributeDataType;
	private String listDelimiter;
	
	private final IconManager iconManager;

	public AttributeEditorPanel(
			Window parent,
			String attrName,
			List<SourceColumnSemantic> availableTypes,
			List<String> availableNamespaces,
			SourceColumnSemantic attrType,
			String namespace,
			AttributeDataType attrDataType,
			String listDelimiter,
			IconManager iconManager
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
	
	public AttributeSettings getSettings() {
		return new AttributeSettings(
				getAttributeType(),
				getNamespace(),
				getAttributeDataType(),
				getListDelimiter()
		);
	}

	public void setSettings(AttributeSettings settings) {
		setAttributeType(settings.getAttrType());
		setNamespace(settings.getNamespace());
		setAttributeDataType(settings.getAttrDataType());
		setListDelimiter(settings.getListDelimiter());
		
		updateComponents();
	}

	public String getAttributeName() {
		return getAttributeNameTextField().getText().trim();
	}
	
	public SourceColumnSemantic getSelectedAttributeType() {
		var model = typeButtonGroup.getSelection();

		for (var entry : typeButtons.entrySet()) {
			var btn = entry.getValue();

			if (btn.getModel().equals(model))
				return entry.getKey();
		}

		return NONE;
	}

	public AttributeDataType getSelectedAttributeDataType() {
		var model = dataTypeButtonGroup.getSelection();

		for (var entry : dataTypeButtons.entrySet()) {
			var btn = entry.getValue();

			if (btn.getModel().equals(model))
				return entry.getKey();
		}

		return TYPE_STRING;
	}
	
	public String getListDelimiter() {
		if (isOtherDelimiterSelected())
			return getOtherTextField().getText();

		var label = getListDelimiterComboBox().getSelectedItem().toString();
		var del = TextDelimiter.getByLabel(label);
		
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
	
	public void setListDelimiter(String listDelimiter) {
		if (this.listDelimiter != listDelimiter)
			firePropertyChange("listDelimiter", this.listDelimiter, this.listDelimiter = listDelimiter);
	}
	
	private void initComponents() {
		listDelimiterLabel = new JLabel("List Delimiter:");
		listDelimiterLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		typeButtonGroup = new ButtonGroup();
		namespaceButtonGroup = new ButtonGroup();
		dataTypeButtonGroup = new ButtonGroup();

		var dataTypeBtnList = new ArrayList<JToggleButton>();
		
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
		
		var layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		var namespaceHGroup = layout.createSequentialGroup();
		var namespaceVGroup = layout.createParallelGroup(CENTER, false);
		
		if (availableNamespaces.size() > 1) {
			for (String ns : availableNamespaces) {
				var btn = createNamespaceButton(ns);
				namespaceHGroup.addComponent(btn, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				namespaceVGroup.addComponent(btn);
			}
		}
		
		var typeHGroup = layout.createSequentialGroup();
		var typeVGroup = layout.createParallelGroup(CENTER, false);
		
		for (var type : availableTypes) {
			var btn = createTypeButton(type);
			typeHGroup.addComponent(btn, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			typeVGroup.addComponent(btn);
		}
		
		setStyles(typeButtons.values().toArray(new JToggleButton[typeButtons.size()]));
		
		var typeLabel = new JLabel("Meaning:");
		typeLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		var dataTypeLabel = new JLabel("Data Type:");
		dataTypeLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		layout.setHorizontalGroup(layout.createParallelGroup(TRAILING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getCopyButton())
						.addComponent(getPasteButton())
				)
				.addComponent(getAttributeNameTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAttrNameWarningLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
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
				.addGroup(layout.createParallelGroup(CENTER)
						.addComponent(getCopyButton())
						.addComponent(getPasteButton())
				)
				.addComponent(getAttributeNameTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getAttrNameWarningLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
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
	
	private JLabel getAttrNameWarningLabel() {
		if (attrNameWarningLabel == null) {
			attrNameWarningLabel = new JLabel(" ");
			attrNameWarningLabel.setHorizontalAlignment(JLabel.CENTER);
			attrNameWarningLabel.setForeground(LookAndFeelUtil.getErrorColor());
			LookAndFeelUtil.makeSmall(attrNameWarningLabel);
		}
		
		return attrNameWarningLabel;
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
					updateAttrNameWarningLabel();
					updateTypeButtons();
					
					// Notify listeners that the column name has changed
					var oldValue = attrName;
					var newValue = getAttributeName();
					
					if (newValue.isBlank()) {
						// Just send the old value if the new one is blank
						oldValue = null;
						newValue = attrName;
					}
					
					newValue = newValue.isBlank() ? attrName : newValue;
					firePropertyChange("attributeName", oldValue, newValue);
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
			
			var renderer = listDelimiterComboBox.getRenderer();
			
			listDelimiterComboBox.setRenderer(new ListCellRenderer<String>() {
				@Override
				public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
						boolean isSelected, boolean cellHasFocus) {
					var c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					if (OTHER.equals(value) && c instanceof JComponent)
						((JComponent)c).setFont(((JComponent)c).getFont().deriveFont(Font.ITALIC));
					
					return c;
				}
			});
			
			listDelimiterComboBox.addActionListener(evt -> {
				boolean isOther = isOtherDelimiterSelected();
				getOtherTextField().setEnabled(isOther);
				
				if (!isOther || !getOtherTextField().getText().isEmpty())
					setListDelimiter(listDelimiter = getListDelimiter());
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
	
	JButton getCopyButton() {
		if (copyButton == null) {
			copyButton = createIconButton(IconManager.ICON_COPY, "Copy Settings");
		}
		
		return copyButton;
	}
	
	public JButton getPasteButton() {
		if (pasteButton == null) {
			pasteButton = createIconButton(IconManager.ICON_PASTE, "Paste Settings");
		}
		
		return pasteButton;
	}

	private void setStyles(JToggleButton... btnList) {
		if (LookAndFeelUtil.isAquaLAF()) {
			for (int i = 0; i < btnList.length; i++) {
				var btn = btnList[i];
				btn.putClientProperty("JButton.buttonType", "segmentedGradient");
				btn.putClientProperty("JButton.segmentPosition", "only");
				btn.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		LookAndFeelUtil.equalizeSize(btnList);
	}
	
	private void updateComponents() {
		updateAttrNameWarningLabel();
		updateTypeButtonGroup();
		updateDataTypeButtonGroup();
		updateListDelimiterComboBox();
		updateOtherTextField();
		updateTypeButtons();
		updateDataTypeButtons();
		updateNamespaceButtons();
	}
	
	private void updateAttrNameWarningLabel() {
		getAttrNameWarningLabel().setText(getAttributeName().isBlank() ? "The column name cannot be empty!" : " ");
	}

	private void updateTypeButtons() {
		var dataType = getSelectedAttributeDataType();
		var attrName = getAttributeName();

		for (var entry : typeButtons.entrySet()) {
			var type = entry.getKey();
			var btn = entry.getValue();
			
			if (attrName.isBlank())
				btn.setEnabled(type == NONE);
			else
				btn.setEnabled(TypeUtil.isValid(type, dataType));
			
			btn.setForeground(btn.isEnabled() ? type.getForeground() : UIManager.getColor("Button.disabledForeground"));
		}
	}
	
	private void updateDataTypeButtons() {
		var type = getSelectedAttributeType();

		for (var entry : dataTypeButtons.entrySet()) {
			var dataType = entry.getKey();
			var btn = entry.getValue();
			btn.setEnabled(TypeUtil.isValid(type, dataType));
		}
	}
	
	private void updateNamespaceButtons() {
		var type = getSelectedAttributeType();

		for (var entry : namespaceButtons.entrySet()) {
			var namespace = entry.getKey();
			var btn = entry.getValue();
			btn.setEnabled(TypeUtil.isValid(type, namespace));
			
			if (namespace.equals(this.namespace) && !btn.isEnabled())
				setNamespace(TypeUtil.getPreferredNamespace(type));
		}
		
		updateNamespaceButtonGroup();
	}
	
	private void updateTypeButtonGroup() {
		var btn = typeButtons.get(attributeType);

		if (btn == null)
			btn = typeButtons.get(NONE);
		if (btn != null)
			typeButtonGroup.setSelected(btn.getModel(), true);
	}
	
	private void updateDataTypeButtonGroup() {
		var button = dataTypeButtons.get(attributeDataType);
		var model = button != null ? button.getModel() : null;
		
		if (model != null)
			dataTypeButtonGroup.setSelected(model, true);
	}
	
	private void updateNamespaceButtonGroup() {
		var btn = namespaceButtons.get(namespace);

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
				var label = getListDelimiterComboBox().getItemAt(i);
				var del = TextDelimiter.getByLabel(label);
				
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

	private JToggleButton createTypeButton(SourceColumnSemantic type) {
		var btn = new JToggleButton(type.getText());
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
	
	private JButton createIconButton(String icon, String tooltip) {
		var button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		button.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		
		return button;
	}
	
	private JToggleButton createNamespaceButton(String namespace) {
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
		
		var btn = new JToggleButton(text);
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
	
	private JToggleButton createDataTypeButton(AttributeDataType dataType) {
		var btn = new JToggleButton(dataType.getText());
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
		
		DataTypeButtonActionListener(boolean isList) {
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
