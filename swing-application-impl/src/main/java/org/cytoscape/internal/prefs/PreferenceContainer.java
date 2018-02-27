package org.cytoscape.internal.prefs;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
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
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.SeparatorUI;

import org.cytoscape.internal.prefs.lib.AntiAliasedPanel;
import org.cytoscape.internal.prefs.lib.DialogFooter;
import org.cytoscape.internal.prefs.lib.FontAwesomeIcon;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.VBox;

public class PreferenceContainer extends JPanel  implements ActionListener//implements PrefsStringDefs 
{
	public PreferenceContainer() {		 this("");		}
	static String homeIcon = "\uf015;";
	static String leftArrow = "\uf060;";
	static String rtArrow = "\uf061;";
	
	public PreferenceContainer(String defaultTitle) 
	{
		super();
//		WindowDragger g = new WindowDragger(header);
		this.defaultTitle = defaultTitle;
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
		
		textModeButton.addActionListener(e -> { showTextPage(); });	
		AntiAliasedPanel.setSizes(textModeButton, new Dimension(90,28));
		textModeButton.setFont(new Font("SansSerif", 0, 10));
		textModeButton.setVisible(true);

		repack();
		
		if(isMac) {
			header.setUI(new GradientHeaderUI());
			separator.setForeground(new Color(64,64,64));
		}
	}
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

//	private JButton selectedButton;
//	public String getSelectedPanelName() { return selectedButton == null ? "" : selectedButton.getText();		}
	private final JSeparator separator = new JSeparator();
//	private final FadingPanel contents = new FadingPanel(new GridBagLayout());
	protected final JPanel contentsPanel = new JPanel(new CardLayout());
	
	private final ArrayList<Row> rows = new ArrayList<Row>();
	protected  JPanel homePanel;
	public JPanel getHomePanel()	{ return homePanel;	}
	
	final protected JButton leftButton = new JButton("");
	final protected JButton rightButton = new JButton("");
	final protected JButton homeButton = new JButton("");
	final protected JButton textModeButton = new JButton("Advanced");
	public JButton getHomeButton() {  return homeButton; }

	private String defaultTitle = "Preferences";
	
	public void HomeActionPerformed() {		showHome();			adjust();	}	
	public void NextActionPerformed() {		showNext();			adjust(); }
	public void PrevActionPerformed() {		showPrevious();		adjust(); }

