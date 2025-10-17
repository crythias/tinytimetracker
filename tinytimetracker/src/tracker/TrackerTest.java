package tracker;

import java.util.Calendar;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;

import junit.framework.TestCase;

public class TrackerTest extends TestCase {
    Tracker tracker;
    
    public void setUp() throws Exception {
        tracker = new Tracker(Preferences.userNodeForPackage(Tracker.class).node("test"));
    }

    public void tearDown() throws Exception {
        Preferences.userNodeForPackage(Tracker.class).node("test").clear();
    }

    public void testParseArguments_All() {
        String[] args = { "-update", "-d", "fooDirectory", "-console", 
                "-update-check-frequency", "5", "-locale", "fooLocale"};
        tracker.parseArguments(args);
        assertTrue( tracker.logToConsole );
        assertEquals( "fooDirectory", tracker.dirName );
        assertEquals( new Locale("fooLocale"), Locale.getDefault() );        
    }
    
    public void testParseArguments_None() {
        Object defaultLocale = Locale.getDefault();
        String[] args = { };
        tracker.parseArguments(args);
        assertFalse( tracker.logToConsole );
        assertEquals( System.getProperty("user.home") + "/timecards", tracker.dirName );
        assertEquals(defaultLocale , Locale.getDefault());        
    } 
    
    public void testSetupLogging() { // TODO
    }
    
    public void testInstall_AlreadyInstalled_WithoutAutostart() {
        tracker.prefs.putBoolean("installed", true);
        
        tracker.install(null); // must change firstDayOfWeek if it's not already set
        assertEquals(Calendar.SUNDAY, tracker.prefs.getInt("firstDayOfWeek", -1));

        tracker.prefs.putInt("firstDayOfWeek", Calendar.FRIDAY);
        tracker.install(null); // must not change firstDayOfWeek if it's already set
        assertEquals(Calendar.FRIDAY, tracker.prefs.getInt("firstDayOfWeek", -1));
    }
    
    public void testInstall_NotAlreadyInstalled_WithoutAutostart() {
        tracker.install(null);
        assertTrue( tracker.prefs.getBoolean("installed", false) );
        assertEquals( Calendar.getInstance().getFirstDayOfWeek(), 
                tracker.prefs.getInt("firstDayOfWeek", -1) ); 
    }
        
    public void testInstall_NotAlreadyInstalled_WithAutostart() {
        MockAutoStart mockAutoStart = new MockAutoStart();
        tracker.install( mockAutoStart );
        assertTrue( mockAutoStart.autoStartCalled );
        assertTrue( mockAutoStart.autoStartValue );
    }
    
    public void testInstall_AlreadyInstalled_WithAutostart() {
        tracker.prefs.putBoolean("installed", true);
        MockAutoStart mockAutoStart = new MockAutoStart();
        
        tracker.install( mockAutoStart );
        assertFalse( mockAutoStart.autoStartCalled );
    }    
    
    public class MockAutoStart implements AutoStart {
        public boolean autoStartCalled = false;
        public boolean autoStartValue = false;
        public void autoStart(boolean autoStart) {
            autoStartCalled = true;
            this.autoStartValue = autoStart;
        }
        public JCheckBoxMenuItem getAutoStartCheckBox() {
            return null;
        }
    }
}
