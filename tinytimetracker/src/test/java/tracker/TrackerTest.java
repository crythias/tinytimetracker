package tracker;

import java.util.Calendar;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrackerTest {
    Tracker tracker;
    
    @BeforeEach
    public void setUp() {
        tracker = new Tracker(Preferences.userNodeForPackage(Tracker.class).node("test"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        Preferences.userNodeForPackage(Tracker.class).node("test").clear();
    }
 
    @Test
    public void testParseArguments_All() {
        String[] args = { "-update", "-d", "fooDirectory", "-console", 
                "-update-check-frequency", "5", "-locale", "fooLocale"};
        tracker.parseArguments(args);
        assertTrue( tracker.logToConsole );
        assertEquals( "fooDirectory", tracker.dirName );
        assertEquals( new Locale("fooLocale"), Locale.getDefault() );        
    }
    
    @Test
    public void testParseArguments_None() {
        Object defaultLocale = Locale.getDefault();
        String[] args = { };
        tracker.parseArguments(args);
        assertFalse( tracker.logToConsole );
        assertEquals( System.getProperty("user.home") + "/timecards", tracker.dirName );
        assertEquals(defaultLocale , Locale.getDefault());        
    } 

    @Test
    public void testSetupLogging() { // TODO
    }

    @Test
    public void testInstall_AlreadyInstalled_WithoutAutostart() {
        tracker.prefs.putBoolean("installed", true);
        
        tracker.install(null); // must change firstDayOfWeek if it's not already set
        assertEquals(Calendar.SUNDAY, tracker.prefs.getInt("firstDayOfWeek", -1));

        tracker.prefs.putInt("firstDayOfWeek", Calendar.FRIDAY);
        tracker.install(null); // must not change firstDayOfWeek if it's already set
        assertEquals(Calendar.FRIDAY, tracker.prefs.getInt("firstDayOfWeek", -1));
    }
    
    @Test
    public void testInstall_NotAlreadyInstalled_WithoutAutostart() {
        tracker.install(null);
        assertTrue( tracker.prefs.getBoolean("installed", false) );
        assertEquals( Calendar.getInstance().getFirstDayOfWeek(), 
                tracker.prefs.getInt("firstDayOfWeek", -1) ); 
    }
        
    @Test
    public void testInstall_NotAlreadyInstalled_WithAutostart() {
        MockAutoStart mockAutoStart = new MockAutoStart();
        tracker.install( mockAutoStart );
        assertTrue( mockAutoStart.autoStartCalled );
        assertTrue( mockAutoStart.autoStartValue );
    }
    
    @Test
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
