package org.cytoscape.internal.prefs;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.SeparatorUI;

import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.internal.prefs.lib.DialogFooter;
import org.cytoscape.internal.prefs.lib.FontAwesomeIcon;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.VBox;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;

abstract public class PreferenceContainer extends JPanel  implements ActionListener//implements PrefsStringDefs 
{
	final JDialog dialog;
	static String homeIcon = "\uf015;";
	static String leftArrow = "\uf060;";
	static String rtArrow = "\uf061;";
	static String TEXT_MODE = "Text Mode";
	static String TABULAR = "Show All";
	static String GUI = "GUI";
	final CyServiceRegistrar serviceRegistrar;
	boolean advanced = false;

	public PreferenceContainer(JDialog dlog, CyServiceRegistrar reg) 
	{
		super();
		serviceRegistrar = reg;
		dialog = dlog;
//		WindowDragger g = new WindowDragger(header);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		homeButton.addActionListener(e -> { showHome(); });	
		AntiAliasedPanel.setSizes(homeButton, new Dimension(36, 36));
		Icon home = new FontAwesomeIcon(homeIcon.charAt(0), 18);
		homeButton.setVisible(true);
		homeButton.setIcon(home);

		leftButton.addActionListener(e -> { showPrevious(); });	
		AntiAliasedPanel.setSizes(leftButton, new Dimension(36,36));
		Icon lft = new FontAwesomeIcon(leftArrow.charAt(0), 14);
		leftButton.setVisible(true);
		leftButton.setIcon(lft);

		rightButton.addActionListener(e -> { showNext(); });	
		AntiAliasedPanel.setSizes(rightButton, new Dimension(36,36));
		rightButton.setVisible(true);
		Icon rt = new FontAwesomeIcon(rtArrow.charAt(0), 14);
		rightButton.setIcon(rt);

		JLabel spacer = new JLabel("");
		AntiAliasedPanel.setSizes(spacer, new Dimension(60,24));
		header.add(Box.createHorizontalGlue());
		
		AntiAliasedPanel.setSizes(modeTabPane, new Dimension(180,28));
		modeTabPane.addTab("Interactive",new JPanel());
		modeTabPane.addTab("Advanced",new JPanel());
		modeTabPane.setFont(new Font("Dialog", 0, 10));
		modeTabPane.addChangeListener(new ChangeListener()
				{

					@Override
					public void stateChanged(ChangeEvent e) {
						extract();
						advanced = modeTabPane.getSelectedIndex() == 0;
						if (advanced)  showHome();
						else  showTextPage(); 
//						System.out.println("State Changed: " + modeTabPane.getSelectedIndex());
						
					}
			
				});
		
		header.add(modeTabPane);
		
		if(isMac) {
			header.setUI(new GradientHeaderUI());
			separator.setForeground(new Color(64,64,64));
		}
		if(isMac) {
		separator.setForeground(new Color(64,64,64));
	}
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	//	dialog.setResizable(true);			// RESIZABLE
		VBox page = new VBox();
		add(page);
		page.add(header);
		page.add(contentsPanel);
		page.add(Box.createRigidArea(new Dimension(3,5)));
		page.add(footer);
		page.add(Box.createRigidArea(new Dimension(3,5)));
	
//		dialog.setContentPane(this);
//		InputMap inputMap = getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW);
//		ActionMap actionMap = getRootPane().getActionMap();
//		
//		inputMap.put(escapeKey, escapeKey);
//		inputMap.put(commandPeriodKey, escapeKey);
//		actionMap.put(escapeKey,closeDialogAndDisposeAction);
//		
		dialog.pack();
		dialog.setLocationByPlatform(true);
		showHome();
	}
	private static KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	private static KeyStroke commandPeriodKey = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	private static final long serialVersionUID = 1L;
	protected static boolean isMac = System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1;

	static int NPREFS = 15; 

	protected ActionListener buttonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton)e.getSource();
			show( button.getText());
		}
	};

	private final JSeparator separator = new JSeparator();
	protected final JPanel contentsPanel = new JPanel(new CardLayout());
	
	private final ArrayList<Row> rows = new ArrayList<Row>();
