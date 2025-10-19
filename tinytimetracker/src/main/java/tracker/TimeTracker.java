/*
 * Created on Nov 8, 2004
 *
 * Copyright (c) 2004 iArchives
 * 
 * Updated by Gerald Young 2025
 * 
 */
package tracker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.JTextComponent;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * To compile: 
 * 
 * set ANT_HOME=c:\Program Files\Java\jdk1.5.0
 * ant
 * 
 * To run:
 * 
 * "C:\Program Files\Java\jdk1.5.0\bin\javaw.exe" -classpath "c:\bin\Time Tracker\classes_g;c:\bin\Time Tracker\lib\poi-5.4.1.jar" tracker.Tracker 
 * 
 * @author rblack
 */
public class TimeTracker extends JDialog {
    public static long lastTimeWrittenToFile = System.currentTimeMillis();
    private final ComboBoxModel model;
    private final JComboBox taskCombo;
    private final JLabel timeLabel = new JLabel("    "); //$NON-NLS-1$
    private final JLabel extraLabel = new JLabel();
    private Preferences prefs ;
    private final File directory;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
    private Calendar calendar;
    private Week week;
    private static final String enterTaskHere = "<"+  //$NON-NLS-1$
        Messages.getString("Tracker.EnterTaskHere") + //$NON-NLS-1$
        ">"; //$NON-NLS-1$
    private static final long periodMillis = 60000;
    
    private long hideExtraTextMillis;
    private Object mutex = new Object();
    private MultipleInstancesLock multipleInstancesLock;
    