	private void adjust()
	{
//		int idx = getSelectedPanelIndex();
		String selectedPanel = getTopCardName();
		textModeButton.setText("Advanced".equals(selectedPanel) ? "Graphical" : "Tabular");

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
	
	JDialog dialog;
	public JDialog showDlog()
	{
		dialog = createDialog(null,"Preferences");    //TODO don't pass in a null here. 
		dialog.setLocationByPlatform(true);
		dialog.pack();
		showHome();
		dialog.setVisible(true);
		return dialog;
	}
	VBox page = new VBox();

	
	private void reset()	
	{   String resetQuestion = "Your preferences are about to be reset to Factory Defaults. Continue? " ; //TODO INTL
		boolean reallyResetPrefs = true;  // Dialogs.confirm(new DictKey(resetQuestion), false);

		if (reallyResetPrefs)
			Prefs.getPrefs().reset();
		install();
	}
	
	public void savePrefs()	
	{  			

	}
	void openHelp(String s)
	{
		
	}
	public void actionPerformed(ActionEvent e)
	{
		String cmd =  e.getActionCommand();
		if ("Help".equals(cmd))  openHelp("cytoscape preferences");
		else if ("Reset".equals(cmd))  reset();
		else if ("OK".equals(cmd)) 		savePrefs();
		else if ("Text Mode".equals(cmd)) 		showTextPage();
	}
	
	
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
	
	public void showHome() {		show("home");	repack(); adjust();}
	public void showTextPage() 
	{
		String cur = getTopCardName();
		if ("Advanced".equals(cur))
		{
			show("home");
			textModeButton.setText("Tabular");
		}
		else 
		{
			show("Advanced");
			textModeButton.setText("Graphical");
		}
		repack(); 
	}
	
	public void showNext() 
	{	String name = getTopCardName();
		if ("Links".equals(name)) 
			showHome();
		else
		{
			CardLayout layout = (CardLayout) contentsPanel.getLayout();
			layout.next(contentsPanel);
			repack();
			adjust();		
		}
							
	}		

	public void showPrevious() {	
		String name = getTopCardName();
		if ("Groups".equals(name)) 
			showHome();
		else
		{
			CardLayout layout = (CardLayout) contentsPanel.getLayout();
			layout.previous(contentsPanel);
			repack();
			adjust();
		}
	}			


	void show(String txt)
	{
		if (txt != null)//  && compone != null
		{
			try
			{
				CardLayout layout = (CardLayout) contentsPanel.getLayout();
				layout.first(contentsPanel);
				layout.show(contentsPanel, txt);
				setDialogTitle();
//				compone.setVisible(true);
			}
			catch (ClassCastException e) {}
		}
		String title = defaultTitle + (txt == null ? "" : (": " + txt));
		repack(); 
		adjust();
	}
		
	//---------------------------------------------------------------------------
	/** Add a row of buttons to this PreferencePanel.
	 * Each button corresponds to the component of the same index.
	 * @param buttons
	 * @param components
	 * @param title this is only used if more than 1 row is present
	 */
	public void addButtonRow(JButton[] buttons, JComponent[] components, String title) 
	{
//		if(rows.size()==1) 			selectedButton = null;
		Row newRow = new Row(buttons, components, title);
		rows.add(newRow);
		
		for(int a = 0; a< rows.size(); a++) 
		{
			Row row = rows.get(a);
			row.setBorder(new EmptyBorder(3, 3, 3, 3));
//			row.setBorder(Borders.bevel1);
			row.removeAll();

			row.setBackground((a%2==0) ? color1 : color2);		// isEven
			AntiAliasedPanel.setSizes(row.label, new Dimension(90,23));
			VBox padding = new VBox(true, true, row.label);
			padding.setOpaque(false);
//			row.add(Box.createHorizontalStrut(10));
			row.add(padding);

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
//			row.setBorder(BorderFactory.createLineBorder(Color.green));
			homePanel.add(row);
		}
//		Dimension prefSize = homePanel.getPreferredSize();
//		maxContentSize.setSize(prefSize);
//		repack();
	}
	public String toString()	{ return getName();	}	
	//private boolean animateResizing = isMac;
	
	private void repack() {
//		contents.animateStates(layoutRunnable);
		Window w = SwingUtilities.getWindowAncestor(this);
		if (w != null) w.pack();
	}
//
	private final  Color color1 = new Color(0x3f,0xEE,0xF2);
	private final  Color color2 = new Color(225,225,225);

//	private final Dimension maxContentSize = new Dimension(500,500);
	//---------------------------------------------------------------------------
	
	class Row extends JPanel 
	{
		private static final long serialVersionUID = 1L;
		
		JButton[] buttons;
		String title;
		JToolBar toolbar = new JToolBar();
		JLabel label = new JLabel();


		public Row(JButton[] inButtons, JComponent[] inComponents, String inTitle) 
		{
			super();
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
//			setBorder(Borders.cyan);
			if(isMac) {
				setUI(new GradientHeaderUI());
			}
			AntiAliasedPanel.setSizes(this, new Dimension((int) Cy3PreferencesRoot.WINDOW_WIDTH, (int) Cy3PreferencesRoot.ROW_HEIGHT));
			buttons = new JButton[inButtons.length];
			
			System.arraycopy(inButtons,0,buttons,0,buttons.length);
			title = inTitle;
			setOpaque(false);
			
			if(title!=null)
				label.setText(title);
			
			label.setFont(new Font(Font.SERIF, 0, 12));			//label.getFont().deriveFont(Font.BOLD)
			label.setMinimumSize(new Dimension(90,21));
			label.setForeground(Color.BLUE.darker().darker());
			label.setHorizontalAlignment(SwingConstants.CENTER);
			
				
			for(int a = 0; a<buttons.length; a++) 
			{
				if(buttons[a]==null) continue;
				buttons[a].setOpaque(false);
				AntiAliasedPanel.setSizes(buttons[a], new Dimension(80, 80));
				buttons[a].setBackground(Color.orange);
				buttons[a].addActionListener(buttonListener);
				add(buttons[a]);
			}
//			WindowDragger g = new WindowDragger(this);
		}
	}
	//-------------------------------------------------------------------------------------------------
	private void setDialogTitle()
	{
		String top = getTopCardName();
		String title = "Preferences" + ((top == null) ? "" : (": " + top));
		dialog.setTitle(title);
	}

	/** Creates a modal dialog displaying this PreferencePanel. */
	public JDialog createDialog(Frame parent,String name) { 
		JDialog d = new JDialog(parent,name,true);

		if(isMac) {
			//brush metal isn't available for dialogs, only frames
			//d.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
			separator.setForeground(new Color(64,64,64));
		}
		d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.PAGE_AXIS));
		d.getContentPane().add(this);
		d.setResizable(true);			// RESIZABLE
		d.getContentPane().add(page);
		page.add(header);
		page.add(contentsPanel);
//		contents.setBorder(BorderFactory.createLineBorder(Color.blue));
		page.add(Box.createVerticalGlue());
		page.add(footer);
		page.add(Box.createRigidArea(new Dimension(3,5)));

		InputMap inputMap = d.getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = d.getRootPane().getActionMap();
		
		inputMap.put(escapeKey, escapeKey);
		inputMap.put(commandPeriodKey, escapeKey);
		actionMap.put(escapeKey,closeDialogAndDisposeAction);
		
		return d;
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
	   	header.add(textModeButton); 
//	   	header.setBorder(BorderFactory.createLineBorder(Color.red));
	   	return header;
	}

	public void install()
	{
		System.out.println("install");
	}
   

	private static KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	private static KeyStroke commandPeriodKey = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
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
//	public JComponent getSelectedComponent(JButton b) {
//		return b;
//	}
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