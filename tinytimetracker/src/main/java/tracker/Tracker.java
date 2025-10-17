package tracker;

import java.io.*;
import java.util.Calendar;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

/**
 * To compile:
 * 
 * set ANT_HOME=c:\Program Files\Java\jdk1.5.0 ant
 * 
 * To run:
 * 
 * "C:\Program Files\Java\jdk1.5.0\bin\javaw.exe" -classpath "c:\bin\TimeTracker\classes_g;c:\bin\Time Tracker\lib\poi-2.0-RC2.jar" tracker.Tracker
 * 
 * @author rblack
 */
public class Tracker {
    
    public static void main(String[] args) throws MultipleInstancesException {
        new Tracker(Preferences.userNodeForPackage(Tracker.class)).start( args );
    }
    
    Preferences prefs ;
    String dirName = System.getProperty("user.home") + "/timecards"; //$NON-NLS-1$ //$NON-NLS-2$
    boolean logToConsole = false;
    
    public Tracker(Preferences prefs) {
        this.prefs = prefs;
    }
    
    void start(String[] args) throws MultipleInstancesException {
        parseArguments(args);

        File directory = new File(dirName);
        directory.mkdirs();
        
        setupLogging(directory);

        System.out.println("os: " + System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println("Locale: " + Locale.getDefault()); //$NON-NLS-1$

        AutoStartManager autoStartManager = AutoStartManager.createAutoStartManager();

        install(autoStartManager);

        TimeTracker tracker = new TimeTracker(directory, autoStartManager, prefs);
        tracker.setVisible(true);
    }
    
    void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-d")) { //$NON-NLS-1$
                dirName = args[++i];
            } else if (arg.equals("-update")) { //$NON-NLS-1$
                // update = true;
            } else if (arg.equals("-update-check-frequency")) { //$NON-NLS-1$
                i++;
            } else if (arg.equals("-console")) { //$NON-NLS-1$
                logToConsole = true;
            } else if (arg.equals("-locale")) { //$NON-NLS-1$
                Locale.setDefault(new Locale(args[++i]));
            } else {
                System.out.println("Unrecognized argument " + arg); //$NON-NLS-1$
                usage();
            }
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
        }
    }
    
    void setupLogging(File directory) {
        if (!logToConsole) {
            File stdOutFile = new File(directory, "stdout.log"); //$NON-NLS-1$
            File stdErrFile = new File(directory, "stderr.log"); //$NON-NLS-1$
            try {
                System.setOut(new PrintStream(new FileOutputStream(stdOutFile)));
                System.setErr(new PrintStream(new FileOutputStream(stdErrFile)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

  
    
    void install(AutoStart autoStartManager) {
        boolean installed = prefs.getBoolean("installed", false); //$NON-NLS-1$
        if (installed) {
            int firstDayPreference = prefs.getInt("firstDayOfWeek", -1); //$NON-NLS-1$
            if (firstDayPreference == -1) {
                prefs.putInt("firstDayOfWeek", Calendar.SUNDAY); //$NON-NLS-1$
                // This is for backwards compatibility. Versions prior to May 2007 always used Sunday
                // as the 1st day of the week. We don't want everyone to suddenly start using the
                // system 1st day. We want them to be able to control it. If it's not installed
                // yet we should use the system's first day of week. But otherwise, keep using
                // what was used before.
            }
        } else {
            if (autoStartManager != null) {
                autoStartManager.autoStart(true);
            }
            prefs.putBoolean("installed", true); //$NON-NLS-1$
            prefs.putInt("firstDayOfWeek", Calendar.getInstance().getFirstDayOfWeek()); //$NON-NLS-1$
        }
    }

    void usage() {
        System.err.println("Usage: tracker.Tracker [-d <timecard directory>]"); //$NON-NLS-1$
        System.exit(1);
    }
}