    public TimeTracker(File directory, AutoStart autoStartManager, Preferences prefs) throws MultipleInstancesException {
        super((JFrame)null, Messages.getString("Tracker.Title")); //$NON-NLS-1$
        this.prefs = prefs;
        this.directory = directory;

        multipleInstancesLock = new MultipleInstancesLock(new File(directory,"TinyTimeTracker.lock")); //$NON-NLS-1$
        multipleInstancesLock.preventMultipleInstances();
        
        initializeCalendar();
        
        setUndecorated(true);
        setAlwaysOnTop(true);
        Mover mover = new Mover();
        timeLabel.setFocusable(true);
        setContentPane(new JPanel() {@Override
            public Point getToolTipLocation(MouseEvent event) {
                return new Point(getWidth() + 5, event.getY() + 20);
            }});
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        JComponent cp = (JComponent)getContentPane();
        cp.setToolTipText(Messages.getString("Tracker.Tooltip")); //$NON-NLS-1$
        
        cp.addMouseMotionListener(mover);
        cp.addMouseListener(mover);
        
        cp.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                ToolTipManager.sharedInstance().setEnabled(false);
                eatIntoPreviousTask(e.getWheelRotation() * ((e.isControlDown() || e.isShiftDown())? 10 : 1));
                ToolTipManager.sharedInstance().setEnabled(true);
            }});
        

        String[] taskArray = loadList();

        if (taskArray == null) {
            taskArray = new String[] {};
        }

        taskCombo = new JComboBox(taskArray);
        taskCombo.setUI(new MyComboBoxUI());
        model = taskCombo.getModel();

        taskCombo.setEditable(true);
        
        addWindowListener(new WindowAdapter() {

//            public void windowActivated(WindowEvent e) {
//
//               Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
//
//               if (focusOwner == getContentPane()) {
//                   taskCombo.requestFocus();
//               }
//            }
            
            public void windowClosing(WindowEvent e) {
                exit();
            }});


        // remove action
        final JPopupMenu comboPopup = new JPopupMenu();
        comboPopup.add(new AbstractAction(Messages.getString("Tracker.RemoveFromDropdown")) { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e) {
                removeCurrentTask();
            }});
        
        ((JComponent)taskCombo.getEditor().getEditorComponent()).setToolTipText(Messages.getString("Tracker.RightClickToRemove")); //$NON-NLS-1$
        
        ((JTextComponent)taskCombo.getEditor().getEditorComponent()).addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                int numModifiers = 0;
                if (e.isControlDown()) numModifiers++;
                if (e.isShiftDown()) numModifiers++;
                if (e.isAltDown()) numModifiers++;
                
                if (numModifiers > 0) {
                    int code = e.getKeyCode();
                    int minutes = numModifiers > 1? 10 : 1;
                    switch(code) {
                        case KeyEvent.VK_UP:
                            eatIntoPreviousTask(-minutes);
                            break;
                        case KeyEvent.VK_DOWN:
                            eatIntoPreviousTask(minutes);
                            break;
                    }
                }
            }});
        
        ((JTextComponent)taskCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {

            boolean eventPosted;
            
            public void insertUpdate(DocumentEvent e) {
                
                if (!eventPosted) {
                    eventPosted = true;
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            autoComplete();
                            eventPosted = false;
                        }});
                }
            }

            public void removeUpdate(DocumentEvent e) {
            }

            public void changedUpdate(DocumentEvent e) {
            }});
        
        taskCombo.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                handleEvent(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleEvent(e);
            }

            public void mouseReleased(MouseEvent e) {
                handleEvent(e);
            }

            /**
             * @param e the event to handle
             */
            private void handleEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = new Point(e.getPoint());
                    Component comp = e.getComponent();
                    p.translate(-comp.getX(), -comp.getY());
                    comboPopup.show(comp, p.x, p.y);
                }
            }});

        taskCombo.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU,0),"showRemoveFromDropdownMenu"); //$NON-NLS-1$
        taskCombo.getActionMap().put("showRemoveFromDropdownMenu", new AbstractAction() { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e) {
                comboPopup.show((Component)e.getSource(), 80, 20);
            }});
        
        firstDayOfWeekMenu = new JMenu(Messages.getString("Tracker.FirstDayOfWeek")); //$NON-NLS-1$
        ButtonGroup firstDayOfWeekButtonGroup = new ButtonGroup();
        for(int weekDay=1; weekDay<=7; weekDay++)
        {
            final int finalWeekDay = weekDay;
            final JRadioButtonMenuItem weekDayRadio = new JRadioButtonMenuItem(week.getLocalizedDayOfWeekFor(weekDay));
            firstDayOfWeekButtonGroup.add(weekDayRadio);
            weekDayRadio.setSelected(calendar.getFirstDayOfWeek() == weekDay);
            weekDayRadio.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (weekDayRadio.isSelected())
                    {
                        setFirstDayOfWeek(finalWeekDay);
                    }
                }});
            firstDayOfWeekMenu.add(weekDayRadio);
        }
        
        final JPopupMenu mainPopup = new JPopupMenu();

        MnemonicActionName name = new MnemonicActionName("Tracker.OpenTimecard"); //$NON-NLS-1$
        AbstractAction openTimecardAction = new AbstractAction(name.actionMessage) {
            public void actionPerformed(ActionEvent e) {
                openTimeCard();
            }
        };
        openTimecardAction.putValue(Action.MNEMONIC_KEY, name.actionMnemonic);
            
        name = new MnemonicActionName("Tracker.OpenPreviousTimecard"); //$NON-NLS-1$
        AbstractAction openPreviousTimecardAction = new AbstractAction(name.actionMessage) {
            public void actionPerformed(ActionEvent e) {
                openLastWeeksCard();
            }
        };
        openPreviousTimecardAction.putValue(Action.MNEMONIC_KEY, name.actionMnemonic);
            
        name = new MnemonicActionName("Tracker.ViewAllTimecards"); //$NON-NLS-1$
        AbstractAction openAllTimecardsAction = new AbstractAction(name.actionMessage) { 
            public void actionPerformed(ActionEvent e) {
                openAllTimeCards();
            }};
        openAllTimecardsAction.putValue(Action.MNEMONIC_KEY, name.actionMnemonic);
        JMenuItem openTimecardItem = mainPopup.add(openTimecardAction);
        Font f = openTimecardItem.getFont();
        openTimecardItem.setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
        mainPopup.add(openPreviousTimecardAction);
        mainPopup.add(openAllTimecardsAction);
        
        // AutoStartManager is now a stub; no autostart menu item.
        
        mainPopup.add(firstDayOfWeekMenu);

        name = new MnemonicActionName("Tracker.Exit"); //$NON-NLS-1$
        Action exitAction = new AbstractAction(name.actionMessage) { 
            public void actionPerformed(ActionEvent e) {
                exit();
            }};
        exitAction.putValue(Action.MNEMONIC_KEY, name.actionMnemonic);
        mainPopup.add(exitAction);
        
        Action displayContextMenu = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                mainPopup.show(timeLabel, 0, 0);
            }
        };
        timeLabel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU,0),"displayContextMenu"); //$NON-NLS-1$
        timeLabel.getActionMap().put("displayContextMenu", displayContextMenu); //$NON-NLS-1$

        MouseAdapter mainPopupAdapter = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                handleEvent(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleEvent(e);
            }

            /**
             * @param e
             */
            private void handleEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = new Point(e.getPoint());
                    Component comp = e.getComponent();
                    p.translate(-comp.getX(), -comp.getY());
                    mainPopup.show(comp, p.x, p.y);
                }
            }};
            
        MouseAdapter doubleClickListener = new MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        openTimeCard();
                    }
                }};
                
        cp.addMouseListener(mainPopupAdapter);
        cp.addMouseListener(doubleClickListener);
        
        taskCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !taskCombo.isPopupVisible()) {
                    leavingTracker();
                }
            }
            
        });
        
        //addMouseMotionListener(new Mover());
        //taskCombo.addMouseMotionListener(new Mover());
        taskCombo.getEditor().getEditorComponent().addMouseMotionListener(new Mover());
        
        taskCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxEdited")) { //$NON-NLS-1$
                    addNewTaskToList();
                }
            }
        });
        
        addWindowFocusListener(new WindowAdapter() {

            public void windowLostFocus(WindowEvent e) {
                leavingTracker();
            }});
        
        taskCombo.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {
                JTextComponent tc = (JTextComponent)taskCombo.getEditor().getEditorComponent();
                tc.selectAll();
            }});
        
        taskCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED ) {
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            if (!taskCombo.isPopupVisible())
                                addNewTaskToList();
                        }});
                }
            }});
        
        model.addListDataListener(new ListDataListener() {

            public void contentsChanged(ListDataEvent e) {
                saveList();
            }

            public void intervalAdded(ListDataEvent e) {
                saveList();
            }

            public void intervalRemoved(ListDataEvent e) {
                saveList();
            }});
        
        cp.setLayout(new FlowLayout(FlowLayout.LEFT, 0 ,0));
        extraLabel.setVisible(false);
        cp.add(taskCombo);
        cp.add(timeLabel);
        cp.add(extraLabel);
        
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        int lx = prefs.getInt("window.x", ss.width / 2); //$NON-NLS-1$
        int ly = prefs.getInt("window.y", ss.height / 2); //$NON-NLS-1$
        setLocation(lx,ly);
        
        pack();
        
        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                long scheduledExecutionTime = scheduledExecutionTime();
                tick(scheduledExecutionTime, scheduledExecutionTime + periodMillis);
            }}, 0, periodMillis);

        if (model.getSize() > 0) {
            taskCombo.setSelectedIndex(0);
        }
        else {
            setTaskComboText(enterTaskHere);
        }

        new Timer().schedule(new TimerTask() {
            Method m = null;
            boolean noMethodFound = false;
            public void run() {
                // this task pops the window to the top periodically so it stays 
                // on top of the task bar (which is also an always-on-top window)
                if (System.currentTimeMillis() > hideExtraTextMillis) {
                    hideExtraText();
                }
            }
        }, 0, 1 * 1000);

    }

    private void setFirstDayOfWeek(int weekDay) {
        boolean doIt = false;
        if (weekDay == calendar.getFirstDayOfWeek())
        {
            // nothing to do, and this prevents us from getting in an infinite event loop
            return;
        }
        if (prefs.getBoolean("warn.firstDayOfWeek", true)) //$NON-NLS-1$
        {
            final JCheckBox dontAskAgain = new JCheckBox(Messages.getString("Tracker.DontAskAgain")); //$NON-NLS-1$
            class Warning extends JPanel
            {
                Warning()
                {
                    super(new BorderLayout());
                    MultiLineLabel label = new MultiLineLabel(Messages.getString("Tracker.ChangingFirstDayInfo"), 400); //$NON-NLS-1$
                    add(label, BorderLayout.CENTER);
                    add(dontAskAgain, BorderLayout.SOUTH);
                }
            };
            Warning warning = new Warning();
            if (JOptionPane.showConfirmDialog(TimeTracker.this, warning, Messages.getString("Tracker.ChangingFirstDayOfWeek"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) //$NON-NLS-1$
            {
                doIt = true;
            }
            if (dontAskAgain.isSelected())
            {
                prefs.putBoolean("warn.firstDayOfWeek", false); //$NON-NLS-1$
            }
        }
        else
        {
            doIt = true;
        }
        if (doIt)
        {
            prefs.putInt("firstDayOfWeek", weekDay); //$NON-NLS-1$
            initializeCalendar(); 
            tickNow();
        }
        else
        {
            for(int i=0; i<7; i++)
            {
                final JMenuItem item = firstDayOfWeekMenu.getItem(i);
                if (i+1 == calendar.getFirstDayOfWeek())
                {
                    item.setSelected(true);
                }
            }
        }
    }

    private void initializeCalendar() {
        calendar = Calendar.getInstance();

        int firstDayPreference = prefs.getInt("firstDayOfWeek", 1); //$NON-NLS-1$
        calendar.setFirstDayOfWeek(firstDayPreference);

        week = new Week( calendar.getFirstDayOfWeek() );
        System.out.println("First day of week being used: "+week.getLocalizedDayOfWeekFor(calendar.getFirstDayOfWeek())); //$NON-NLS-1$
    }
    
    private void leavingTracker() {
        String currentTask = getCurrentTask();
        if (currentTask == null || currentTask.length()==0) {
            setTaskComboText(enterTaskHere);
        }
        else {
            setTaskComboText(currentTask);
        }
        // getContentPane().requestFocus();
    }

    private void autoComplete() {
        JTextComponent tc = (JTextComponent) taskCombo.getEditor().getEditorComponent();
        String text = tc.getText();
        String textLower = text.toLowerCase();
        int size = model.getSize();
        for (int i=0; i<size; i++) {
            String item = (String) model.getElementAt(i);
            if (item.toLowerCase().startsWith(textLower)) {
                int length = text.length();
                tc.setText(text + item.substring(length));
                tc.setSelectionStart(length);
                tc.setSelectionEnd(item.length());
                return;
            }
        }
    }
    
    /**
     * @param wheelRotation
     */
    private void eatIntoPreviousTask(int minutes) {
        synchronized(mutex)
        {
            try {
                TimecardSpreadsheet timecard = new TimecardSpreadsheet(getWeekFile(System.currentTimeMillis()), System.currentTimeMillis(), week);
                boolean[] beep = new boolean[1];
                TaskRow otherAffectedTask = timecard.moveLastSwitch(minutes * 60, beep);
                if (beep[0]) {
                    beep();
                }
                else {
                    long currentTaskDailyTotal = timecard.save();
                    
                    if (otherAffectedTask != null) {
                        setExtraText(otherAffectedTask.getTask() + ": " +  //$NON-NLS-1$
                                formatTime(Math.round(otherAffectedTask.getDurationMillis() / 1000f)));
                    }
                    showTime(currentTaskDailyTotal);
                }
            } catch (Exception e) {
                setLabel(e);
            }
        }
    }

    private static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }
    
    private void setTaskComboText(String text) {
        ((JTextComponent)taskCombo.getEditor().getEditorComponent()).setText(text);
    }
    
    private void setLabel(String text) {
        synchronized(mutex)
        {
            timeLabel.setText(" " + text); //$NON-NLS-1$
            pack();
            if (!movedOnScreen)
            {
                // only do this once per session so people can move it off the 
                // screen a bit if they want to.
                keepOnScreen();
                movedOnScreen=true;
            }
        }
    }
    
    private void setLabel(Throwable t) {
        setLabel(t.getMessage());
        t.printStackTrace();
    }
    
    private void tick(long scheduledExecutionTime, long nextExecutionTime) {
        synchronized(mutex)
        {
            try {
    
                String currentTask = getCurrentTask();
                if (currentTask.length() == 0) {
                    setLabel("     "); //$NON-NLS-1$
                    return;
                }
                
                calendar.setTimeInMillis(scheduledExecutionTime);
                int thisDay = calendar.get(Calendar.DAY_OF_WEEK);
                calendar.setTimeInMillis(nextExecutionTime);
                int nextDay = calendar.get(Calendar.DAY_OF_WEEK);
                
                // see if we should log or we should wait til the next one
                // When currentTimeMillis > nextExecution time, we're playing catch-up, 
                // possibly due to coming out of hibernate, or a high-priority application is consuming
                // our cpu.
                if (System.currentTimeMillis() < nextExecutionTime /* We're current, not catching up */ 
                        || thisDay != nextDay /* Day transition */) {
                    
                    TimecardSpreadsheet timecard = new TimecardSpreadsheet(getWeekFile(scheduledExecutionTime), scheduledExecutionTime, week);
                    timecard.tick(currentTask);
                    showTime(timecard.save());
                }
                
            } catch (Exception e) {
                setLabel(e);
            }
        }
    }
        
    /**
     * @param newTime
     */
    private void showTime(long seconds) {
        setLabel(formatTime(seconds));
    }

    /**
     * @param time
     * @return
     */
    private File getWeekFile(long time) {
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        String dateString = df.format(calendar.getTime());
        return new File(directory, "timecard." + dateString + ".xls"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected String[] loadList()
    { 
        String names[];
        try
        {
            names = prefs.keys();
        }
        catch(BackingStoreException e)
        {
            throw new RuntimeException(e);
        }
        Arrays.sort(names);
        List<String> retVal = new ArrayList<String>(names.length);
        for(int i = 0; i < names.length; i++)
        {
            String name = names[i];
            if(!name.startsWith("task.")) //$NON-NLS-1$
                continue;
            String val = prefs.get(name, ""); //$NON-NLS-1$
            if(val.length() > 0)
                retVal.add(val);
        }

        String taskArray[] = (String[])(String[])getPrefsObject("taskArray"); //$NON-NLS-1$
        if(retVal.size() > 0)
            return (String[])retVal.toArray(new String[retVal.size()]);
        else
            return taskArray;
    }

    protected void saveList()
    {
        Set<String> toRemove;
        try
        {
            toRemove = new HashSet<String>(Arrays.asList(prefs.keys()));
        }
        catch(BackingStoreException e)
        {
            throw new RuntimeException(e);
        }
        int itemCount = model.getSize();
        for(int i = 0; i < itemCount; i++)
        {
            String item = (String)model.getElementAt(i);
            String prefName = (new StringBuilder()).append("task.").append(pad(i, 4)).toString(); //$NON-NLS-1$
            prefs.put(prefName, item);
            toRemove.remove(prefName);
        }

        Iterator<String> i$ = toRemove.iterator();
        do
        {
            if(!i$.hasNext())
                break;
            String prefName = (String)i$.next();
            if(prefName.startsWith("task.")) //$NON-NLS-1$
                prefs.remove(prefName);
        } while(true);
    }

    public static String pad(int i, int minNumDigits)
    {
        String s;
        for(s = String.valueOf(i); s.length() < minNumDigits; s = (new StringBuilder()).append("0").append(s).toString()); //$NON-NLS-1$
        return s;
    }

    private Object getPrefsObject(String key) {
        byte[] bytes = prefs.getByteArray(key, null);
        if (bytes != null) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                return ois.readObject();
            }
            catch (Exception e) {
                e.printStackTrace();
            } // not fatal
        }
        return null;
    }

    /**
     * 
     */
    protected void addNewTaskToList() {
        
        String currentTask = taskCombo.getEditor().getItem().toString();
        if (currentTask == null || currentTask.equals(enterTaskHere) || currentTask.length()==0) return; // don't add that.
        
        // see if it's already in there ignoring case
        int itemCount = model.getSize();
        for (int i = 0; i < itemCount; i++) {
            String item = (String) model.getElementAt(i);
            if (item.equalsIgnoreCase(currentTask)) {
                currentTask = item;
                break;
            }
        }
        
        Object firstItem = taskCombo.getItemAt(0);
        if (firstItem == null || !firstItem.equals(currentTask)) {
            // put it to the top
            taskCombo.removeItem(currentTask);
            taskCombo.insertItemAt(currentTask, 0);
            taskCombo.setSelectedIndex(0);
            //setTaskComboText(currentTask);

            tickNow();
        }
        
    }

    /**
     * 
     */
    private void tickNow() {
        new Thread() {
            public void run() {
                // hit the file again
                long now = System.currentTimeMillis();
                long nextExecutionTime = lastTimeWrittenToFile + periodMillis;
                if (now < nextExecutionTime) {
                    tick(now, nextExecutionTime);
                }
            }
        }.start();
    }

    private String getCurrentTask() {
        Object item = taskCombo.getSelectedItem();
        if (item == null) return ""; //$NON-NLS-1$
        String currentTask = item.toString().trim();
        if (currentTask.equals(enterTaskHere)) return ""; //$NON-NLS-1$
        return currentTask;
    }
     
    private static String formatTime(long seconds)
    {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuffer buf = new StringBuffer();

        if (days > 0) {
            buf.append(days);
            buf.append(':');
        }

        if (hours < 10)
            buf.append('0');
        buf.append(hours);
        buf.append(':');

        if (minutes < 10)
            buf.append('0');
        buf.append(minutes);
//        buf.append(':');
//
//        if (seconds < 10)
//            buf.append('0');
//        buf.append(seconds);

        return buf.toString();
    }
    
    private void openTimeCard() {
        shellExec(getWeekFile(System.currentTimeMillis()).getAbsolutePath(), Messages.getString("Tracker.ErrorViewingTimecard.Message"), Messages.getString("Tracker.ErrorViewingTimecard.Title")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    private void openLastWeeksCard() {
        final File lastWeek = getWeekFile(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
        if (lastWeek.exists()) {
            shellExec(lastWeek.getAbsolutePath(), Messages.getString("Tracker.ErrorViewingTimecard.Message"), Messages.getString("Tracker.ErrorViewingTimecard.Title")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            EventQueue.invokeLater(new Runnable() {

                public void run()
                {
                    JOptionPane.showMessageDialog(TimeTracker.this, Messages.getString("Tracker.ErrorPreviousTimecardDoesNotExist.Message"), Messages.getString("Tracker.ErrorViewingTimecard.Title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                }
            });
        }
    }
    private void openAllTimeCards() {
        shellExec(getWeekFile(System.currentTimeMillis()).getParentFile().getAbsolutePath(), Messages.getString("Tracker.ErrorViewingDirectory.Message"), Messages.getString("Tracker.ErrorViewingDirectory.Title")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private void shellExec(final String toExec, final String msg, final String title)
    {
        new Thread() {

            public void run()
            {
                try
                {
                    int retValue = shellExec(toExec);
                    if(retValue != 0)
                        EventQueue.invokeLater(new Runnable() {

                            public void run()
                            {
                                JOptionPane.showMessageDialog(TimeTracker.this, msg, title, 0);
                            }
                        });
                }
                catch(final Throwable t)
                {
                    EventQueue.invokeLater(new Runnable() {

                        public void run()
                        {
                            setLabel(t);
                        }
                    });
                }
            }

        }.start();
    }

    /**
     * Replaced: Attempts to open the given file or directory using platform-specific commands.
     * 
     * @param toExec the file or directory to open
     * @return -1 if all attempts failed, 0 if one succeeded
     * @throws InterruptedException if the thread is interrupted while waiting for a process to complete
     */
    private int shellExec(final String toExec) throws InterruptedException {
        String[][] fallbackCommands;

        if (AutoStartManager.isWindows()) {
            fallbackCommands = new String[][] {
                { "explorer", toExec }
            };
        } else if (AutoStartManager.isMac()) {
            fallbackCommands = new String[][] {
                { "/usr/bin/open", toExec }
            };
        } else {
        // Linux and others
            fallbackCommands = new String[][] {
                { "xdg-open", toExec },
                { "gio", "open", toExec },
                { "nautilus", "--no-desktop", toExec },
                { "kfmclient", "exec", toExec }
            };
        }

        for (String[] cmd : fallbackCommands) {
            try {
                System.out.println("Trying: " + String.join(" ", cmd));
                Process process = Runtime.getRuntime().exec(cmd);
                int ret = StreamCopier.copyProcessStreams(process).waitFor();
                if (ret == 0) return 0;
                System.err.println("Command failed with code: " + ret);
            } catch (IOException e) {
                System.err.println("Command failed: " + Arrays.toString(cmd));
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * Removes the current task from the dropdown list.
     *  
     */
    private void removeCurrentTask() {
        Object selectedItem = taskCombo.getSelectedItem();
        if (selectedItem != null)
            taskCombo.removeItem(selectedItem);
        taskCombo.setSelectedItem(null);
        setTaskComboText(enterTaskHere);
    }

    /**
     * Mouse listener and motion listener to move the window around.
     * @author rblack
     * 
     */
    private class Mover implements MouseMotionListener, MouseListener {

        private Point pressPoint = null;;
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
         */
        public void mouseDragged(MouseEvent e) {
            if (pressPoint == null) return;
            Point los = getLocationOnScreen();
            Point dragPoint = e.getPoint();
            dragPoint.translate(los.x, los.y);
            int dx = dragPoint.x - pressPoint.x;
            int dy = dragPoint.y - pressPoint.y;
            Point newLocation = new Point(los.x + dx, los.y + dy);
            setLocation(newLocation);
            prefs.putInt("window.x", newLocation.x); //$NON-NLS-1$
            prefs.putInt("window.y", newLocation.y); //$NON-NLS-1$
            
            pressPoint = dragPoint;
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
         */
        public void mouseMoved(MouseEvent e) {
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent e) {
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(MouseEvent e) {
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent e) {
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent e) {
            pressPoint = new Point(e.getPoint());
            Point los = getLocationOnScreen();
            pressPoint.translate(los.x, los.y);
            timeLabel.requestFocus();
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent e) {
            pressPoint = null;
        }
        
    }
    
    /**
     * creates a custom ComboBoxUI to provide a custom ComboPopup
     * @author rblack
     */
    private class MyComboBoxUI extends BasicComboBoxUI {

        @Override
        protected ComboPopup createPopup() {
            return new MyComboPopup(comboBox);
    }
}
    
    private class MyComboPopup extends BasicComboPopup {

        /**
         * @param combo
         */
        public MyComboPopup(JComboBox combo) {
            super(combo);
        }
        
        /* (non-Javadoc)
         * @see javax.swing.plaf.basic.BasicComboPopup#createListMouseListener()
         */
        protected MouseListener createListMouseListener() {
            return new DelegatingMouseListener(super.createListMouseListener());
        }
    }

    private class DelegatingMouseListener implements MouseListener {
        private MouseListener delegate;

        /**
         * @param delegate
         */
        public DelegatingMouseListener(MouseListener delegate) {
            this.delegate = delegate;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return delegate.hashCode();
        }
        /**
         * @param e
         */
        public void mouseClicked(MouseEvent e) {
            delegate.mouseClicked(e);
        }
        /**
         * @param e
         */
        public void mouseEntered(MouseEvent e) {
            delegate.mouseEntered(e);
        }
        /**
         * @param e
         */
        public void mouseExited(MouseEvent e) {
            delegate.mouseExited(e);
        }
        /**
         * @param e
         */
        public void mousePressed(MouseEvent e) {
            delegate.mousePressed(e);
        }
        /**
         * @param e
         */
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                final JList list = (JList) e.getComponent();
                final int selectedIndex = list.getSelectedIndex();
                taskCombo.removeItemAt(selectedIndex);
            }
            else {
                delegate.mouseReleased(e);
            }
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return delegate.toString();
        }
    }
    
    private JMenu firstDayOfWeekMenu;
    private boolean movedOnScreen;
    
    private void setExtraText(String text) {
        hideExtraTextMillis = System.currentTimeMillis() + 5000;
        extraLabel.setText("     " + text + " "); //$NON-NLS-1$ //$NON-NLS-2$
        extraLabel.setVisible(true);
    }
    
    private void hideExtraText() {
        if (extraLabel.isVisible()) {
            extraLabel.setVisible(false);
            pack();
        }
    }

    private void exit() {
        multipleInstancesLock.allowOtherInstances();
        System.exit(0);
    }
    
    public void pack()
    {
        super.pack();
    }
    
    private void keepOnScreen()
    {
        // set the location, keep it on the screen
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        Point loc = getLocation();
        int lx = loc.x;
        int ly = loc.y;
        
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        if (!AutoStartManager.isMac()) 
        {
            // don't let it hide behind the insets for macs since macs don't support 
            // alwaysontop of of menus or docks
            screenInsets.top = screenInsets.bottom = screenInsets.left = screenInsets.right = 0;
        }
        if (lx < screenInsets.left) lx = screenInsets.left;
        if (ly < screenInsets.top) ly = screenInsets.top;
        if (lx > ss.width - screenInsets.right - getWidth()) lx = ss.width - screenInsets.right - getWidth();
        if (ly > ss.height - screenInsets.bottom - getHeight() ) ly = ss.height - screenInsets.bottom - getHeight();
        /*
        if (lx < 0) lx = 0;
        if (ly < 0) ly = 0;
        if (lx + getWidth() > ss.width) lx = ss.width - getWidth();
        if (ly + getHeight() > ss.height) ly = ss.height- getHeight();
        */
        setLocation(new Point(lx, ly));
    }
}