//	protected  JPanel homePanel;
//	public JPanel getHomePanel()	{ return homePanel;	}
	
	final protected JButton leftButton = new JButton("");
	final protected JButton rightButton = new JButton("");
	final protected JButton homeButton = new JButton("");
	final protected JTabbedPane modeTabPane = new JTabbedPane(JTabbedPane.TOP);
	public JButton getHomeButton() {  return homeButton; }
	public void HomeActionPerformed() {		showHome();			adjust();	}	
	public void NextActionPerformed() {		showNext();			adjust(); }
	public void PrevActionPerformed() {		showPrevious();		adjust(); }

	private void adjust()
	{
		String selectedPanel = getTopCardName();
		boolean advanced = "Advanced".equals(selectedPanel);
		modeTabPane.setSelectedIndex(advanced ? 1 : 0);
		boolean atHome = "".equals(selectedPanel);
		boolean inTabularView = "Advanced".equals(selectedPanel);
		leftButton.setEnabled(!atHome);	
		boolean enableNext = true;
		if (atHome || inTabularView)		enableNext = false;
		rightButton.setEnabled(enableNext);		
		
		boolean enablePrev = true;
		if (atHome || inTabularView) 	enablePrev = false;
		leftButton.setEnabled(enablePrev);		
		
		homeButton.setEnabled(!atHome);		
		setDialogTitle();
	}
	
//	public JDialog showDlog()
//	{
//		if(isMac) {
//			separator.setForeground(new Color(64,64,64));
//		}
//		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
////		dialog.setResizable(true);			// RESIZABLE
//		VBox page = new VBox();
//		add(page);
//		page.add(header);
//		page.add(contentsPanel);
//		page.add(Box.createRigidArea(new Dimension(3,5)));
//		page.add(footer);
//		page.add(Box.createRigidArea(new Dimension(3,5)));
//
//		InputMap inputMap = getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW);
//		ActionMap actionMap = getRootPane().getActionMap();
//		
//		inputMap.put(escapeKey, escapeKey);
//		inputMap.put(commandPeriodKey, escapeKey);
//		actionMap.put(escapeKey,closeDialogAndDisposeAction);
//		
//		dialog.pack();
//		dialog.setLocationByPlatform(true);
//		showHome();
////		dialog.setVisible(true);
//		return dialog;
//	}

	
	private void reset()	
	{  
		String selectedPanel = getTopCardName();
		boolean isHome = null == selectedPanel;
		Object[] panelOptions = { "Cancel", "All Panels", "This Panel Only"};
		Object[] homeOptions = { "Cancel", "Reset"};
		Object[] options = (isHome) ? homeOptions : panelOptions;
		String prompt = "[TODO] This will set your preferences to the Factory Defaults.";
		if (!isHome)
			prompt += "\nYou can choose to reset just this panel or all panels.";
		int n = JOptionPane.showOptionDialog(dialog,
				prompt,
				"Reset Preferences",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				(isHome ? options[1] : options[2]));
		
		if (n == 1) resetAllPanels();
		if (n == 2) resetCurrentPanel();

	}
	
	protected void resetAllPanels()  {}
	protected void resetCurrentPanel() {
//		AbstractPrefsPanel current = 
		
	}
	public abstract void extract();
	public void savePrefs()	
	{  			

	}
	void openHelp(String s)
	{
		OpenBrowser opener = serviceRegistrar.getService(OpenBrowser.class);
		if (opener != null) opener.openURL(s);
	}
	
	static String helpUrl = "http://manual.cytoscape.org/en/stable/Cytoscape_Preferences.html";
	public void actionPerformed(ActionEvent e)
	{
		String cmd =  e.getActionCommand();
		if ("Help".equals(cmd))  			openHelp(helpUrl);
		else if ("Cancel".equals(cmd))  		setCancelled();
		else if ("Reset".equals(cmd))  		reset();
		else if ("OK".equals(cmd)) 			savePrefs();
		else if (TEXT_MODE.equals(cmd)) 		showTextPage();
	}
	protected boolean cancelled = false;
	private void setCancelled() 			{		cancelled = true;	}
	protected void clearCancelled()	 	{		cancelled = false;	}
	public String getTopCardName()
	{
		for (Component comp : contentsPanel.getComponents()) {
			if (comp.isVisible() == true) {
				JPanel card = (JPanel) comp;
				 return card.getName();
		    }
		}
		return "";
	}
	public void showHome() 
	{		
		show("home");	
		advanced = false; 
//		repack(); 
		adjust();
	}
	
	public void showTextPage() 
	{
		advanced = true;
		show("Advanced");
		install();
//		repack(); 
	}
	
	public void showNext() 
	{	String name = getTopCardName();
		if ("Privacy".equals(name))  		showHome();
		else if ("home".equals(name)) 		show("Behavior");		// CYCLE TO FRONT
		else
		{
			CardLayout layout = (CardLayout) contentsPanel.getLayout();
			layout.next(contentsPanel);
//			repack();
			adjust();		
		}
							
	}		

	public void showPrevious() {	
		String name = getTopCardName();
		if ("Behavior".equals(name))  		showHome();
		else if ("home".equals(name)) 		show("Privacy");		// CYCLE TO BACK
		
		else
		{
			CardLayout layout = (CardLayout) contentsPanel.getLayout();
			layout.previous(contentsPanel);
//			repack();
			adjust();
		}
	}			


	void show(String txt)
	{
		if (txt != null)
		{
			try
			{
				CardLayout layout = (CardLayout) contentsPanel.getLayout();
				layout.first(contentsPanel);
				layout.show(contentsPanel, txt);
				setDialogTitle(); 
			}
			catch (ClassCastException e) {}
		}
//		String title = defaultTitle + (txt == null ? "" : (": " + txt));
//		repack(); 
		adjust();
	}
		
	//---------------------------------------------------------------------------
	/** Add a row of buttons to this PreferencePanel.
	 * Each button corresponds to the component of the same index.
	 * @param buttons
	 * @param components
	 * @param title this is only used if more than 1 row is present
	 */
	public void addButtonRow(JButton[] buttons, String title, Container homePanel) 
	{
//		if(rows.size()==1) 			selectedButton = null;
		Row newRow = new Row(buttons, title);
		rows.add(newRow);
		
		for(int a = 0; a< rows.size(); a++) 
		{
			Row row = rows.get(a);
			row.setBorder(new EmptyBorder(3, 3, 3, 3));
//			row.setBorder(Borders.bevel1);
			row.removeAll();

			row.setBackground((a%2==0) ? color1 : color2);		// isEven
//			AntiAliasedPanel.setSizes(row.label, new Dimension(90,23));
//			VBox padding = new VBox(true, true, row.label);
//			padding.setOpaque(false);
//			row.add(Box.createHorizontalStrut(10));
//			row.add(padding);

			row.add(Box.createHorizontalStrut(60));
			for(int b = 0; b<row.buttons.length; b++) 
			{
				if (row.buttons[b] != null) 
				{
					row.add(row.buttons[b]);
					row.add(Box.createHorizontalStrut(10));
//					JLabel label = new JLabel("\uf0c0" + b);
//					row.add(label);				
				}

			}
			homePanel.add(row);
		}
	}
	public String toString()	{ return getName();	}	
	
	private void repack() {
		Window w = SwingUtilities.getWindowAncestor(this);
		if (w != null) w.pack();
	}
