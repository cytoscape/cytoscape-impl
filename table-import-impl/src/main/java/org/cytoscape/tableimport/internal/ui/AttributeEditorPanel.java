package org.cytoscape.tableimport.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.BACKSLASH;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.COLON;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.COMMA;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.SLASH;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING_LIST;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;


@SuppressWarnings("serial")
public class AttributeEditorPanel extends JPanel {

	private static final String OTHER = "Other:";

	private static final float ICON_FONT_SIZE = 14.0f;
	
	private JTextField attributeNameTextField;
	
	private final Map<SourceColumnSemantic, JToggleButton> typeButtons = new LinkedHashMap<>();
	private final Map<AttributeDataType, JToggleButton> dataTypeButtons = new LinkedHashMap<>();
	
	private JToggleButton stringButton;
	private JToggleButton booleanButton;
	private JToggleButton floatingPointButton;
	private JToggleButton integerButton;
	private JToggleButton stringListButton;
	private JToggleButton booleanListButton;
	private JToggleButton floatingPointListButton;
	private JToggleButton integerListButton;
	
	private JLabel listDelimiterLabel;
	private JComboBox<String> listDelimiterComboBox;
	private JTextField otherTextField;
	
	private ButtonGroup typeButtonGroup;
	private ButtonGroup dataTypeButtonGroup;
	
	private final String name;
	private final SourceColumnSemantic type;
	private final List<SourceColumnSemantic> availableTypes;
	private final AttributeDataType dataType;
	private final String listDelimiter;
	
	private final IconManager iconManager;

	public AttributeEditorPanel(
			final Window parent,
			final String name,
			final List<SourceColumnSemantic> availableTypes,
			final SourceColumnSemantic type,
			final AttributeDataType dataType,
			final String listDelimiter,
			final IconManager iconManager
	) {
		this.name = name;
		this.availableTypes = availableTypes;
		this.type = type;
		this.dataType = dataType;
		this.listDelimiter = listDelimiter;
		this.iconManager = iconManager;
		
		if (!availableTypes.contains(NONE))
			availableTypes.add(0, NONE);
		
		initComponents();
		updateComponents();
	}

	@Override
	public String getName() {
		return attributeNameTextField.getText().trim();
	}
	
	public SourceColumnSemantic getType() {
		final ButtonModel model = typeButtonGroup.getSelection();
		
		for (Entry<SourceColumnSemantic, JToggleButton> entry : typeButtons.entrySet()) {
			final JToggleButton btn = entry.getValue();
			
			if (btn.getModel().equals(model))
				return entry.getKey();
		}
		
		return NONE;
	}

	public AttributeDataType getDataType() {
		final ButtonModel model = dataTypeButtonGroup.getSelection();
		
		for (Entry<AttributeDataType, JToggleButton> entry : dataTypeButtons.entrySet()) {
			final JToggleButton btn = entry.getValue();
			
			if (btn.getModel().equals(model))
				return entry.getKey();
		}
		
		return TYPE_STRING;
	}
	
	public String getListDelimiterType() {
		if (isOtherDelimiterSelected())
			return otherTextField.getText().trim();

		if (listDelimiterComboBox.getSelectedItem().toString().equals("|"))
			return PIPE.toString();
		
		return listDelimiterComboBox.getSelectedItem().toString();
	}
	
