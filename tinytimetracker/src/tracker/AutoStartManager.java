package tracker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;

import javax.swing.JCheckBoxMenuItem;

public class AutoStartManager implements AutoStart {
    private static final String jnlpUrl = "http://tinytimetracker.sourceforge.net/webstart/tinytimetracker.jnlp"; //$NON-NLS-1$
    private static final String jnlpName = "tinytimetracker.jnlp"; //$NON-NLS-1$

    private JCheckBoxMenuItem autoStartCheckBox;

    public static AutoStartManager createAutoStartManager() {
        File startupJnlp = getStartupJnlp();
        if (startupJnlp != null) {
            return new AutoStartManager();
        }
        return null;
    }
    
    private AutoStartManager() {
        autoStartCheckBox = new JCheckBoxMenuItem(Messages.getString("Tracker.StartTrackerOnWindowsStartup")); //$NON-NLS-1$
        autoStartCheckBox.setSelected(getStartupJnlp().exists());
        autoStartCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autoStart(autoStartCheckBox.isSelected());
            }
        });
    }

    public JCheckBoxMenuItem getAutoStartCheckBox() {
        return autoStartCheckBox; 
    }

    public void autoStart(boolean autoStart) {
        File startupJnlp = getStartupJnlp();
        if (startupJnlp == null)
            return;
        if (autoStart) {
            try {
                String jnlpContents = readJNLP();
                if (jnlpContents == null) {
                    System.err.println("Autostart setting failed: can't read jnlp from " + jnlpUrl); //$NON-NLS-1$
                    return;
                }
                OutputStreamWriter os = new OutputStreamWriter(
                        new FileOutputStream(startupJnlp), "UTF8"); //$NON-NLS-1$
                try {
                    os.write(jnlpContents);
                } finally {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            startupJnlp.delete();
        }
        autoStartCheckBox.setSelected(startupJnlp.exists());
    }

    private static File getStartupJnlp() {
        File startupFolder = getStartupFolder();
        if (startupFolder == null)
            return null;
        return new File(startupFolder, jnlpName);
    }

    private static File getStartupFolder() {
        if (!isWindows())
            return null;
        return new File(
                System.getProperty("user.home") + "\\Start Menu\\Programs\\Startup"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String readJNLP() {
        try {
            return readStream(new URL(jnlpUrl).openStream(), "UTF8"); //$NON-NLS-1$
        } catch (Throwable e) {
            System.err.println("Can't read jnlp from " + jnlpUrl); //$NON-NLS-1$
            e.printStackTrace();
            return null;
        }
    }

    private static String readStream(InputStream is, String encoding)
            throws IOException {

        InputStreamReader r;

        if (encoding != null)
            r = new InputStreamReader(is, encoding);
        else
            r = new InputStreamReader(is);

        return readStream(r);
    }

    private static String readStream(Reader reader) throws IOException {

        StringBuilder builder = new StringBuilder();

        char[] buf = new char[8192];
        int len;

        while ((len = reader.read(buf)) > 0)
            builder.append(buf, 0, len);

        return builder.toString();
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