//
	private final  Color color1 = new Color(0x3f,0xEE,0xF2);
	private final  Color color2 = new Color(225,225,225);

	//---------------------------------------------------------------------------
	
	class Row extends JPanel 
	{
		private static final long serialVersionUID = 1L;
		
		JButton[] buttons;
		String title;
//		JToolBar toolbar = new JToolBar();
//		JLabel label = new JLabel();


		public Row(JButton[] inButtons, String inTitle) 
		{
			super();
//			setBorder(BorderFactory.createLineBorder(Color.green));
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			if(isMac)  setUI(new GradientHeaderUI());
			
			Dimension size = new Dimension(AbstractPrefsPanel.getPanelSize());
			size.height = 140;
			AntiAliasedPanel.setSizes(this, size);
			buttons = new JButton[inButtons.length];
			
			System.arraycopy(inButtons,0,buttons,0,buttons.length);
			title = inTitle;
			setOpaque(false);
			
//			if(title!=null)
//				label.setText(title);
//			
//			label.setFont(new Font(Font.SERIF, 0, 12));			//label.getFont().deriveFont(Font.BOLD)
//			label.setMinimumSize(new Dimension(30,21)); // 9	0
//			label.setForeground(Color.BLUE.darker().darker());
//			label.setHorizontalAlignment(SwingConstants.CENTER);
//			
			for(int a = 0; a<buttons.length; a++) 
			{
				if(buttons[a]==null) continue;
				buttons[a].setOpaque(false);
				AntiAliasedPanel.setSizes(buttons[a], new Dimension(160, 100));
//				buttons[a].setBackground(Color.orange);
				buttons[a].addActionListener(buttonListener);
				add(buttons[a]);
			}
		}
	}
	//-------------------------------------------------------------------------------------------------
	private void setDialogTitle()
	{
		String top = getTopCardName();
		String title = "Preferences" + ((top == null) ? "" : (": " + top));
		dialog.setTitle(title);
	}

	private final DialogFooter footer = makeDlogFooter();  
	
	private DialogFooter makeDlogFooter()
	{
		JButton okButton = new JButton("OK"), cancelButton = new JButton("Cancel");
		JButton helpButton = new JButton("Help"), resetButton = new JButton("Reset");
		okButton.setActionCommand("OK");
		cancelButton.setActionCommand("Cancel");
		helpButton.setActionCommand("Help");
		resetButton.setActionCommand("Reset");
		okButton.setToolTipText("Save all preferences and write them to disk");
		cancelButton.setToolTipText("Dismiss this window without making any changes to preferences");
		helpButton.setToolTipText("Launch a web browser going to the Preferences page in the Cytoscape manual");
		resetButton.setToolTipText("Revert parameters to default vaules, made either for the current panel or all preferences");
	   	JButton[] leftControls = new JButton[] {helpButton, resetButton};
	   	JButton[] rightButtons = new JButton[] {okButton, cancelButton};
	   	okButton.addActionListener(this);
	   	cancelButton.addActionListener(this);
	   	helpButton.addActionListener(this);
	   	resetButton.addActionListener(this);
	   	DialogFooter f = new DialogFooter(leftControls,rightButtons,true,null, false);
	   	return f;
	}

	private HBox header = makeDlogHeader();  

	private HBox makeDlogHeader()
	{
	   	header = new HBox(true, false);
	   	header.add(leftButton); 
	   	header.add(homeButton); 
	   	header.add(rightButton); 
	   	header.add(Box.createHorizontalGlue()); 
	   	header.add(Box.createHorizontalGlue()); 
//	   	header.add(textModeButton); 
	   	header.add(modeTabPane); 
//	   	header.setBorder(BorderFactory.createLineBorder(Color.red));
	   	return header;
	}

	abstract public void install();

	/** This action takes the Window associated with the source of this event,
	 * hides it, and then calls <code>dispose()</code> on it.
	 * <P>(This will not throw an exception if there is no parent window,
	 * but it does nothing in that case...)
	 */
	public static Action closeDialogAndDisposeAction = new AbstractAction() {
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Component src = (Component)e.getSource();
			Window w = SwingUtilities.getWindowAncestor(src);
			if(w==null) return;
			
			w.setVisible(false);
			w.dispose();
		}
	};
}
//---------------------------------------------------------------------------
class HairSeparatorUI extends SeparatorUI {
	@Override	public Dimension getMaximumSize(JComponent c) {		return getPreferredSize(c);	}