	private void initComponents() {
		listDelimiterLabel = new JLabel("List Delimiter:");
		listDelimiterLabel.putClientProperty("JComponent.sizeVariant", "small");
		
		typeButtonGroup = new ButtonGroup();
		dataTypeButtonGroup = new ButtonGroup();

		attributeNameTextField = new JTextField(name);
		attributeNameTextField.setToolTipText("Column Name");
		attributeNameTextField.putClientProperty("JComponent.sizeVariant", "small");
		
		final List<JToggleButton> dataTypeBtnList = new ArrayList<>();
		
		dataTypeBtnList.add(stringButton = createDataTypeButton(TYPE_STRING));
		dataTypeBtnList.add(integerButton = createDataTypeButton(TYPE_INTEGER));
		dataTypeBtnList.add(floatingPointButton = createDataTypeButton(TYPE_FLOATING));
		dataTypeBtnList.add(booleanButton = createDataTypeButton(TYPE_BOOLEAN));
		dataTypeBtnList.add(stringListButton = createDataTypeButton(TYPE_STRING_LIST));
		dataTypeBtnList.add(integerListButton = createDataTypeButton(TYPE_INTEGER_LIST));
		dataTypeBtnList.add(floatingPointListButton = createDataTypeButton(TYPE_FLOATING_LIST));
		dataTypeBtnList.add(booleanListButton = createDataTypeButton(TYPE_BOOLEAN_LIST));
		
		setStyles(dataTypeBtnList);
		
		listDelimiterComboBox = new JComboBox<>();
		listDelimiterComboBox.putClientProperty("JComponent.sizeVariant", "small");
		listDelimiterComboBox.setModel(
				new DefaultComboBoxModel<String>(new String[] {
						"|",
                        COLON.toString(),
                        SLASH.toString(),
                        BACKSLASH.toString(),
                        COMMA.toString(),
                        OTHER
                    }));
		listDelimiterComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				otherTextField.setEnabled(isOtherDelimiterSelected());
			}
		});
		
		otherTextField = new JTextField();
		otherTextField.putClientProperty("JComponent.sizeVariant", "small");
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		final SequentialGroup typeHGroup = layout.createSequentialGroup();
		final ParallelGroup typeVGroup = layout.createParallelGroup(CENTER, false);
		
		for (SourceColumnSemantic type : availableTypes) {
			final JToggleButton btn = createTypeButton(type);
			typeHGroup.addComponent(btn, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			typeVGroup.addComponent(btn);
		}
		
		setStyles(new ArrayList<JToggleButton>(typeButtons.values()));
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(attributeNameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(typeHGroup)
				.addGroup(layout.createSequentialGroup()
						.addComponent(stringButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(integerButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(floatingPointButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(booleanButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(stringListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(integerListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(floatingPointListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(booleanListButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(listDelimiterLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(listDelimiterComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(otherTextField, 12, 36, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(attributeNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(typeVGroup)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(CENTER)
						.addComponent(stringButton)
						.addComponent(integerButton)
						.addComponent(floatingPointButton)
						.addComponent(booleanButton)
				)
				.addGroup(layout.createParallelGroup(CENTER)
						.addComponent(stringListButton)
						.addComponent(integerListButton)
						.addComponent(floatingPointListButton)
						.addComponent(booleanListButton)
						)
				.addGroup(layout.createSequentialGroup()
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(CENTER)
								.addComponent(listDelimiterLabel)
								.addComponent(listDelimiterComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(otherTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
				)
		);
	}

	private void setStyles(final List<JToggleButton> btnList) {
		if (LookAndFeelUtil.isAquaLAF()) {
			for (int i = 0; i < btnList.size(); i++) {
				final JToggleButton btn = btnList.get(i);
				btn.putClientProperty("JButton.buttonType", "segmentedGradient");
				btn.putClientProperty("JButton.segmentPosition", "only");
				btn.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		LookAndFeelUtil.equalizeSize(btnList.toArray(new JToggleButton[btnList.size()]));
	}
	
	private void updateComponents() {
		updateTypeButtonGroup();
		updateDataTypeButtonGroup();
		updateListDelimiterComboBox();
		updateOtherTextField();
	}

	private void updateTypeButtonGroup() {
		JToggleButton btn = typeButtons.get(type);
		
		if (btn == null)
			btn = typeButtons.get(NONE);
		if (btn != null)
			typeButtonGroup.setSelected(btn.getModel(), true);
	}
	
	private void updateDataTypeButtonGroup() {
		final JToggleButton button = dataTypeButtons.get(dataType);
		final ButtonModel model = button != null ? button.getModel() : null;
		
		if (model != null)
			dataTypeButtonGroup.setSelected(model, true);
	}
	
	private void updateListDelimiterComboBox() {
		listDelimiterLabel.setEnabled(dataType.isList());
		listDelimiterComboBox.setEnabled(dataType.isList());
		
		if (listDelimiter == null || listDelimiter.isEmpty()) {
			listDelimiterComboBox.setSelectedIndex(0);
		} else {
			for (int i = 0; i < listDelimiterComboBox.getItemCount(); i++) {
				if (listDelimiter.equals(listDelimiterComboBox.getItemAt(i))) {
					listDelimiterComboBox.setSelectedIndex(i);

					return;
				}
			}
			
			listDelimiterComboBox.setSelectedItem(OTHER);
		}
	}
	
	private void updateOtherTextField() {
		otherTextField.setEnabled(dataType.isList() && isOtherDelimiterSelected());
		
		if (listDelimiter != null && !listDelimiter.isEmpty() && isOtherDelimiterSelected())
			otherTextField.setText(listDelimiter);
	}
	
	private boolean isOtherDelimiterSelected() {
		return OTHER.equals(listDelimiterComboBox.getSelectedItem().toString());
	}

	private JToggleButton createTypeButton(final SourceColumnSemantic type) {
		final JToggleButton btn = new JToggleButton(type.getText());
		btn.setToolTipText(type.getDescription());
		btn.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
		btn.setForeground(type.getForeground());
		btn.setName(type.toString());
		
		typeButtonGroup.add(btn);
		typeButtons.put(type, btn);
		
		return btn;
	}
	
	private JToggleButton createDataTypeButton(final AttributeDataType dataType) {
		final JToggleButton btn = new JToggleButton(dataType.getText());
		btn.setToolTipText(dataType.getDescription());
		btn.setFont(new Font("Serif", Font.BOLD, 11));
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
			listDelimiterComboBox.setEnabled(isList);
			otherTextField.setEnabled(isList && isOtherDelimiterSelected());
		}
	}
}
