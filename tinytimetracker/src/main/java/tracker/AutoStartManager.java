// AutoStartManager is now a stub. WebStart/autostart logic removed for local-only use.
package tracker;

import javax.swing.JCheckBoxMenuItem;

interface AutoStart {
    void autoStart(boolean autoStart);
    JCheckBoxMenuItem getAutoStartCheckBox();
}

public class AutoStartManager implements AutoStart {
    public static AutoStartManager createAutoStartManager() {
        return null;
    }

    @Override
    public void autoStart(boolean autoStart) {
        // No-op: autostart is not supported in local-only mode
    }

    @Override
    public JCheckBoxMenuItem getAutoStartCheckBox() {
        // No-op: autostart is not supported in local-only mode
        return null;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac");
    }
}