	@Override	public Dimension getMinimumSize(JComponent c) {		return new Dimension(1,1);	}

	@Override	public Dimension getPreferredSize(JComponent c) 
	{
		if( ((JSeparator)c).getOrientation()==JSeparator.HORIZONTAL)
			return new Dimension(100,1);
		return new Dimension(1,100);
	}

	@Override public void installUI(JComponent c) {
		super.installUI(c);
		Color foreground = Color.gray;
		c.setForeground(foreground);
	}
	
	@Override public void paint(Graphics g, JComponent c) {
		g.setColor(c.getForeground());
		if( ((JSeparator)c).getOrientation()==JSeparator.HORIZONTAL) {
			g.drawLine(0,0,c.getWidth(),0);
		} else {
			g.drawLine(0,0,0,c.getHeight());
		}
	}	
}

class GradientHeaderUI extends PanelUI {

	@Override public void paint(Graphics g, JComponent c)
	{
		super.paint(g,c);
		
		Window w = SwingUtilities.getWindowAncestor(c);
		if(w instanceof JFrame) {
			JFrame frame = (JFrame)w;
			Object obj = frame.getRootPane().getClientProperty("apple.awt.brushMetalLook");
			if(obj !=null && obj.toString().equals("true"))
				return;
		}
		Graphics2D g2 = (Graphics2D)g;
		GradientPaint paint = new GradientPaint( 0,0,new Color(220,220,220), 0,c.getHeight(),new Color(200,200,200) );
		g2.setPaint(paint);
		g2.fillRect(0,0,c.getWidth(),c.getHeight());
	}	
}